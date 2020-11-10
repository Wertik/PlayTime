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

        addDefault("Commands.Check.Done", "&7Playtime: &f%time%");
        addDefault("Commands.Check.Could-Not", "&cCouldn't get playtime.");

        addDefault("Commands.Reset.Import-Warning", "&7Player has been reset, but '&fimport-statistics&7' is enabled. Time will be imported.");
        addDefault("Commands.Reset.Done", "&7Played time reset.");
        addDefault("Commands.Reset.Done-Others", "&7Played time reset for &f%player%");

        addDefault("Commands.Global-Check.No-Servers", "&cThere are no remote servers connected.");
        addDefault("Commands.Global-Check.Header", "&8&m    &3 Global Play Times");
        addDefault("Commands.Global-Check.Line", "&8 - &f%serverName% &8= &r%time%");

        addDefault("Commands.Top.Header", "&8&m    &3 PlayTime Top");
        addDefault("Commands.Top.Line", "&f%position% &8- &f%name% &8= &r%time%");
        addDefault("Commands.Top.Failed", "&cFailed to fetch the playtime top.");
    }
}