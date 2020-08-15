package space.devport.wertik.playtime.spigot;

import space.devport.utils.text.language.LanguageDefaults;

public class PlayTimeLanguage extends LanguageDefaults {

    @Override
    public void setDefaults() {
        addDefault("Commands.Invalid-Player", "&cPlayer &f%player% &cdoes not exist.");
        addDefault("Commands.No-Record", "&f%player% &chas no record.");

        addDefault("Commands.Check", "&7Play time: &f%time%");
    }
}