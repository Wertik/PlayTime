package space.devport.wertik.playtime.spigot.commands.subcommands;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import space.devport.utils.commands.struct.ArgumentRange;
import space.devport.utils.commands.struct.CommandResult;
import space.devport.wertik.playtime.spigot.PlayTimePlugin;
import space.devport.wertik.playtime.spigot.commands.PlayTimeSubCommand;
import space.devport.wertik.playtime.struct.User;

public class CheckSubCommand extends PlayTimeSubCommand {

    public CheckSubCommand(PlayTimePlugin plugin) {
        super("check", plugin);
    }

    @Override
    protected CommandResult perform(CommandSender sender, String label, String[] args) {

        OfflinePlayer target;
        if (args.length > 0) {
            target = Bukkit.getOfflinePlayer(args[0]);

            if (!sender.hasPermission("playtime.check.others")) return CommandResult.NO_PERMISSION;
        } else {
            if (!(sender instanceof Player)) return CommandResult.NO_CONSOLE;

            target = (Player) sender;
        }

        User user = getPlugin().getLocalUserManager().getOrLoadUser(target.getUniqueId()).join();
        if (user == null) {
            language.getPrefixed("Commands.No-Record")
                    .replace("%player%", target.getName())
                    .send(sender);
            return CommandResult.FAILURE;
        }

        language.getPrefixed("Commands.Check")
                .replace("%time%", DurationFormatUtils.formatDuration(user.getPlayedTime(), getPlugin().getDurationFormat()))
                .send(sender);
        return CommandResult.SUCCESS;
    }

    @Override
    public @NotNull String getDefaultUsage() {
        return "/%label% check (player)";
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Check the play time of a player.";
    }

    @Override
    public @NotNull ArgumentRange getRange() {
        return new ArgumentRange(0, 1);
    }
}