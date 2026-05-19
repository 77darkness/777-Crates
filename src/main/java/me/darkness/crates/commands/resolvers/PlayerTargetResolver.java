package me.darkness.crates.commands.resolvers;

import dev.darkness.commands.argument.ArgumentParseException;
import dev.darkness.commands.argument.ArgumentResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class PlayerTargetResolver implements ArgumentResolver<PlayerTargetResolver.PlayerTarget> {

    @Override
    public Class<PlayerTarget> getType() {
        return PlayerTarget.class;
    }

    @Override
    public PlayerTarget resolve(CommandSender sender, String input) throws ArgumentParseException {
        if ("all".equalsIgnoreCase(input)) return new PlayerTarget(input);
        if (Bukkit.getPlayer(input) == null) throw new ArgumentParseException("player", input, PlayerTarget.class);
        return new PlayerTarget(input);
    }

    @Override
    public List<String> suggest(CommandSender sender) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("all");
        Bukkit.getOnlinePlayers().forEach(p -> suggestions.add(p.getName()));
        return suggestions;
    }

    public static final class PlayerTarget {
        private final String raw;

        PlayerTarget(String raw) { this.raw = raw; }

        public boolean isAll() { return "all".equalsIgnoreCase(raw); }

        public Player getPlayer() {
            return isAll() ? null : Bukkit.getPlayer(raw);
        }

        public Collection<? extends Player> resolve() {
            if (isAll()) return Bukkit.getOnlinePlayers();
            Player p = getPlayer();
            return p != null ? List.of(p) : List.of();
        }

        public String getRaw() { return raw; }

        @Override
        public String toString() { return raw; }
    }
}
