package no.vibrantfounder.bachelor.ai.client.dto;

import java.util.List;

/**
 * Response payload for the Anthropic Messages API.
 *
 * This DTO maps directly to the external API contract and
 * intentionally includes only fields used by the application.
 */
public record AnthropicMessageResponse(
        String id,
        String type,
        String role,
        String model,
        List<AnthropicContentBlock> content,
        String stop_reason,
        AnthropicUsage usage
) {
}
