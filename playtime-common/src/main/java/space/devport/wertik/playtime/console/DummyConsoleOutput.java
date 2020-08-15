package space.devport.wertik.playtime.console;

/**
 * Fall here if no console output is defined.
 */
public class DummyConsoleOutput extends AbstractConsoleOutput {

    @Override
    public void err(String msg) {
    }

    @Override
    public void warn(String msg) {
    }

    @Override
    public void info(String msg) {
    }

    @Override
    public void debug(String msg) {
    }
}