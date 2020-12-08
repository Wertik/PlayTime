package space.devport.wertik.playtime.spigot;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import space.devport.utils.text.StringUtil;
import space.devport.wertik.playtime.TimeElement;
import space.devport.wertik.playtime.TimeUtil;
import space.devport.wertik.playtime.console.CommonLogger;
import space.devport.wertik.playtime.struct.GlobalUser;
import space.devport.wertik.playtime.struct.ServerInfo;
import space.devport.wertik.playtime.struct.User;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@RequiredArgsConstructor
public class PlayTimeExpansion extends PlaceholderExpansion {

    private final PlayTimePlugin plugin;

    /*
     * %playtime% -- time spent on server in millis
     * %playtime_<formatted/time-element>% -- time spent on server formatted
     * %playtime_global_<server>_<formatted/time-element>%
     * %playtime_top_<position>_<name/time-element/formatted>%
     * %playtime_top_global_<server>_<position>_<name/time-element/formatted>%
     * */

    @Override
    public @NotNull
    String getIdentifier() {
        return "playtime";
    }

    @Override
    public @NotNull
    String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull
    String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {

        String[] args = params.split("_");

        if (player == null) return "no_player";

        CompletableFuture<User> future = plugin.getLocalUserManager().getOrCreateUser(player.getUniqueId());

        if (args.length == 0) {

            User user = attemptFetch(future);

            if (user == null)
                return "loading";

            return String.valueOf(user.getPlayedTime());
        }

        if (args[0].equalsIgnoreCase("global")) {

            if (args.length < 2)
                return "not_enough_args";

            String serverName = args[1];

            if (!plugin.getGlobalUserManager().hasRemote(serverName))
                return "invalid_server";

            GlobalUser globalUser = plugin.getGlobalUserManager().getGlobalUser(player.getUniqueId());

            if (globalUser == null) {
                plugin.getGlobalUserManager().loadGlobalUser(player.getUniqueId());
                return "loading";
            }

            ServerInfo serverInfo = plugin.getGlobalUserManager().getServerInfo(serverName);

            if (args.length == 2)
                return String.valueOf(globalUser.getPlayedTime(serverInfo));

            return parseTime(globalUser.getPlayedTime(serverInfo), args[2], args.length > 3 && args[3].equalsIgnoreCase("start"));
        } else if (args[0].equalsIgnoreCase("top")) {

            if (args[1].equalsIgnoreCase("global")) {

                String serverName = args[2];

                if (!plugin.getGlobalUserManager().hasRemote(serverName))
                    return "invalid_server";

                int position = parsePosition(args[3]);

                if (position == -1) return "invalid_position";

                ServerInfo info = plugin.getGlobalUserManager().getServerInfo(serverName);

                User topUser = plugin.getGlobalUserManager().getTopCache().get(info).getPosition(position);

                if (topUser == null)
                    return "not_populated";

                if (args.length == 4)
                    return String.valueOf(topUser.getPlayedTime());

                if (args[4].equalsIgnoreCase("name"))
                    return topUser.getLastKnownName();
                else
                    return parseTime(topUser.getPlayedTime(), args[4], args.length > 5 && args[5].equalsIgnoreCase("start"));
            }

            int position = parsePosition(args[1]);

            if (position == -1) return "invalid_position";

            User topUser = plugin.getLocalUserManager().getTopCache().getPosition(position);

            if (topUser == null)
                return "not_populated";

            if (args.length == 2)
                return String.valueOf(topUser.getPlayedTime());

            if (args[2].equalsIgnoreCase("name"))
                return topUser.getLastKnownName();
            else
                return parseTime(topUser.getPlayedTime(), args[2], args.length > 3 && args[3].equalsIgnoreCase("start"));
        }

        User user = attemptFetch(future);

        if (user == null)
            return "loading";

        return parseTime(user.getPlayedTime(), args[0], args.length > 1 && args[1].equalsIgnoreCase("start"));
    }

    private User attemptFetch(CompletableFuture<User> future) {
        if (!future.isDone())
            return null;

        try {
            return future.join();
        } catch (CompletionException | CancellationException e) {
            CommonLogger.getImplementation().warn("Could not load user for placeholders. Reason:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private int parsePosition(String str) {
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException exception) {
            return -1;
        }
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