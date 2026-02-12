package no.vibrantfounder.bachelor.ai.client.dto;

import java.util.List;

/**
 * A single message in the Anthropic Messages API format.
 */
public record AnthropicMessage(
        String role,
        List<AnthropicContentBlock> content
) {
}
