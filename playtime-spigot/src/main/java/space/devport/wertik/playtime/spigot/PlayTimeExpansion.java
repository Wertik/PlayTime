package space.devport.wertik.playtime.spigot;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import space.devport.utils.text.StringUtil;
import space.devport.wertik.playtime.TimeElement;
import space.devport.wertik.playtime.TimeUtil;
import space.devport.wertik.playtime.struct.User;

@RequiredArgsConstructor
public class PlayTimeExpansion extends PlaceholderExpansion {

    private final PlayTimePlugin plugin;

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

        if (player == null) return "no_player";

        User user = plugin.getLocalUserManager().getOrCreateUser(player.getUniqueId());

        if (args.length == 0) {
            return String.valueOf(user.getPlayedTime());
        } else {

            TimeElement timeElement = TimeElement.fromString(args[0]);

            if (timeElement != null) {
                int val = TimeUtil.takeElement(user.getPlayedTime(), timeElement, args.length > 1 && args[1].equalsIgnoreCase("start"));
                return String.valueOf(val);
            }

            //TODO
            switch (args[0].toLowerCase()) {
                case "formatted":
                    return StringUtil.color(DurationFormatUtils.formatDuration(user.getPlayedTime(), plugin.getDurationFormat()));
            }
        }
        return "invalid_params";
    }
}