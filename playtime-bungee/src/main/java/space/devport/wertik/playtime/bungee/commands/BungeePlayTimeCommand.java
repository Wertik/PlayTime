package space.devport.wertik.playtime.bungee.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.apache.commons.lang3.time.DurationFormatUtils;
import space.devport.wertik.playtime.bungee.BungeePlayTimePlugin;
import space.devport.wertik.playtime.bungee.utils.BungeeStringUtil;
import space.devport.wertik.playtime.struct.GlobalUser;
import space.devport.wertik.playtime.struct.ServerInfo;
import space.devport.wertik.playtime.struct.User;

public class BungeePlayTimeCommand extends Command {

    private final BungeePlayTimePlugin plugin;

    public BungeePlayTimeCommand(BungeePlayTimePlugin plugin) {
        super("bungeeplaytime", "playtime.bungee", "bpt");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(BungeeStringUtil.format("&cSub commands: &fcheck (player)&7, &freload&7, &fglobalcheck (player)"));
            return;
        }

        ProxiedPlayer player;
        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.reload(sender);
                break;
            case "check":
                User user;

                if (args.length <= 1) {
                    if (!(sender instanceof ProxiedPlayer)) {
                        sender.sendMessage(BungeeStringUtil.format("&cYou're not a player!"));
                        return;
                    }

                    player = (ProxiedPlayer) sender;
                    user = plugin.getLocalUserManager().getUser(player.getUniqueId());
                } else {
                    user = plugin.getLocalUserManager().getUser(args[1]);
                }

                if (user == null) {
                    sender.sendMessage(BungeeStringUtil.format("&cPlayer has no record."));
                    return;
                }

                sender.sendMessage(BungeeStringUtil.format("&7Play time: &f" + DurationFormatUtils.formatDuration(user.getPlayedTime(), plugin.getDurationFormat())));
                break;
            case "checkglobal":

                if (plugin.getGlobalUserManager().getRemoteStorages().isEmpty()) {
                    sender.sendMessage(BungeeStringUtil.format("&cNo remote servers connected."));
                    return;
                }

                GlobalUser globalUser;
                if (args.length <= 1) {
                    if (!(sender instanceof ProxiedPlayer)) {
                        sender.sendMessage(BungeeStringUtil.format("&cYou're not a player!"));
                        return;
                    }

                    player = (ProxiedPlayer) sender;
                    globalUser = plugin.getGlobalUserManager().getGlobalUser(player.getUniqueId());
                } else {
                    globalUser = plugin.getGlobalUserManager().getGlobalUser(args[1]);
                }

                if (globalUser == null) {
                    sender.sendMessage(BungeeStringUtil.format("&cPlayer has no record over all connected servers."));
                    return;
                }

                StringBuilder message = new StringBuilder("&8&m    &3 Global Play Times");
                for (ServerInfo serverInfo : globalUser.getUserRecord().keySet()) {
                    message.append("\n&8 - &f%serverName% &8= &r%time%"
                            .replace("%serverName%", serverInfo.getName())
                            .replace("%time%", DurationFormatUtils.formatDuration(globalUser.getPlayedTime(serverInfo), plugin.getDurationFormat())));
                }
                sender.sendMessage(BungeeStringUtil.format(message.toString()));
                break;
            default:
                sender.sendMessage(BungeeStringUtil.format("&cInvalid sub command."));
        }
    }
}