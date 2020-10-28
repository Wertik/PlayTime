package space.devport.wertik.playtime.spigot.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import space.devport.utils.commands.struct.ArgumentRange;
import space.devport.utils.commands.struct.CommandResult;
import space.devport.wertik.playtime.spigot.PlayTimePlugin;
import space.devport.wertik.playtime.spigot.commands.PlayTimeSubCommand;

import java.util.concurrent.atomic.AtomicBoolean;

public class ResetSubCommand extends PlayTimeSubCommand {

    public ResetSubCommand(PlayTimePlugin plugin) {
        super("reset", plugin);
    }

    @Override
    protected CommandResult perform(CommandSender sender, String label, String[] args) {

        AtomicBoolean others = new AtomicBoolean(false);

        OfflinePlayer target;
        if (args.length > 0) {
            target = Bukkit.getOfflinePlayer(args[0]);

            if (!sender.hasPermission("playtime.reset.others")) return CommandResult.NO_PERMISSION;

            others.set(true);
        } else {
            if (!(sender instanceof Player)) return CommandResult.NO_CONSOLE;

            target = (Player) sender;
        }

        getPlugin().getLocalUserManager().getOrLoadUser(target.getUniqueId()).thenAccept(user -> {
            if (user == null) {
                language.getPrefixed("Commands.No-Record")
                        .replace("%player%", target.getName())
                        .send(sender);
                return;
            }

            getPlugin().getLocalUserManager().deleteUser(user.getUniqueID());

            if (getPlugin().getConfig().getBoolean("import-statistics", false))
                language.sendPrefixed(sender, "Commands.Reset.Import-Warning");

            language.getPrefixed(others.get() ? "Commands.Reset.Done-Others" : "Commands.Reset.Done")
                    .replace("%player%", target.getName())
                    .send(sender);
        });
        return CommandResult.SUCCESS;
    }

    @Override
    public @NotNull String getDefaultUsage() {
        return "/%label% reset (player)";
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Reset all times, or for a player.";
    }

    @Override
    public @NotNull ArgumentRange getRange() {
        return new ArgumentRange(0);
    }
}
