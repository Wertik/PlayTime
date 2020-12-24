package space.devport.wertik.playtime.spigot.commands.subcommands;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;
import space.devport.utils.commands.struct.ArgumentRange;
import space.devport.utils.commands.struct.CommandResult;
import space.devport.utils.text.message.Message;
import space.devport.wertik.playtime.spigot.PlayTimePlugin;
import space.devport.wertik.playtime.spigot.commands.PlayTimeSubCommand;
import space.devport.wertik.playtime.struct.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TopSubCommand extends PlayTimeSubCommand {

    public TopSubCommand(PlayTimePlugin plugin) {
        super("top", plugin);
    }

    @Override
    protected CommandResult perform(CommandSender sender, String label, String[] args) {

        int count = args.length > 0 ? parseInt(args[0]) : 10;

        CompletableFuture<List<User>> topFuture = getPlugin().getLocalUserManager().getTop(count);

        topFuture.thenAcceptAsync((top) -> {
            Message message = language.get("Commands.Top.Header");
            Message lineFormat = language.get("Commands.Top.Line");

            int n = 1;
            for (User user : top) {
                n++;
                message.append(lineFormat.toString()
                        .replace("%position%", String.valueOf(n))
                        .replace("%name%", user.getLastKnownName())
                        .replace("%time%", DurationFormatUtils.formatDuration(user.getPlayedTime(), getPlugin().getDurationFormat())));
            }
            message.send(sender);
        }).exceptionally((exc) -> {
            if (getPlugin().getConsoleOutput().isDebug())
                exc.printStackTrace();
            language.sendPrefixed(sender, "Commands.Top.Failed");
            return null;
        });
        return CommandResult.SUCCESS;
    }

    private int parseInt(String arg) {
        try {
            return Integer.parseInt(arg.trim());
        } catch (NumberFormatException e) {
            return 10;
        }
    }

    @Override
    public @Nullable String getDefaultUsage() {
        return "/%label% top (count)";
    }

    @Override
    public @Nullable String getDefaultDescription() {
        return "Get top x players.";
    }

    @Override
    public @Nullable ArgumentRange getRange() {
        return new ArgumentRange(0, 1);
    }
}
