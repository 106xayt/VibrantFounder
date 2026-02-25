package no.vibrantfounder.bachelor.marketing.api.dto;

import java.util.List;

/**
 * Content idea/asset the UI can render in "Content Ideas & Assets".
 */
public record ContentIdeaDto(
        String id,                          // uuid string recommended
        String type,                        // "VIDEO" | "PHOTO" | "CAROUSEL" | "TEXT"
        String title,
        String description,

        List<String> distributionPlatforms, // e.g. ["LINKEDIN","INSTAGRAM"]
        String duration,                    // e.g. "2:30" optional

        String targetAudience,              // optional
        List<String> scriptSteps,           // optional
        List<String> productionNotes,       // optional
        String callToAction                 // optional
) {
}