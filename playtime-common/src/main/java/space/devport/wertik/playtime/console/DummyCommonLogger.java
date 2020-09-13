package space.devport.wertik.playtime.console;

/**
 * Fall here if no logger is implemented.
 */
public class DummyCommonLogger extends CommonLogger {

    @Override
    public void err(String msg) {
        System.out.print("ERROR: " + msg);
    }

    @Override
    public void warn(String msg) {
        System.out.print("ERROR: " + msg);
    }

    @Override
    public void info(String msg) {
        System.out.print("ERROR: " + msg);
    }

    @Override
    public void debug(String msg) {
        if (isDebug())
            System.out.print("ERROR: " + msg);
    }
}