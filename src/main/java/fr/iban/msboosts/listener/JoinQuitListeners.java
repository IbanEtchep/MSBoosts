package fr.iban.msboosts.listener;

import fr.iban.msboosts.MSBoostsPlugin;
import fr.iban.msboosts.api.BoostManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinQuitListeners implements Listener {

    private final MSBoostsPlugin plugin;

    public JoinQuitListeners(MSBoostsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        BoostManager boostManager = plugin.getBoostManager();

        if(boostManager.getBoosts(player.getUniqueId()).isEmpty()) {
            boostManager.loadBoosts(player.getUniqueId());
        }
    }
}
