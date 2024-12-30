package fr.iban.msboosts.listener;

import fr.iban.bukkitcore.event.CoreMessageEvent;
import fr.iban.common.messaging.Message;
import fr.iban.msboosts.MSBoostsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class CoreMessageListener implements Listener {

    private final MSBoostsPlugin plugin;

    public CoreMessageListener(MSBoostsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMessage(CoreMessageEvent event) {
        Message message = event.getMessage();
        String channel = message.getChannel();

        if (channel.equals(MSBoostsPlugin.BOOSTS_SYNC_CHANNEL)) {
            plugin.getBoostManager().loadBoosts(UUID.fromString(message.getMessage()));
        }
    }
}
