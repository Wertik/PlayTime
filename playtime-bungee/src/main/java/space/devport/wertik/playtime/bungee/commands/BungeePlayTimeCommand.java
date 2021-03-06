package space.devport.wertik.playtime.bungee.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.Nullable;
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
            sender.sendMessage(BungeeStringUtil.format("&cSub commands: &fcheck (player)&7, &freload (hard)&7, &fglobalcheck (player)"));
            return;
        }

        ProxiedPlayer player;
        switch (args[0].toLowerCase()) {
            case "reload":
                if (args.length > 1 && args[1].equalsIgnoreCase("hard"))
                    plugin.hardReload(sender);
                else
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
                    user = plugin.getLocalUserManager().getOrLoadUser(player.getUniqueId()).join();
                } else
                    user = plugin.getLocalUserManager().getUser(args[1]);

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

                    if (globalUser == null) {
                        sender.sendMessage(BungeeStringUtil.format("&7&oLoading the user..."));
                        plugin.getGlobalUserManager().loadGlobalUser(player.getUniqueId())
                                .thenRunAsync(() -> {
                                    GlobalUser loadedUser = plugin.getGlobalUserManager().getGlobalUser(player.getUniqueId());
                                    printUserInfo(sender, loadedUser);
                                });
                        return;
                    }
                } else {
                    globalUser = plugin.getGlobalUserManager().getGlobalUser(args[1]);

                    if (globalUser == null) {
                        sender.sendMessage(BungeeStringUtil.format("&7&oLoading the user..."));
                        plugin.getGlobalUserManager().loadGlobalUser(args[1])
                                .thenRunAsync(() -> {
                                    GlobalUser loadedUser = plugin.getGlobalUserManager().getGlobalUser(args[1]);
                                    printUserInfo(sender, loadedUser);
                                });
                        return;
                    }
                }

                printUserInfo(sender, globalUser);
                break;
            default:
                sender.sendMessage(BungeeStringUtil.format("&cInvalid sub command."));
        }
    }

    private void printUserInfo(CommandSender sender, @Nullable GlobalUser user) {

        if (user == null) {
            sender.sendMessage(BungeeStringUtil.format("&cPlayer has no record over all connected servers."));
            return;
        }

        StringBuilder message = new StringBuilder("&8&m    &3 Global Play Times");
        for (ServerInfo serverInfo : user.getUserRecord().keySet()) {
            message.append("\n&8 - &f%serverName% &8= &r%time%"
                    .replace("%serverName%", serverInfo.getName())
                    .replace("%time%", DurationFormatUtils.formatDuration(user.getPlayedTime(serverInfo), plugin.getDurationFormat())));
        }
        sender.sendMessage(BungeeStringUtil.format(message.toString()));
    }
}