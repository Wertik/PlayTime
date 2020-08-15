package space.devport.wertik.playtime.spigot;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayTimeExpansion extends PlaceholderExpansion {

    private final PlayTimePlugin plugin = PlayTimePlugin.getInstance();

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

        if (args.length < 1)
            return "not_enough_args";

        //TODO
        return "not_done_yet";
    }
}
