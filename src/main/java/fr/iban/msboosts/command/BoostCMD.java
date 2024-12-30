package fr.iban.msboosts.command;

import fr.iban.common.model.MSPlayer;
import fr.iban.msboosts.MSBoostsPlugin;
import fr.iban.msboosts.api.BoostManager;
import fr.iban.msboosts.lang.Lang;
import fr.iban.msboosts.model.Boost;
import fr.iban.msboosts.util.TimeFormatter;
import net.kyori.adventure.text.Component;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.List;
import java.util.UUID;

@Command({"boost", "msboost"})
@CommandPermission("msboosts.admin")
public class BoostCMD {

    private final MSBoostsPlugin plugin ;
    private final BoostManager boostManager;

    public BoostCMD(MSBoostsPlugin plugin) {
        this.plugin = plugin;
        this.boostManager = plugin.getBoostManager();
    }

    @Subcommand("reload")
    public void reload(BukkitCommandActor sender) {
        plugin.loadConfig();
        plugin.getLangManager().load();
        sender.reply(Lang.RELOADED.component());
    }

    @Subcommand("add")
    @Usage("/boost add <player> <percentage> <duration>")
    public void addBoost(BukkitCommandActor sender, MSPlayer target, int percentage, long duration) {
        duration = duration * 1000;

        Boost boost = new Boost(UUID.randomUUID(), target.getUniqueId(), percentage, duration);
        boostManager.addBoost(boost);

        sender.reply(Lang.BOOST_PERSONAL_ADDED.component(
                "player", target.getName(),
                "percentage", percentage+"",
                "duration", TimeFormatter.formatTime(duration)
        ));

        plugin.getPlayerManager().sendMessageIfOnline(target.getUniqueId(), Lang.BOOST_PERSONAL_RECEIVED.component(
                "percentage", percentage+"",
                "duration", TimeFormatter.formatTime(duration)
        ));
    }

    @Subcommand("addglobal")
    @Usage("/boost addglobal <percentage> <duration>")
    public void addGlobalBoost(BukkitCommandActor sender, int percentage, long duration) {
        duration = duration * 1000;

        Boost boost = new Boost(UUID.randomUUID(), UUID.fromString("00000000-0000-0000-0000-000000000000"), percentage, duration);
        boostManager.addBoost(boost);

        sender.reply(Lang.BOOST_GLOBAL_ADDED.component(
                "percentage", percentage+"",
                "duration", TimeFormatter.formatTime(duration)
        ));
    }

    @Subcommand("remove")
    @Usage("/boost remove <player> <boost>")
    public void removeBoost(BukkitCommandActor actor, UUID ownerID, UUID boostID) {
        Boost boost = boostManager.getBoosts(ownerID).stream().filter(b -> b.getId().equals(boostID)).findFirst().orElse(null);

        if(boost == null) {
            actor.reply(Lang.BOOST_NOT_FOUND.component());
            return;
        }

        boostManager.removeBoost(boost);
        actor.reply(Lang.BOOST_REMOVED.component());
    }

    @Subcommand("list")
    @Usage("/boost list <player>")
    public void listBoosts(BukkitCommandActor actor, MSPlayer target, @Default("false") @Flag boolean include_expired) {
        actor.reply(Lang.BOOST_LIST_GLOBAL_TITLE.component());

        List<Boost> globalBoosts = boostManager.getGlobalBoosts().stream()
                .filter(b -> include_expired || !b.isExpired()).toList();

        if(globalBoosts.isEmpty()) {
            actor.reply(Lang.BOOST_LIST_NONE.component());
        }else {
            for (Boost globalBoost : globalBoosts
            ) {
                showBoost(actor, globalBoost);
            }
        }

        actor.reply(Lang.BOOST_LIST_PERSONAL_TITLE.component("player", target.getName()));

        List<Boost> boosts = boostManager.getBoosts(target.getUniqueId()).stream()
                .filter(b -> include_expired || !b.isExpired()).toList();

        if(boosts.isEmpty()) {
            actor.reply(Lang.BOOST_LIST_NONE.component());
        }else {
            for (Boost playerBoost : boosts) {
                showBoost(actor, playerBoost);
            }
        }

        if(boostManager.getPercentage(target.getUniqueId()) > 0) {
            actor.reply(Lang.BOOST_LIST_TOTAL.component(
                    "percentage", boostManager.getTotalPercentage(target.getUniqueId())+"",
                    "multiplier", boostManager.getTotalMultiplier(target.getUniqueId())+""
            ));
        }
    }

    private void showBoost(BukkitCommandActor actor, Boost playerBoost) {
        Component message = switch (playerBoost.getStatus()) {
            case WAITING_TO_START -> Lang.BOOST_LIST_LINE_QUEUED.component(
                    "percentage", playerBoost.getPercentage() + "",
                    "duration", TimeFormatter.formatTime(playerBoost.getDuration())
            );
            case ACTIVE -> Lang.BOOST_LIST_LINE_ACTIVE.component(
                    "percentage", playerBoost.getPercentage() + "",
                    "duration", TimeFormatter.formatTime(playerBoost.getDuration()),
                    "remaining", TimeFormatter.formatTime(playerBoost.getRemainingTime())
            );
            case PAUSED -> Lang.BOOST_LIST_LINE_PAUSED.component(
                    "percentage", playerBoost.getPercentage() + "",
                    "duration", TimeFormatter.formatTime(playerBoost.getDuration()),
                    "remaining", TimeFormatter.formatTime(playerBoost.getRemainingTime())
            );
            case EXPIRED -> Lang.BOOST_LIST_LINE_EXPIRED.component(
                    "percentage", playerBoost.getPercentage() + "",
                    "duration", TimeFormatter.formatTime(playerBoost.getDuration())
            );
        };

        message = message.append(Component.text(" "))
                .append(Lang.BOOST_LIST_REMOVE_BUTTON.component(
                        "owner", playerBoost.getOwner().toString(),
                        "boost", playerBoost.getId().toString()
                ));

        actor.reply(message);
    }
}
