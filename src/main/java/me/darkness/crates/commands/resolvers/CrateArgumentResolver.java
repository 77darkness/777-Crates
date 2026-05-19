package me.darkness.crates.commands.resolvers;

import dev.darkness.commands.argument.ArgumentParseException;
import dev.darkness.commands.argument.ArgumentResolver;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.CrateService;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class CrateArgumentResolver implements ArgumentResolver<Crate> {

    private final CrateService crateService;

    public CrateArgumentResolver(CrateService crateService) {
        this.crateService = crateService;
    }

    @Override
    public Class<Crate> getType() {
        return Crate.class;
    }

    @Override
    public Crate resolve(CommandSender sender, String input) throws ArgumentParseException {
        return crateService.getCrate(input)
                .orElseThrow(() -> new ArgumentParseException("skrzynka", input, Crate.class));
    }

    @Override
    public List<String> suggest(CommandSender sender) {
        return crateService.getAllCrates().stream()
                .map(Crate::getName)
                .toList();
    }
}

