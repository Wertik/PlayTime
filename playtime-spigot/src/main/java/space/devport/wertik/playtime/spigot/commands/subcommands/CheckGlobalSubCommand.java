package space.devport.wertik.playtime.spigot.commands.subcommands;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import space.devport.utils.commands.struct.ArgumentRange;
import space.devport.utils.commands.struct.CommandResult;
import space.devport.utils.text.StringUtil;
import space.devport.wertik.playtime.spigot.PlayTimePlugin;
import space.devport.wertik.playtime.spigot.commands.PlayTimeSubCommand;
import space.devport.wertik.playtime.struct.GlobalUser;
import space.devport.wertik.playtime.struct.ServerInfo;

public class CheckGlobalSubCommand extends PlayTimeSubCommand {

    public CheckGlobalSubCommand(PlayTimePlugin plugin) {
        super("checkglobal", plugin);
    }

    @Override
    protected CommandResult perform(CommandSender sender, String label, String[] args) {

        OfflinePlayer target;
        if (args.length > 0) {
            target = Bukkit.getOfflinePlayer(args[0]);

            if (!sender.hasPermission("playtime.check.others")) return CommandResult.NO_PERMISSION;
        } else {
            if (!(sender instanceof Player)) return CommandResult.NO_CONSOLE;

            target = (Player) sender;
        }

        //TODO Beautify
        GlobalUser globalUser = getPlugin().getGlobalUserManager().getGlobalUser(target.getUniqueId());
        sender.sendMessage("Global user " + globalUser.getUniqueID() + ", name: " + Bukkit.getOfflinePlayer(globalUser.getUniqueID()).getName());
        for (ServerInfo serverInfo : globalUser.getUserRecord().keySet()) {
            sender.sendMessage(StringUtil.color("Time on " + serverInfo.getName() + " = " + DurationFormatUtils.formatDuration(globalUser.getPlayedTime(serverInfo), getPlugin().getDurationFormat())));
        }
        sender.sendMessage("Simplistic implementation.");
        return CommandResult.SUCCESS;
    }

    @Override
    public @NotNull String getDefaultUsage() {
        return "/%label% checkglobal (player)";
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Check the play time of a player on a specified server.";
    }

    @Override
    public @NotNull ArgumentRange getRange() {
        return new ArgumentRange(0, 1);
    }
}