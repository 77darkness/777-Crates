package me.darkness.crates.configuration;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class Config extends OkaeriConfig {
    @Comment("Czy wysyłać powiadomienia o aktualizacjach pluginu")
    public boolean updateChecker = true;

    @Comment("")
    @Comment("Po ilu sekundach wyzwanie do casebattle ma wygasać")
    public int challengeExpire = 120;

    @Comment("")
    @Comment("Ile sekund trwa odliczanie przed rozpoczęciem bitwy (0 = brak odliczania)")
    public int battleCountdownSeconds = 10;
}
