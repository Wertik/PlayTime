package space.devport.wertik.playtime.spigot;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import space.devport.utils.text.StringUtil;
import space.devport.wertik.playtime.TimeElement;
import space.devport.wertik.playtime.TimeUtil;
import space.devport.wertik.playtime.struct.GlobalUser;
import space.devport.wertik.playtime.struct.ServerInfo;
import space.devport.wertik.playtime.struct.User;

@RequiredArgsConstructor
public class PlayTimeExpansion extends PlaceholderExpansion {

    private final PlayTimePlugin plugin;

    /*
     * %playtime% -- time spent on server in millis
     * %playtime_formatted% -- time spent on server formatted
     * %playtime_global_<server>_formatted/time-element%
     * */

    @Override
    public @NotNull String getIdentifier() {
        return "playtime";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {

        String[] args = params.split("_");

        if (player == null) return "no_player";

        User user = plugin.getLocalUserManager().getOrCreateUser(player.getUniqueId());

        if (args.length == 0) {
            return String.valueOf(user.getPlayedTime());
        }

        if (args[0].equalsIgnoreCase("global")) {
            GlobalUser globalUser = plugin.getGlobalUserManager().getGlobalUser(player.getUniqueId());

            if (args.length < 2) return "not_enough_args";

            String serverName = args[1];

            if (!plugin.getGlobalUserManager().getRemoteStorages().containsKey(serverName))
                return "invalid_server";

            if (args.length == 2) {
                return String.valueOf(globalUser.getPlayedTime(new ServerInfo(serverName)));
            }

            return parseTime(globalUser.getPlayedTime(new ServerInfo(serverName)), args[2], args.length > 3 && args[3].equalsIgnoreCase("start"));
        }

        return parseTime(user.getPlayedTimeRaw(), args[0], args.length > 1 && args[0].equalsIgnoreCase("start"));
    }

    private String parseTime(long time, String param, boolean starting) {
        TimeElement timeElement = TimeElement.fromString(param);

        if (timeElement != null) {
            int val = TimeUtil.takeElement(time, timeElement, starting);
            return String.valueOf(val);
        }

        if (param.equalsIgnoreCase("formatted")) {
            return StringUtil.color(DurationFormatUtils.formatDuration(time, plugin.getDurationFormat()));
        }
        return "invalid_params";
    }
}