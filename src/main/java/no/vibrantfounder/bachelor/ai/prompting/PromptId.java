package no.vibrantfounder.bachelor.ai.prompting;

/**
 * Identifiers for all supported AI prompt types in the system.
 * Each PromptId maps to a concrete prompt template stored in resources/prompts.
 *
 * This enum defines the explicit AI capabilities of the system and
 * acts as a contract between domain logic and AI orchestration.
 */
public enum PromptId {

    /**
     * Generates a full marketing decision-support plan based on
     * a given business profile and constraints.
     */
    MARKETING_PLAN_V1,

    /**
     * Repairs invalid or malformed AI output into valid JSON
     * that conforms to the expected output schema.
     */
    FORMAT_REPAIR_V1
}
