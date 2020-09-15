package space.devport.wertik.playtime.console;

import lombok.Getter;
import lombok.Setter;

public abstract class CommonLogger {

    @Setter
    private static CommonLogger implementation;

    @Getter
    @Setter
    private boolean debug = false;

    public static CommonLogger getImplementation() {
        if (implementation == null)
            setImplementation(new DummyCommonLogger());
        return implementation;
    }

    public abstract void err(String msg);

    public abstract void warn(String msg);

    public abstract void info(String msg);

    public abstract void debug(String msg);
}