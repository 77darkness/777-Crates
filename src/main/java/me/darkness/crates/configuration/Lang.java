package me.darkness.crates.configuration;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

import java.util.List;
public class Lang extends OkaeriConfig {

    public String prefix = "&#FFB000&lꜱᴋʀᴢʏɴᴋɪ &8» ";

    @Comment("Typy wiadomosci: CHAT, ACTIONBAR, TITLE, SUBTITLE, TITLE_SUBTITLE, BOSSBAR")
    @Comment("")
    public MessageEntry noPermission = MessageEntry.chat("{prefix}&4Błąd! &cBrak uprawnień.");
    public MessageEntry playerNotFound = MessageEntry.chat("{prefix}&cGracz &4{player} &cjest offline.");
    public MessageEntry reloadSuccess = MessageEntry.chat("{prefix}&aPrzeładowano konfiguracje.");
    public MessageEntry commandUsage = MessageEntry.chat("{prefix}&cPoprawne użycie: &4{usage}");
    public MessageEntry notLookingAtBlock = MessageEntry.chat("{prefix}&cMusisz patrzeć na blok.");

    @Comment("")
    public MessageEntry crateNotFound = MessageEntry.chat("{prefix}&cSkrzynka &4{crate} &cnie istnieje.");
    public MessageEntry crateAlreadyExists = MessageEntry.chat("{prefix}&cSkrzynka &4{crate} &cjuż istnieje.");
    public MessageEntry crateCreated = MessageEntry.chat("{prefix}&aUtworzono skrzynkę &2{crate}&a.");
    public MessageEntry crateDeleted = MessageEntry.chat("{prefix}&cUsunięto skrzynkę &4{crate}&c.");
    public MessageEntry crateLocationSet = MessageEntry.chat("{prefix}&aUstawiono lokalizację skrzynki &2{crate}&a.");
    public MessageEntry crateLocationRemoved = MessageEntry.chat("{prefix}&cUsunięto lokalizację skrzynki &4{crate}&c.");
    public MessageEntry notCrate = MessageEntry.chat("{prefix}&cTen blok to nie skrzynka &4{crate}&c.");
    public MessageEntry crateNoRewards = MessageEntry.chat("{prefix}&cTa skrzynka &4({crate}&4) &cnie ma ustawionych nagród.");
    public MessageEntry noKeyInHand = MessageEntry.chat("{prefix}&cNie posiadasz klucza do tej skrzynki. Potrzebujesz &4{need}x &ckluczy.");
    public MessageEntry rewardWon = MessageEntry.chat("{prefix}&fTwoja wygrana: &e{item}&f!");

    @Comment("")
    public MessageEntry keyGiven = MessageEntry.chat("{prefix}&aNadano &2{amount}x &akluczy do skrzynki &2{crate} &agraczowi &2{player}&a.");
    public MessageEntry keyReceived = MessageEntry.chat("{prefix}&aOtrzymałeś &2{amount}x &akluczy do skrzynki &2{crate}&a.");
    public MessageEntry keyGivenAll = MessageEntry.chat("{prefix}&aWszyscy gracze otrzymali &2{amount}x &akluczy do skrzynki &2{crate}&a.");

    @Comment("")
    public MessageEntry chanceEditPrompt = MessageEntry.chat("{prefix}&fWpisz szansę dropu pomiędzy &#FFB0001-100&f. Aby anulować, wpisz &#FFB000anuluj&f.");
    public MessageEntry chanceEditCancelled = MessageEntry.chat("{prefix}&cAnulowano zmianę szansy.");
    public MessageEntry chanceEditInvalidNumber = MessageEntry.chat("{prefix}&cPodaj poprawną liczbę np. &425.5 &club &415&c.");
    public MessageEntry chanceEditOutOfRange = MessageEntry.chat("{prefix}&cSzansa musi być w zakresie &41-100&c.");
    public MessageEntry chanceEditSuccess = MessageEntry.chat("{prefix}&aUstawiono szansę na &2{chance}%");

    @Comment("")
    public MessageEntry commandEditPrompt = MessageEntry.chat("{prefix}&fWpisz komendę która ma być wykonywana jako nagroda. Użyj &#FFB000{player} &fjako nick gracza. Aby usunąć komendy wpisz &#FFB000brak&f. Aby anulować wpisz &#FFB000anuluj&f.");
    public MessageEntry commandEditCancelled = MessageEntry.chat("{prefix}&cAnulowano zmianę komend.");
    public MessageEntry commandEditSuccess = MessageEntry.chat("{prefix}&aZapisano komendę.");

    @Comment("")
    public MessageEntry giveItemToggled = MessageEntry.chat("{prefix}&aUstawiono typ nagrody na: &2{mode}&a.");

    @Comment("")
    public MessageEntry battleCantChallengeSelf = MessageEntry.chat("{prefix}&cNie możesz wyzwać samego siebie.");
    public MessageEntry battleChallengeSent = MessageEntry.chat("{prefix}&aWysłano wyzwanie do &2{player}&a.");
    public MessageEntry battleChallengeReceived = MessageEntry.chat("{prefix}&fMasz wyzwanie od &e{player}&f. Użyj &e/bitwa akceptuj {player} &f lub &e&nkliknij&e na tą wiadomość&f.");
    public MessageEntry battleChallengeFailed = MessageEntry.chat("{prefix}&cGracz jest podczas bitwy.");
    public MessageEntry battleNoChallengeFromPlayer = MessageEntry.chat("{prefix}&cNie masz wyzwania od &4{player}&c.");
    public MessageEntry battleAccepted = MessageEntry.chat("{prefix}&aZaakceptowano bitwę z &2{player}&a.");
    public MessageEntry battleChallengeExpired = MessageEntry.chat("{prefix}&cTo wyzwanie wygasło.");
    public MessageEntry battleNoKeysToAccept = MessageEntry.chat("{prefix}&cNie posiadasz wystarczającej ilości kluczy. Masz &4{have}x&c, potrzebujesz &4{need}x&c.");


    @Comment("")
    public boolean rewardBroadcastEnabled = true;
    @Comment("Wiadomosc o wylosowaniu nagrody dla wszystkich (Ponizej ustaw od jakiej szansy ma wysylac wiadomosc, np. 5.0 oznacza 5%)")
    @Comment("Zostaw 100 aby wysylac przy kazdym dropie")
    public double rewardBroadcastMaxChance = 100.0;
    public MessageEntry rewardBroadcast = MessageEntry.chat(
            "{prefix}&fGracz &e{player} &fwylosował &6{item} &fze skrzynki &6{crate}&f!"
    );

    @Comment("")
    public List<String> adminHelp = List.of(
        "&#00FF00&lSKRZYNKI - KOMENDY ADMINISTRACYJNE",
        "&8» &a/admincrate create <nazwa> &8- &fTworzy skrzynkę",
        "&8» &a/admincrate delete <nazwa> &8- &fUsuwa skrzynkę",
        "&8» &a/admincrate setlocation <nazwa> &8- &fUstawia lokalizację",
        "&8» &a/admincrate removelocation <nazwa> &8- &fUsuwa lokalizację",
        "&8» &a/admincrate give <gracz|all> <skrzynka> <ilość> &8- &fDaje klucze",
        "&8» &a/admincrate edit <nazwa> &8- &fEdytuje skrzynkę",
        "&8» &a/admincrate reload &8- &fPrzeładowuje plugin"
    );
    public static class MessageEntry extends OkaeriConfig {
        public String type = "CHAT";
        public Object message;

        public static MessageEntry chat(String message) {
            MessageEntry entry = new MessageEntry();
            entry.type = "CHAT";
            entry.message = message;
            return entry;
        }
    }
}
