package fr.iban.msboosts.util;

import java.time.Duration;

public class TimeFormatter {

    public static String formatTime(long milliseconds) {
        Duration duration = Duration.ofMillis(milliseconds);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        String formattedTime;
        if (hours > 0) {
            formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            formattedTime = String.format("%02d:%02d", minutes, seconds);
        }

        return formattedTime;
    }

}
