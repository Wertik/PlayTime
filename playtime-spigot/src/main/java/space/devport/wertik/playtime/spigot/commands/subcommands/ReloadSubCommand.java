package space.devport.wertik.playtime.spigot.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import space.devport.utils.commands.struct.ArgumentRange;
import space.devport.utils.commands.struct.CommandResult;
import space.devport.wertik.playtime.spigot.PlayTimePlugin;
import space.devport.wertik.playtime.spigot.commands.PlayTimeSubCommand;

public class ReloadSubCommand extends PlayTimeSubCommand {

    public ReloadSubCommand(PlayTimePlugin plugin) {
        super("reload", plugin);
    }

    @Override
    protected CommandResult perform(CommandSender sender, String label, String[] args) {

        if (args.length > 0 && args[0].equalsIgnoreCase("hard")) {
            getPlugin().hardReload(sender);
            return CommandResult.SUCCESS;
        }

        getPlugin().reload(sender);
        return CommandResult.SUCCESS;
    }

    @Override
    public @NotNull String getDefaultUsage() {
        return "/%label% reload (hard)";
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Reload the plugin. Add 'hard' to call a hard reload. (Reload data and connections)";
    }

    @Override
    public @NotNull ArgumentRange getRange() {
        return new ArgumentRange(0, 1);
    }
}