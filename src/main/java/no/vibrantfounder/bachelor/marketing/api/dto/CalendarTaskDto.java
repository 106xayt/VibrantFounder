package no.vibrantfounder.bachelor.marketing.api.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Calendar task for the plan execution UI.
 *
 * NOTE: date must be ISO-8601 "YYYY-MM-DD" in the AI JSON for LocalDate to parse.
 */
public record CalendarTaskDto(
        String id,                    // uuid string recommended
        LocalDate date,               // REQUIRED
        String time,                  // "09:00" optional
        String platform,              // "LINKEDIN", ...
        String label,                 // "LinkedIn Post"
        String type,                  // "POST" | "VIDEO" | "REELS" etc

        String priority,              // "LOW" | "MEDIUM" | "HIGH"
        List<String> stepByStepGuide, // optional
        String proTip                 // optional
) {
}