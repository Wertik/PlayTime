package space.devport.wertik.playtime;

import lombok.experimental.UtilityClass;
import space.devport.wertik.playtime.console.AbstractConsoleOutput;

@UtilityClass
public class TimeUtil {

    public int takeElement(long millis, TimeElement element, boolean startingElement) {

        long seconds = millis / 1000;
        AbstractConsoleOutput.getImplementation().debug("Input seconds: " + seconds);

        int val = 0;
        for (TimeElement loopElement : TimeElement.values()) {

            if (startingElement && loopElement.getSeconds() > element.getSeconds()) continue;

            val = (int) (seconds / loopElement.getSeconds());
            seconds = seconds % loopElement.getSeconds();
            AbstractConsoleOutput.getImplementation().debug("Loop: " + loopElement.toString() + " - " + val + " - " + seconds + " - " + element.toString());
            if (loopElement == element) return val;
        }
        return val;
    }
}