package space.devport.wertik.playtime;

import lombok.Getter;

public enum TimeElement {

    MONTH(2419200),
    WEEK(604800),
    DAY(86400),
    HOUR(3600),
    MINUTE(60),
    SECOND(1);

    @Getter
    private final int seconds;

    TimeElement(int seconds) {
        this.seconds = seconds;
    }

    public static TimeElement fromString(String str) {
        try {
            return TimeElement.valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
