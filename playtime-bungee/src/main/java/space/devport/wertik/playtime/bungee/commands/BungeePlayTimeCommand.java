package space.devport.wertik.playtime.bungee.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.apache.commons.lang.time.DurationFormatUtils;
import space.devport.wertik.playtime.bungee.BungeePlayTimePlugin;
import space.devport.wertik.playtime.struct.User;

public class BungeePlayTimeCommand extends Command {

    private final BungeePlayTimePlugin plugin;

    public BungeePlayTimeCommand(BungeePlayTimePlugin plugin) {
        super("playtime");
        this.plugin = plugin;
    }

    /*
     * TODO: Beatify commands, use language.yml
     * */

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(new TextComponent("&cSub commands: &fcheck (player)&7, &freload"));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.reload(sender);
                break;
            case "check":
                ProxiedPlayer player;
                if (args.length > 1) {
                    player = ProxyServer.getInstance().getPlayer(args[1]);

                    if (player == null) {
                        sender.sendMessage(new TextComponent("&cThat player does not exist."));
                        return;
                    }
                } else {
                    if (!(sender instanceof ProxiedPlayer)) {
                        sender.sendMessage(new TextComponent("&cYou're not a player!"));
                        return;
                    }

                    player = (ProxiedPlayer) sender;
                }

                User user = plugin.getLocalUserManager().getUser(player.getUniqueId());

                if (user == null) {
                    sender.sendMessage(new TextComponent("&cPlayer has no record."));
                    return;
                }

                sender.sendMessage(new TextComponent("&7Play time: &f" + DurationFormatUtils.formatDuration(user.getPlayedTime(), plugin.getDurationFormat())));
                break;
            default:
                sender.sendMessage(new TextComponent("&cInvalid sub command."));
        }
    }
}