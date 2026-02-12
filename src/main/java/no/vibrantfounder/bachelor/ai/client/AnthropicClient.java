package no.vibrantfounder.bachelor.ai.client;

import no.vibrantfounder.bachelor.ai.client.dto.AnthropicMessageRequest;
import no.vibrantfounder.bachelor.ai.client.dto.AnthropicMessageResponse;

/**
 * Abstraction for communicating with the Anthropic Messages API.
 *
 * The rest of the system should depend on this interface, not on
 * HTTP details or provider-specific implementations.
 */
public interface AnthropicClient {

    AnthropicMessageResponse createMessage(AnthropicMessageRequest request);
}
