package me.darkness.crates.configuration;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class Config extends OkaeriConfig {
    @Comment("Czy wysyłać powiadomienia o aktualizacjach pluginu")
    public boolean updateChecker = true;

    @Comment("")
    @Comment("Po ilu sekundach wyzwanie ma wygasać")
    public int challengeExpire = 30;
}
