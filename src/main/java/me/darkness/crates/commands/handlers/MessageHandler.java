package me.darkness.crates.commands.handlers;

import dev.darkness.commands.MessageProvider;
import me.darkness.crates.configuration.ConfigService;
import org.bukkit.command.CommandSender;

import java.util.Map;

public final class MessageHandler implements MessageProvider {

    private final ConfigService configService;

    public MessageHandler(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public void sendOnlyPlayer(CommandSender sender) {
        configService.lang().onlyPlayer.send(sender);
    }

    @Override
    public void sendNoPermission(CommandSender sender, String permission) {
        configService.lang().noPermission.send(sender);
    }

    @Override
    public void sendUsage(CommandSender sender, String usage) {
        configService.lang().commandUsage.send(sender, Map.of("usage", usage));
    }
}

