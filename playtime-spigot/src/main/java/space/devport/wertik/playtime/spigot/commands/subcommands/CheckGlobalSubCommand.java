package space.devport.wertik.playtime.spigot.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import space.devport.utils.commands.SubCommand;
import space.devport.utils.commands.struct.ArgumentRange;
import space.devport.utils.commands.struct.CommandResult;

public class CheckGlobalSubCommand extends SubCommand {

    public CheckGlobalSubCommand() {
        super("checkglobal");
    }

    @Override
    protected CommandResult perform(CommandSender sender, String label, String[] args) {
        return CommandResult.SUCCESS;
    }

    @Override
    public @NotNull String getDefaultUsage() {
        return "/%label% checkglobal <server> (player)";
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Check the play time of a player on a specified server.";
    }

    @Override
    public @NotNull ArgumentRange getRange() {
        return new ArgumentRange(1, 2);
    }
}