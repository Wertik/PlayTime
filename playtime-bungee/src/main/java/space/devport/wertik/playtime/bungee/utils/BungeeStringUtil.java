package space.devport.wertik.playtime.bungee.utils;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

@UtilityClass
public class BungeeStringUtil {
    public TextComponent format(String str) {
        return new TextComponent(ChatColor.translateAlternateColorCodes('&', str));
    }
}
