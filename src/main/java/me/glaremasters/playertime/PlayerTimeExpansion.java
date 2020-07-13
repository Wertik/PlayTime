package me.glaremasters.playertime;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.entity.Player;

public class PlayerTimeExpansion extends PlaceholderExpansion {

    private final PlayerTime playerTime = PlayerTime.getInstance();

    @Override
    public String getIdentifier() {
        return "playertime";
    }

    @Override
    public String getAuthor() {
        return String.join(", ", playerTime.getDescription().getAuthors());
    }

    @Override
    public String getVersion() {
        return playerTime.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {

        String[] args = params.split("_");

        if (args.length < 1)
            return "not_enough_args";

        if (args[0].equalsIgnoreCase("time"))
            return DurationFormatUtils.formatDuration(Long.parseLong(playerTime.getDatabase().getTime(player.getUniqueId().toString())),
                    playerTime.getConfig().getString("placeholder-format", "hh:mm:ss"));

        return "invalid_param";
    }
}
