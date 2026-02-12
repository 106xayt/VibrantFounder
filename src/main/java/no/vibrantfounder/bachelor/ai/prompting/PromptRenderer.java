package no.vibrantfounder.bachelor.ai.prompting;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Renders prompt templates by replacing placeholder variables
 * in the form {{variableName}} with provided values.
 *
 * This class intentionally uses simple string replacement to
 * keep prompt rendering transparent and predictable.
 */
@Component
public class PromptRenderer {

    public String render(String template, Map<String, String> variables) {
        if (template == null || variables == null || variables.isEmpty()) {
            return template;
        }

        String rendered = template;

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            rendered = rendered.replace(placeholder, entry.getValue());
        }

        return rendered;
    }
}
