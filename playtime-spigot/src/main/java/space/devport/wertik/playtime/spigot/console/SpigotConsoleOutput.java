package space.devport.wertik.playtime.spigot.console;

import lombok.RequiredArgsConstructor;
import space.devport.utils.ConsoleOutput;
import space.devport.wertik.playtime.console.AbstractConsoleOutput;

@RequiredArgsConstructor
public class SpigotConsoleOutput extends AbstractConsoleOutput {

    private final ConsoleOutput consoleOutput;

    @Override
    public void err(String msg) {
        consoleOutput.err(msg);
    }

    @Override
    public void warn(String msg) {
        consoleOutput.warn(msg);
    }

    @Override
    public void info(String msg) {
        consoleOutput.info(msg);
    }

    @Override
    public void debug(String msg) {
        consoleOutput.debug(msg);
    }
}
