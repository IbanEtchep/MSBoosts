package fr.iban.msboosts.listener;

import com.gamingmesh.jobs.api.JobsExpGainEvent;
import com.gamingmesh.jobs.api.JobsPrePaymentEvent;
import fr.iban.msboosts.MSBoostsPlugin;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Applies boosts to jobs gains. The guild claim bonus is ADDED to the boost percentage (one
 * multiplier, computed here) rather than applied by another listener, which would compound
 * with it and depend on listener order. It boosts XP and money, not points.
 */
public class JobsListener implements Listener {

    private final MSBoostsPlugin plugin;

    public JobsListener(MSBoostsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPayment(JobsPrePaymentEvent e) {
        Player player = e.getPlayer().getPlayer();

        if (player == null) return;

        int boost = plugin.getBoostManager().getTotalPercentage(player.getUniqueId());
        int guild = guildBonus(player, e.getBlock(), e.getEntity(), e.getLivingEntity(), GuildClaimBonus.Kind.MONEY);

        e.setAmount(e.getAmount() * multiplier(boost + guild));
        e.setPoints(e.getPoints() * multiplier(boost));
    }

    @EventHandler
    public void onExpGain(JobsExpGainEvent e) {
        Player player = e.getPlayer().getPlayer();

        if (player == null) return;

        int boost = plugin.getBoostManager().getTotalPercentage(player.getUniqueId());
        int guild = guildBonus(player, e.getBlock(), e.getEntity(), e.getLivingEntity(), GuildClaimBonus.Kind.XP);

        e.setExp(e.getExp() * multiplier(boost + guild));
    }

    private int guildBonus(Player player, Block block, Entity entity, LivingEntity livingEntity, GuildClaimBonus.Kind kind) {
        GuildClaimBonus bonus = plugin.getGuildClaimBonus();
        if (bonus == null) return 0;
        return bonus.at(player, GuildClaimBonus.actionLocation(block, entity, livingEntity, player), kind);
    }

    private static double multiplier(int percentage) {
        return 1 + percentage / 100D;
    }
}
