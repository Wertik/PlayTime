package space.devport.wertik.playtime.spigot.commands;

import org.bukkit.command.CommandSender;
import space.devport.utils.commands.MainCommand;
import space.devport.utils.commands.struct.CommandResult;

public class PlayTimeCommand extends MainCommand {

    public PlayTimeCommand(String name) {
        super(name);
    }

    @Override
    protected CommandResult perform(CommandSender sender, String label, String[] args) {
        return super.perform(sender, label, args);
    }

    @Override
    public String getDefaultUsage() {
        return null;
    }

    @Override
    public String getDefaultDescription() {
        return null;
    }
}