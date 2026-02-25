package no.vibrantfounder.bachelor.ai.orchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vibrantfounder.bachelor.ai.client.AnthropicClient;
import no.vibrantfounder.bachelor.ai.client.dto.AnthropicContentBlock;
import no.vibrantfounder.bachelor.ai.client.dto.AnthropicMessage;
import no.vibrantfounder.bachelor.ai.client.dto.AnthropicMessageRequest;
import no.vibrantfounder.bachelor.ai.client.dto.AnthropicMessageResponse;
import no.vibrantfounder.bachelor.ai.prompting.PromptId;
import no.vibrantfounder.bachelor.ai.prompting.PromptRenderer;
import no.vibrantfounder.bachelor.ai.prompting.PromptTemplateService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Central orchestration component for all AI interactions.
 *
 * Responsibilities:
 * - Render prompts
 * - Execute AI calls
 * - Parse structured JSON output
 * - Validate AI results
 * - Provide robust error handling (including JSON repair)
 */
@Service
public class AiOrchestrator {

    private final AnthropicClient anthropicClient;
    private final PromptTemplateService promptTemplateService;
    private final PromptRenderer promptRenderer;
    private final ObjectMapper objectMapper;
    private final OutputValidator<Object> outputValidator;

    // One repair attempt is usually enough; keep it tight to avoid loops.
    private static final int MAX_REPAIR_ATTEMPTS = 1;

    public AiOrchestrator(
            AnthropicClient anthropicClient,
            PromptTemplateService promptTemplateService,
            PromptRenderer promptRenderer,
            ObjectMapper objectMapper,
            OutputValidator<Object> outputValidator
    ) {
        this.anthropicClient = anthropicClient;
        this.promptTemplateService = promptTemplateService;
        this.promptRenderer = promptRenderer;
        this.objectMapper = objectMapper;
        this.outputValidator = outputValidator;
    }

    public <T> AiResult<T> callForJson(
            PromptId promptId,
            Map<String, String> variables,
            Class<T> targetClass,
            AiCallOptions options
    ) {
        // 1) Primary call
        AnthropicMessageResponse primaryResponse = callAnthropic(promptId, variables, options);
        String rawText = extractText(primaryResponse);

        if (rawText == null || rawText.isBlank()) {
            throw new AiException(AiException.Type.BAD_OUTPUT, "AI returned empty response.");
        }

        // 2) Parse attempt (strict-ish)
        try {
            T parsed = parseAndValidate(rawText, targetClass);
            return toResult(parsed, rawText, primaryResponse);

        } catch (AiException e) {
            // Only attempt repair for BAD_OUTPUT kinds
            if (e.getType() != AiException.Type.BAD_OUTPUT) {
                throw e;
            }
        } catch (Exception ignored) {
            // Fall through into repair
        }

        // 3) Repair flow
        String repaired = tryRepairJson(rawText, options);
        if (repaired == null || repaired.isBlank()) {
            throw new AiException(AiException.Type.BAD_OUTPUT, "AI returned invalid JSON output.");
        }

        try {
            T parsed = parseAndValidate(repaired, targetClass);

            // Keep rawText from the FIRST call for debugging. If you want, append repaired text here.
            return new AiResult<>(
                    parsed,
                    rawText,
                    primaryResponse.usage() != null ? primaryResponse.usage().input_tokens() : null,
                    primaryResponse.usage() != null ? primaryResponse.usage().output_tokens() : null,
                    primaryResponse.stop_reason()
            );
        } catch (Exception e) {
            throw new AiException(
                    AiException.Type.BAD_OUTPUT,
                    "AI returned invalid JSON output (even after repair).",
                    e
            );
        }
    }

    private <T> T parseAndValidate(String rawText, Class<T> targetClass) throws Exception {
        String json = extractJsonObjectLenient(rawText);
        T parsed = objectMapper.readValue(json, targetClass);
        outputValidator.validate(parsed);
        return parsed;
    }

    /**
     * ✅ FIX: generisk toResult, så vi får AiResult<T> (ikke AiResult<?>)
     */
    private <T> AiResult<T> toResult(T parsed, String rawText, AnthropicMessageResponse response) {
        return new AiResult<>(
                parsed,
                rawText,
                response.usage() != null ? response.usage().input_tokens() : null,
                response.usage() != null ? response.usage().output_tokens() : null,
                response.stop_reason()
        );
    }

    private AnthropicMessageResponse callAnthropic(PromptId promptId, Map<String, String> variables, AiCallOptions options) {
        String systemPrompt = promptRenderer.render(
                promptTemplateService.loadSystem(promptId),
                variables
        );

        String userPrompt = promptRenderer.render(
                promptTemplateService.loadUser(promptId),
                variables
        );

        AnthropicMessageRequest request = new AnthropicMessageRequest(
                options.model(),
                options.maxTokens(),
                options.temperature(),
                systemPrompt,
                List.of(
                        new AnthropicMessage(
                                "user",
                                List.of(new AnthropicContentBlock("text", userPrompt))
                        )
                )
        );

        return anthropicClient.createMessage(request);
    }

    private String tryRepairJson(String rawText, AiCallOptions options) {
        PromptId repairPrompt = PromptId.FORMAT_REPAIR_V1;

        Map<String, String> repairVars = Map.of("raw_output", rawText);

        String last = null;

        for (int attempt = 0; attempt < MAX_REPAIR_ATTEMPTS; attempt++) {
            AnthropicMessageResponse repairResponse = callAnthropic(repairPrompt, repairVars, options);
            last = extractText(repairResponse);

            if (last == null || last.isBlank()) continue;

            // Make sure we return a JSON object (strip fences/explanations if any).
            try {
                return extractJsonObjectLenient(last);
            } catch (Exception ignored) {
                // keep looping
            }
        }

        return last;
    }

    private String extractText(AnthropicMessageResponse response) {
        if (response == null || response.content() == null) {
            return "";
        }

        return response.content().stream()
                .filter(block -> "text".equals(block.type()))
                .map(AnthropicContentBlock::text)
                .collect(Collectors.joining("\n"))
                .trim();
    }

    /**
     * Extract a JSON object from a text response.
     * Lenient by design:
     * - accepts ```json fences
     * - accepts extra text before/after JSON
     * - DOES NOT do brace-balance hard fail (we let parser decide, then repair)
     */
    private String extractJsonObjectLenient(String rawText) {
        String s = rawText == null ? "" : rawText.trim();

        // Strip ```json ... ``` or ``` ... ```
        if (s.startsWith("```")) {
            int firstNewline = s.indexOf('\n');
            if (firstNewline > 0) {
                s = s.substring(firstNewline + 1);
            }
            int lastFence = s.lastIndexOf("```");
            if (lastFence >= 0) {
                s = s.substring(0, lastFence);
            }
            s = s.trim();
        }

        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start < 0 || end < 0 || end <= start) {
            throw new AiException(AiException.Type.BAD_OUTPUT, "AI did not return a JSON object.");
        }

        return s.substring(start, end + 1).trim();
    }
}