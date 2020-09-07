package space.devport.wertik.playtime.spigot.commands;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import space.devport.utils.commands.SubCommand;
import space.devport.utils.commands.struct.ArgumentRange;
import space.devport.wertik.playtime.spigot.PlayTimePlugin;

public abstract class PlayTimeSubCommand extends SubCommand {

    @Getter
    private final PlayTimePlugin plugin;

    public PlayTimeSubCommand(String name, PlayTimePlugin plugin) {
        super(name);
        setPermissions();
        this.plugin = plugin;
    }

    @Override
    public abstract @Nullable String getDefaultUsage();

    @Override
    public abstract @Nullable String getDefaultDescription();

    @Override
    public abstract @Nullable ArgumentRange getRange();
}