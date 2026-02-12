package no.vibrantfounder.bachelor.ai.client;

import no.vibrantfounder.bachelor.ai.client.dto.AnthropicMessageRequest;
import no.vibrantfounder.bachelor.ai.client.dto.AnthropicMessageResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * HTTP-based implementation of the AnthropicClient using Spring's RestClient.
 */
@Component
public class AnthropicHttpClient implements AnthropicClient {

    private final RestClient restClient;

    public AnthropicHttpClient(RestClient anthropicRestClient) {
        this.restClient = anthropicRestClient;
    }

    @Override
    public AnthropicMessageResponse createMessage(AnthropicMessageRequest request) {
        return restClient
                .post()
                .uri("/v1/messages")
                .body(request)
                .retrieve()
                .body(AnthropicMessageResponse.class);
    }
}
