package no.vibrantfounder.bachelor.ai.client.dto;

import java.util.List;

/**
 * Request payload for the Anthropic Messages API.
 *
 * This DTO maps directly to the external API contract and
 * should not contain any business logic.
 */
public record AnthropicMessageRequest(
        String model,
        Integer max_tokens,
        Double temperature,
        String system,
        List<AnthropicMessage> messages
) {
}
