package space.devport.wertik.playtime.spigot;

import space.devport.utils.DevportPlugin;
import space.devport.utils.text.language.LanguageDefaults;

public class PlayTimeLanguage extends LanguageDefaults {

    public PlayTimeLanguage(DevportPlugin plugin) {
        super(plugin);
    }

    @Override
    public void setDefaults() {
        addDefault("Commands.Invalid-Player", "&cPlayer &f%player% &cdoes not exist.");
        addDefault("Commands.No-Record", "&f%player% &chas no record.");

        addDefault("Commands.Check", "&7Play time: &f%time%");

        addDefault("Commands.Reset.Import-Warning", "&7Player has been reset, but '&fimport-statistics&7' is enabled. Time will be imported.");
        addDefault("Commands.Reset.Done", "&7Played time reset.");
        addDefault("Commands.Reset.Done-Others", "&7Played time reset for &f%player%");
    }
}