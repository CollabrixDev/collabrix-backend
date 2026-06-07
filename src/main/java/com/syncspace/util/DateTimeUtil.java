package com.syncspace.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

/**
 * Utility component for date/time operations.
 * 
 * Centralizes:
 * - Date formatting
 * - Timezone handling
 * - Duration calculations
 * - Relative time formatting
 * 
 * @author Prajeeth
 * @version 1.0.0
 */
@Component
public class DateTimeUtil {
    
    private static final DateTimeFormatter ISO_FORMATTER = 
        DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    private static final DateTimeFormatter READABLE_FORMATTER = 
        DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");
    
    /**
     * Get current timestamp.
     */
    public LocalDateTime now() {
        return LocalDateTime.now(DEFAULT_ZONE);
    }
    
    /**
     * Format datetime to ISO standard.
     */
    public String toIsoString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(ISO_FORMATTER);
    }
    
    /**
     * Format datetime to readable format.
     */
    public String toReadableString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(READABLE_FORMATTER);
    }
    
    /**
     * Parse ISO datetime string.
     */
    public LocalDateTime parseIsoString(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, ISO_FORMATTER);
    }
    
    /**
     * Calculate duration between two times in seconds.
     */
    public long getDurationInSeconds(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.SECONDS.between(start, end);
    }
    
    /**
     * Calculate duration between two times in minutes.
     */
    public long getDurationInMinutes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.MINUTES.between(start, end);
    }
    
    /**
     * Check if datetime is in the past.
     */
    public boolean isPast(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isBefore(now());
    }
    
    /**
     * Check if datetime is in the future.
     */
    public boolean isFuture(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isAfter(now());
    }
    
    /**
     * Get relative time string (e.g., "2 hours ago", "in 3 days").
     */
    public String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Unknown";
        }
        
        LocalDateTime now = now();
        
        if (dateTime.isBefore(now)) {
            long seconds = getDurationInSeconds(dateTime, now);
            return formatDurationAgo(seconds);
        } else {
            long seconds = getDurationInSeconds(now, dateTime);
            return formatDurationIn(seconds);
        }
    }
    
    private String formatDurationAgo(long totalSeconds) {
        if (totalSeconds < 60) {
            return "just now";
        }
        
        long minutes = totalSeconds / 60;
        if (minutes < 60) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        }
        
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        }
        
        long days = hours / 24;
        return days + " day" + (days > 1 ? "s" : "") + " ago";
    }
    
    private String formatDurationIn(long totalSeconds) {
        if (totalSeconds < 60) {
            return "in a moment";
        }
        
        long minutes = totalSeconds / 60;
        if (minutes < 60) {
            return "in " + minutes + " minute" + (minutes > 1 ? "s" : "");
        }
        
        long hours = minutes / 60;
        if (hours < 24) {
            return "in " + hours + " hour" + (hours > 1 ? "s" : "");
        }
        
        long days = hours / 24;
        return "in " + days + " day" + (days > 1 ? "s" : "");
    }
}
