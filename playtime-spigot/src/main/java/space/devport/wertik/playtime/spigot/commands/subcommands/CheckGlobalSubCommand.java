package space.devport.wertik.playtime.spigot.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import space.devport.utils.commands.SubCommand;
import space.devport.utils.commands.struct.ArgumentRange;
import space.devport.utils.commands.struct.CommandResult;
import space.devport.utils.commands.struct.Preconditions;

public class CheckGlobalSubCommand extends SubCommand {

    public CheckGlobalSubCommand() {
        super("checkglobal");
        this.preconditions = new Preconditions()
                .permissions("playtime.checkglobal");
    }

    @Override
    protected CommandResult perform(CommandSender sender, String label, String[] args) {

        sender.sendMessage("&cNot implemented yet.");

        /*
        OfflinePlayer target;
        if (args.length > 0) {
            target = CommandUtils.getOfflineTarget(sender, args[0]);

            if (target == null) return CommandResult.FAILURE;

            if (!sender.hasPermission("playtime.checkglobal.others")) return CommandResult.NO_PERMISSION;
        } else {
            if (!(sender instanceof Player)) return CommandResult.NO_CONSOLE;

            target = (Player) sender;
        }

        User user = PlayTimePlugin.getInstance().getLocalUserManager().getUser(target.getUniqueId());
        if (user == null) {
            //TODO err msg
            return CommandResult.FAILURE;
        }

        //TODO msg*/
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