package no.vibrantfounder.bachelor.ai.client.dto;

/**
 * Token usage information returned by the Anthropic API.
 */
public record AnthropicUsage(
        Integer input_tokens,
        Integer output_tokens
) {
}
