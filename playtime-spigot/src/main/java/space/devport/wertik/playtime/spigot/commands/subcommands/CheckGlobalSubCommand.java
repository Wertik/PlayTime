package space.devport.wertik.playtime.spigot.commands.subcommands;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import space.devport.utils.commands.struct.ArgumentRange;
import space.devport.utils.commands.struct.CommandResult;
import space.devport.utils.text.message.Message;
import space.devport.wertik.playtime.spigot.PlayTimePlugin;
import space.devport.wertik.playtime.spigot.commands.PlayTimeSubCommand;
import space.devport.wertik.playtime.struct.GlobalUser;
import space.devport.wertik.playtime.struct.ServerInfo;
import space.devport.wertik.playtime.system.DataManager;

public class CheckGlobalSubCommand extends PlayTimeSubCommand {

    public CheckGlobalSubCommand(PlayTimePlugin plugin) {
        super("checkglobal", plugin);
    }

    @Override
    protected CommandResult perform(CommandSender sender, String label, String[] args) {

        if (DataManager.getInstance().getGlobalUserManager() == null || DataManager.getInstance().getGlobalUserManager().getRemoteStorages().isEmpty()) {
            language.sendPrefixed(sender, "Commands.Global-Check.No-Servers");
            return CommandResult.FAILURE;
        }

        OfflinePlayer target;
        if (args.length > 0) {
            target = Bukkit.getOfflinePlayer(args[0]);

            if (!sender.hasPermission("playtime.check.others")) return CommandResult.NO_PERMISSION;
        } else {
            if (!(sender instanceof Player)) return CommandResult.NO_CONSOLE;

            target = (Player) sender;
        }

        GlobalUser globalUser = getPlugin().getGlobalUserManager().getGlobalUser(target.getUniqueId());

        Message message = language.get("Commands.Global-Check.Header");
        Message lineFormat = language.get("Commands.Global-Check.Line");

        for (ServerInfo serverInfo : globalUser.getUserRecord().keySet()) {
            message.append(lineFormat.toString()
                    .replace("%serverName%", serverInfo.getName())
                    .replace("%time%", DurationFormatUtils.formatDuration(globalUser.getPlayedTime(serverInfo), getPlugin().getDurationFormat())));
        }

        message.send(sender);
        return CommandResult.SUCCESS;
    }

    @Override
    public @NotNull String getDefaultUsage() {
        return "/%label% checkglobal (player)";
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Check the play time of a player on all connected remote servers.";
    }

    @Override
    public @NotNull ArgumentRange getRange() {
        return new ArgumentRange(0, 1);
    }
}