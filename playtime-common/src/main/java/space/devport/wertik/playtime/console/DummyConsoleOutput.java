package space.devport.wertik.playtime.console;

/**
 * Fall here if no console output is defined.
 */
public class DummyConsoleOutput extends AbstractConsoleOutput {

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