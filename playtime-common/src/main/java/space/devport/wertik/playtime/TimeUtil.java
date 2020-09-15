package space.devport.wertik.playtime;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TimeUtil {

    public int takeElement(long millis, TimeElement element, boolean startingElement) {
        long seconds = millis / 1000;

        int value = 0;
        for (TimeElement loopElement : TimeElement.values()) {

            if (startingElement && loopElement.getSeconds() > element.getSeconds()) continue;

            value = (int) (seconds / loopElement.getSeconds());
            seconds = seconds % loopElement.getSeconds();

            if (loopElement == element)
                return value;
        }
        return value;
    }
}