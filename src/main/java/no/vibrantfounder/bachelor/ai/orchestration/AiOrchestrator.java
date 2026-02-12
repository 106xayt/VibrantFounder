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
 * - Provide robust error handling
 */
@Service
public class AiOrchestrator {

    private final AnthropicClient anthropicClient;
    private final PromptTemplateService promptTemplateService;
    private final PromptRenderer promptRenderer;
    private final ObjectMapper objectMapper;
    private final OutputValidator<Object> outputValidator;

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
        String rawText = null;

        try {
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

            AnthropicMessageResponse response = anthropicClient.createMessage(request);
            rawText = extractText(response);

            // Basic guard against truncated JSON
            if (rawText == null || rawText.isBlank()) {
                throw new AiException("AI returned empty response");
            }

            if (!rawText.trim().endsWith("}")) {
                throw new AiException("AI returned incomplete JSON: " + rawText);
            }

            T parsed = objectMapper.readValue(rawText, targetClass);
            outputValidator.validate(parsed);

            return new AiResult<>(
                    parsed,
                    rawText,
                    response.usage() != null ? response.usage().input_tokens() : null,
                    response.usage() != null ? response.usage().output_tokens() : null,
                    response.stop_reason()
            );

        } catch (AiException e) {
            throw e;
        } catch (Exception e) {
            throw new AiException(
                    "AI orchestration failed. Raw response: " + rawText,
                    e
            );
        }
    }

    private String extractText(AnthropicMessageResponse response) {
        if (response.content() == null) {
            return "";
        }

        return response.content().stream()
                .filter(block -> "text".equals(block.type()))
                .map(AnthropicContentBlock::text)
                .collect(Collectors.joining("\n"))
                .trim();
    }
}
