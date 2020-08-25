package space.devport.wertik.playtime.spigot;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import space.devport.utils.text.StringUtil;
import space.devport.wertik.playtime.struct.User;

public class PlayTimeExpansion extends PlaceholderExpansion {

    private final PlayTimePlugin plugin = PlayTimePlugin.getInstance();

    /*
     * %playtime% -- time spent on server in millis
     * %playtime_formatted% -- time spent on server formatted
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

        if (args.length == 0) {
            User user = plugin.getLocalUserManager().getOrCreateUser(player.getUniqueId());
            return String.valueOf(user.getPlayedTime());
        } else {
            //TODO
            switch (args[0].toLowerCase()) {
                case "formatted":
                    User user = plugin.getLocalUserManager().getOrCreateUser(player.getUniqueId());
                    return StringUtil.color(DurationFormatUtils.formatDuration(user.getPlayedTime(), plugin.getDurationFormat()));
            }
        }
        return "invalid_params";
    }
}