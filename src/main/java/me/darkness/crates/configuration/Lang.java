package me.darkness.crates.configuration;

import dev.darkness.utilities.text.TextUtil;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class Lang extends OkaeriConfig {

    @Comment("Typy wiadomosci: CHAT, ACTIONBAR, TITLE, SUBTITLE, TITLE_SUBTITLE, BOSSBAR")
    @Comment("")
    public MessageEntry noPermission = MessageEntry.chat("&#FF0000☹ &cNie masz uprawnień do tej komendy. &#FF0000(777crate.admin)");
    public MessageEntry playerNotFound = MessageEntry.chat("&#FF0000☹ &cGracz &#FF0000{player} &cjest offline.");
    public MessageEntry reloadSuccess = MessageEntry.chat("&#00FF00☺ &fPrzeładowano konfiguracje!");
    public MessageEntry commandUsage = MessageEntry.chat("&#FF0000☹ &cPoprawne użycie: &#FF0000{usage}");
    public MessageEntry notLookingAtBlock = MessageEntry.chat("&#FF0000☹ &cMusisz patrzeć na blok.");

    @Comment("")
    public MessageEntry crateNotFound = MessageEntry.chat("&#FF0000☹ &cSkrzynka &#FF0000{crate} &cnie istnieje.");
    public MessageEntry crateAlreadyExists = MessageEntry.chat("&#FF0000☹ &cSkrzynka &#FF0000{crate} &cjuż istnieje.");
    public MessageEntry crateCreated = MessageEntry.chat("&#00FF00☺ &fUtworzono skrzynkę &#00FF00{crate}&f.");
    public MessageEntry crateDeleted = MessageEntry.chat("&#00FF00☺ &fUsunięto skrzynkę &#00FF00{crate}&f.");
    public MessageEntry crateLocationSet = MessageEntry.chat("&#00FF00☺ &fUstawiono lokalizację skrzynki &#00FF00{crate}&f.");
    public MessageEntry crateLocationRemoved = MessageEntry.chat("&#00FF00☺ &fUsunięto lokalizację skrzynki &#00FF00{crate}&f.");
    public MessageEntry notCrate = MessageEntry.chat("&#FF0000☹ &cTen blok to nie skrzynka &#FF0000{crate}&c.");
    public MessageEntry crateNoRewards = MessageEntry.chat("&#FF0000☹ &cTa skrzynka nie ma ustawionych nagród!");
    public MessageEntry noKey = MessageEntry.subtitle("&8» &#FF0000Nie posiadasz klucza do tej skrzynki!");
    public MessageEntry rewardWon = MessageEntry.chat("&#00FF00☺ &fTwoja wygrana: &#00FF00{item}&f!");

    @Comment("")
    public MessageEntry keyGiven = MessageEntry.chat("&#00FF00☺ &fNadano &#00FF00{amount}x &fkluczy do skrzynki &#00FF00{crate} &fgraczowi &#00FF00{player}&f.");
    public MessageEntry keyReceived = MessageEntry.chat("&#00FF00☺ &fOtrzymałeś &#00FF00{amount}x &fkluczy do skrzynki &#00FF00{crate}&f.");
    public MessageEntry keyGivenAll = MessageEntry.chat("&#00FF00☺ &fWszyscy gracze otrzymali &#00FF00{amount}x &fkluczy do skrzynki &#00FF00{crate}&f.");

    @Comment("")
    public MessageEntry chanceEditPrompt = MessageEntry.chat("&fWpisz szansę dropu pomiędzy &#00FF001-100&f. Aby anulować, wpisz &#FF0000anuluj&f.");
    public MessageEntry chanceEditCancelled = MessageEntry.chat("&#FF0000☹ &cAnulowano zmianę szansy.");
    public MessageEntry chanceEditInvalidNumber = MessageEntry.chat("&#FF0000☹ &cPodaj poprawną liczbę (np. &e25.5 &club &e15&c).");
    public MessageEntry chanceEditOutOfRange = MessageEntry.chat("&#FF0000☹ &cSzansa musi być w zakresie &#FF00001-100&c.");
    public MessageEntry chanceEditSuccess = MessageEntry.chat("&#00FF00☺ &fUstawiono szansę na &#00FF00{chance}%&f.");

    @Comment("")
    public MessageEntry commandEditPrompt = MessageEntry.chat("&fWpisz komendę która ma być wykonywana jako nagroda. Użyj &#00FF00{player} &fjako nick gracza.\n&fAby usunąć komendy wpisz &#FFFF00brak&f. Aby anulować wpisz &#FF0000anuluj&f.");
    public MessageEntry commandEditCancelled = MessageEntry.chat("&#FF0000☹ &cAnulowano zmianę komend.");
    public MessageEntry commandEditSuccess = MessageEntry.chat("&#00FF00☺ &fZapisano komendę.");

    @Comment("")
    public MessageEntry giveItemToggled = MessageEntry.chat("&#00FF00☺ &fUstawiono typ nagrody na: &#00FF00{mode}&f.");

    @Comment("")
    public MessageEntry battleCantChallengeSelf = MessageEntry.chat("&#FF0000☹ &cNie możesz wyzwać samego siebie.");
    public MessageEntry battleChallengeSent = MessageEntry.chat("&#00FF00☺ &fWysłano wyzwanie do &#00FF00{player}&f.");
    public MessageEntry battleChallengeReceived = MessageEntry.chat("&#00FF00☺ &fMasz wyzwanie od &#00FF00{player}&f!\n&fUżyj &#00FF00/bitwa akceptuj {player} &flub &#00FF00&nkliknij&#00FF00 na tą wiadomość&f.");
    public MessageEntry battleChallengeFailed = MessageEntry.chat("&#FF0000☹ &cGracz jest w trakcie bitwy.");
    public MessageEntry battleNoChallengeFromPlayer = MessageEntry.chat("&#FF0000☹ &cNie masz wyzwania od &#FF0000{player}&c.");
    public MessageEntry battleAccepted = MessageEntry.chat("&#00FF00☺ &fZaakceptowano bitwę z &#00FF00{player}&f!");
    public MessageEntry battleChallengeExpired = MessageEntry.chat("&#FF0000☹ &cTo wyzwanie wygasło.");

    @Comment("")
    public boolean rewardBroadcastEnabled = true;
    @Comment("Od jakiej szansy (w %) ma być wysyłany broadcast (100 = zawsze)")
    public double rewardBroadcastMaxChance = 100.0;
    public MessageEntry rewardBroadcast = MessageEntry.chat(
            "&8» &fGracz &#00FF00{player} &fwylosował &#FFFF00{item} &fze skrzynki &#00FF00{crate}&f!"
    );

    @Comment("")
    public List<String> adminHelp = List.of(
        "&#FB0000☹ &cPoprawne użycie komend:",
        "&8- &#FB0000/777crate create <nazwa>",
        "&8- &#FB0000/777crate delete <nazwa>",
        "&8- &#FB0000/777crate setlocation <nazwa>",
        "&8- &#FB0000/777crate removelocation <nazwa>",
        "&8- &#FB0000/777crate give <gracz|all> <skrzynka> <ilość>",
        "&8- &#FB0000/777crate edit <nazwa>",
        "&8- &#FB0000/777crate reload"
    );
    public static class MessageEntry extends OkaeriConfig {
        public String type = "CHAT";
        public Object message;

        public void send(CommandSender sender, Map<String, String> placeholders) {
            if (message == null) return;
            TextUtil.MessageType msgType = parseType(type);
            if (message instanceof List<?> list) {
                if (msgType == TextUtil.MessageType.TITLE_SUBTITLE) {
                    if (sender instanceof Player player) {
                        String title = !list.isEmpty() ? apply(String.valueOf(list.get(0)), placeholders) : "";
                        String subtitle = list.size() > 1 ? apply(String.valueOf(list.get(1)), placeholders) : "";
                        TextUtil.sendTitleSubtitle(player, title, subtitle);
                    }
                } else {
                    for (Object line : list) {
                        sendText(sender, apply(String.valueOf(line), placeholders), msgType);
                    }
                }
            } else {
                sendText(sender, apply(String.valueOf(message), placeholders), msgType);
            }
        }

        public void send(CommandSender sender) {
            send(sender, Map.of());
        }

        private void sendText(CommandSender sender, String text, TextUtil.MessageType type) {
            if (sender instanceof Player player) {
                TextUtil.send(player, text, type);
            } else {
                TextUtil.send(sender, text);
            }
        }

        private String apply(String text, Map<String, String> placeholders) {
            if (text == null) return "";
            return TextUtil.applyPlaceholders(text, placeholders);
        }

        private TextUtil.MessageType parseType(String t) {
            if (t == null) return TextUtil.MessageType.CHAT;
            return switch (t.toUpperCase()) {
                case "ACTIONBAR" -> TextUtil.MessageType.ACTIONBAR;
                case "TITLE" -> TextUtil.MessageType.TITLE;
                case "SUBTITLE" -> TextUtil.MessageType.SUBTITLE;
                case "TITLE_SUBTITLE" -> TextUtil.MessageType.TITLE_SUBTITLE;
                case "BOSSBAR" -> TextUtil.MessageType.BOSSBAR;
                default -> TextUtil.MessageType.CHAT;
            };
        }

        public static MessageEntry chat(String message) {
            MessageEntry entry = new MessageEntry();
            entry.type = "CHAT";
            entry.message = message;
            return entry;
        }

        public static MessageEntry chat(List<String> message) {
            MessageEntry entry = new MessageEntry();
            entry.type = "CHAT";
            entry.message = message;
            return entry;
        }

        public static MessageEntry actionbar(String message) {
            MessageEntry entry = new MessageEntry();
            entry.type = "ACTIONBAR";
            entry.message = message;
            return entry;
        }

        public static MessageEntry title(String title) {
            MessageEntry entry = new MessageEntry();
            entry.type = "TITLE";
            entry.message = title;
            return entry;
        }

        public static MessageEntry subtitle(String subtitle) {
            MessageEntry entry = new MessageEntry();
            entry.type = "SUBTITLE";
            entry.message = subtitle;
            return entry;
        }

        public static MessageEntry titleSubtitle(String title, String subtitle) {
            MessageEntry entry = new MessageEntry();
            entry.type = "TITLE_SUBTITLE";
            entry.message = List.of(title, subtitle);
            return entry;
        }

        public static MessageEntry bossbar(String message) {
            MessageEntry entry = new MessageEntry();
            entry.type = "BOSSBAR";
            entry.message = message;
            return entry;
        }
    }
}
