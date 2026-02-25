package no.vibrantfounder.bachelor.ai.client;

import no.vibrantfounder.bachelor.ai.client.dto.AnthropicMessageRequest;
import no.vibrantfounder.bachelor.ai.client.dto.AnthropicMessageResponse;
import no.vibrantfounder.bachelor.ai.config.AnthropicProperties;
import no.vibrantfounder.bachelor.ai.orchestration.AiException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class AnthropicHttpClient implements AnthropicClient {

    private final RestClient anthropicRestClient;
    private final AnthropicProperties properties;

    public AnthropicHttpClient(RestClient anthropicRestClient, AnthropicProperties properties) {
        this.anthropicRestClient = anthropicRestClient;
        this.properties = properties;
    }

    @Override
    public AnthropicMessageResponse createMessage(AnthropicMessageRequest request) {
        try {
            return anthropicRestClient.post()
                    .uri("/v1/messages")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new AiException(
                                AiException.Type.PROVIDER_ERROR,
                                "Anthropic returned HTTP " + res.getStatusCode().value()
                        );
                    })
                    .body(AnthropicMessageResponse.class);

        } catch (ResourceAccessException e) {
            // This is typically timeout / DNS / blocked connection
            String msg = e.getMostSpecificCause() != null
                    ? e.getMostSpecificCause().getClass().getSimpleName() + ": " + e.getMostSpecificCause().getMessage()
                    : e.getMessage();

            throw new AiException(
                    AiException.Type.PROVIDER_ERROR,
                    "Anthropic request failed (timeout/network). timeout=" + properties.timeout() + " details=" + msg,
                    e
            );

        } catch (AiException e) {
            throw e;

        } catch (Exception e) {
            throw new AiException(
                    AiException.Type.PROVIDER_ERROR,
                    "Anthropic request failed.",
                    e
            );
        }
    }
}