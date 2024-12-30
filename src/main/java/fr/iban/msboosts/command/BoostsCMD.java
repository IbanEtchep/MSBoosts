package fr.iban.msboosts.command;

import fr.iban.msboosts.MSBoostsPlugin;
import fr.iban.msboosts.api.BoostManager;
import fr.iban.msboosts.lang.Lang;
import fr.iban.msboosts.model.Boost;
import fr.iban.msboosts.util.TimeFormatter;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Flag;

import java.util.List;
import java.util.UUID;

@Command({"boosts", "msboosts"})
public class BoostsCMD {

    private final MSBoostsPlugin plugin ;
    private final BoostManager boostManager;

    public BoostsCMD(MSBoostsPlugin plugin) {
        this.plugin = plugin;
        this.boostManager = plugin.getBoostManager();
    }

    @CommandPlaceholder
    public void listBoosts(Player sender, @Default("false") @Flag boolean include_expired) {
        UUID targetId = sender.getUniqueId();

        sender.sendMessage(Lang.BOOST_LIST_GLOBAL_TITLE.component());

        List<Boost> globalBoosts = boostManager.getGlobalBoosts().stream()
                .filter(b -> include_expired || !b.isExpired()).toList();

        if(globalBoosts.isEmpty()) {
            sender.sendMessage(Lang.BOOST_LIST_NONE.component());
        }else {
            for (Boost globalBoost : globalBoosts) {
                showBoost(sender, globalBoost);
            }
        }

        sender.sendMessage(Lang.BOOST_LIST_PERSONAL_TITLE.component("player", sender.getName()));

        List<Boost> boosts = boostManager.getBoosts(targetId).stream()
                .filter(b -> include_expired || !b.isExpired()).toList();

        if(boosts.isEmpty()) {
            sender.sendMessage(Lang.BOOST_LIST_NONE.component());
        }else {
            for (Boost playerBoost : boosts) {
                showBoost(sender, playerBoost);
            }
        }

        if(boostManager.getPercentage(targetId) > 0) {
            sender.sendMessage(Lang.BOOST_LIST_TOTAL.component(
                    "percentage", boostManager.getTotalPercentage(targetId)+"",
                    "multiplier", boostManager.getTotalMultiplier(targetId)+""
            ));
        }
    }

    private void showBoost(Player sender, Boost playerBoost) {
        switch (playerBoost.getStatus()) {
            case WAITING_TO_START:
                sender.sendMessage(Lang.BOOST_LIST_LINE_QUEUED.component(
                        "percentage", playerBoost.getPercentage()+"",
                        "duration", TimeFormatter.formatTime(playerBoost.getDuration())
                ));
                break;
            case ACTIVE:
                sender.sendMessage(Lang.BOOST_LIST_LINE_ACTIVE.component(
                        "percentage", playerBoost.getPercentage()+"",
                        "duration", TimeFormatter.formatTime(playerBoost.getDuration()),
                        "remaining", TimeFormatter.formatTime(playerBoost.getRemainingTime())
                ));
                break;
            case PAUSED:
                sender.sendMessage(Lang.BOOST_LIST_LINE_PAUSED.component(
                        "percentage", playerBoost.getPercentage()+"",
                        "duration", TimeFormatter.formatTime(playerBoost.getDuration()),
                        "remaining", TimeFormatter.formatTime(playerBoost.getRemainingTime())
                        ));
                break;
            case EXPIRED:
                sender.sendMessage(Lang.BOOST_LIST_LINE_EXPIRED.component(
                        "percentage", playerBoost.getPercentage()+"",
                        "duration", TimeFormatter.formatTime(playerBoost.getDuration())
                ));
                break;
        }
    }

}
