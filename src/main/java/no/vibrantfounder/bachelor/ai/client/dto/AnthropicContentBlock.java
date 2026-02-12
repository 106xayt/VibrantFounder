package no.vibrantfounder.bachelor.ai.client.dto;

/**
 * Content block used in Anthropic Messages API.
 *
 * In this project, only text blocks are used.
 */
public record AnthropicContentBlock(
        String type,
        String text
) {
}
