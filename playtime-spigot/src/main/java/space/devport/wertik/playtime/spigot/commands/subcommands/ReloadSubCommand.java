package space.devport.wertik.playtime.spigot.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import space.devport.utils.commands.SubCommand;
import space.devport.utils.commands.struct.ArgumentRange;
import space.devport.utils.commands.struct.CommandResult;
import space.devport.utils.commands.struct.Preconditions;
import space.devport.wertik.playtime.spigot.PlayTimePlugin;

public class ReloadSubCommand extends SubCommand {

    public ReloadSubCommand() {
        super("reload");
        this.preconditions = new Preconditions()
                .permissions("playtime.reload");
    }

    @Override
    protected CommandResult perform(CommandSender sender, String label, String[] args) {
        PlayTimePlugin.getInstance().reload(sender);
        return CommandResult.SUCCESS;
    }

    @Override
    public @NotNull String getDefaultUsage() {
        return "/%label% reload";
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Reload the plugin.";
    }

    @Override
    public @NotNull ArgumentRange getRange() {
        return new ArgumentRange(0);
    }
}