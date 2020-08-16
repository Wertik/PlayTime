package space.devport.wertik.playtime.spigot.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import space.devport.utils.commands.SubCommand;
import space.devport.utils.commands.struct.ArgumentRange;
import space.devport.utils.commands.struct.CommandResult;
import space.devport.utils.commands.struct.Preconditions;
import space.devport.wertik.playtime.spigot.PlayTimePlugin;
import space.devport.wertik.playtime.struct.User;

public class ResetSubCommand extends SubCommand {

    public ResetSubCommand() {
        super("reset");
        this.preconditions = new Preconditions()
                .permissions("playtime.reset");
    }

    @Override
    protected CommandResult perform(CommandSender sender, String label, String[] args) {

        boolean others = false;

        OfflinePlayer target;
        if (args.length > 0) {
            target = Bukkit.getOfflinePlayer(args[0]);

            if (!sender.hasPermission("playtime.reset.others")) return CommandResult.NO_PERMISSION;

            others = true;
        } else {
            if (!(sender instanceof Player)) return CommandResult.NO_CONSOLE;

            target = (Player) sender;
        }

        User user = PlayTimePlugin.getInstance().getLocalUserManager().getUser(target.getUniqueId());
        if (user == null) {
            language.getPrefixed("Commands.No-Record")
                    .replace("%player%", target.getName())
                    .send(sender);
            return CommandResult.FAILURE;
        }

        PlayTimePlugin.getInstance().getLocalUserManager().deleteUser(user.getUniqueID());

        if (PlayTimePlugin.getInstance().getConfig().getBoolean("import-statistics", false))
            language.sendPrefixed(sender, "Commands.Reset.Import-Warning");

        language.getPrefixed(others ? "Commands.Reset.Done-Others" : "Commands.Reset.Done")
                .replace("%player%", target.getName())
                .send(sender);
        return CommandResult.SUCCESS;
    }

    @Override
    public @NotNull String getDefaultUsage() {
        return "/%label% reset (player)";
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Reload the plugin.";
    }

    @Override
    public @NotNull ArgumentRange getRange() {
        return new ArgumentRange(0);
    }
}
