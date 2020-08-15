package space.devport.wertik.playtime.console;

import lombok.Getter;
import lombok.Setter;

public abstract class AbstractConsoleOutput {

    private static AbstractConsoleOutput implementation;

    public static void setImplementation(AbstractConsoleOutput implementation) {
        AbstractConsoleOutput.implementation = implementation;
    }

    public static AbstractConsoleOutput getImplementation() {
        if (implementation == null)
            setImplementation(new DummyConsoleOutput());
        return implementation;
    }

    @Getter
    @Setter
    private boolean debug = false;

    public abstract void err(String msg);

    public abstract void warn(String msg);

    public abstract void info(String msg);

    public abstract void debug(String msg);
}