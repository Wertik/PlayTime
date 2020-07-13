package me.glaremasters.playertime.commands;

import me.glaremasters.playertime.PlayerTime;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import static me.glaremasters.playertime.utils.ColorUtil.color;

/**
 * Created by GlareMasters
 * Date: 7/21/2018
 * Time: 2:18 PM
 */
public class CMDReload implements CommandExecutor {

    private final PlayerTime playerTime;

    public CMDReload(PlayerTime playerTime) {
        this.playerTime = playerTime;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!sender.hasPermission("playertime.reload")) return true;

        playerTime.reloadConfig();
        sender.sendMessage(color(playerTime.getConfig().getString("messages.config-reload")));
        return true;
    }
}