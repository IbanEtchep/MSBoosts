package fr.iban.msboosts.listener;

import com.gamingmesh.jobs.api.JobsExpGainEvent;
import com.gamingmesh.jobs.api.JobsPrePaymentEvent;
import fr.iban.msboosts.MSBoostsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class JobsListener implements Listener {

    private final MSBoostsPlugin plugin;

    public JobsListener(MSBoostsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPayment(JobsPrePaymentEvent e) {
        Player player = e.getPlayer().getPlayer();

        if (player == null) return;

        double multiplier = plugin.getBoostManager().getMultiplier(player.getUniqueId());

        e.setAmount(e.getAmount() * multiplier);
    }

    @EventHandler
    public void onExpGain(JobsExpGainEvent e) {
        Player player = e.getPlayer().getPlayer();

        if (player == null) return;

        double multiplier = plugin.getBoostManager().getMultiplier(player.getUniqueId());

        e.setExp(e.getExp() * multiplier);
    }

}
