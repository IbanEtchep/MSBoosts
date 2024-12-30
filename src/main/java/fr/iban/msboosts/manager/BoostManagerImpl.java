package fr.iban.msboosts.manager;

import fr.iban.bukkitcore.manager.BukkitPlayerManager;
import fr.iban.msboosts.MSBoostsPlugin;
import fr.iban.msboosts.api.BoostManager;
import fr.iban.msboosts.enums.BoostStatus;
import fr.iban.msboosts.enums.PauseReason;
import fr.iban.msboosts.lang.Lang;
import fr.iban.msboosts.model.Boost;
import fr.iban.msboosts.storage.SqlStorage;
import fr.iban.msboosts.storage.Storage;
import fr.iban.msboosts.util.TimeFormatter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Time;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BoostManagerImpl implements BoostManager {

    private final MSBoostsPlugin plugin;
    private final UUID GLOBAL_OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private final Storage storage;

    private final Map<UUID, List<Boost>> boosts = new ConcurrentHashMap<>();

    public BoostManagerImpl(MSBoostsPlugin plugin) {
        this.plugin = plugin;
        this.storage = new SqlStorage();
        plugin.executeQueued(storage::init);
        load();
        startBoostStatusCheckTask();
    }

    private void load() {
        loadBoosts(GLOBAL_OWNER_ID);
        Bukkit.getOnlinePlayers().forEach(player -> loadBoosts(player.getUniqueId()));
    }

    @Override
    public void addBoost(Boost boost) {
        UUID owner = boost.getOwner();
        boosts.computeIfAbsent(owner, k -> List.of());

        // if one boost with the same percentage is already active, we add the duration to it
        for (Boost activeBoost : boosts.get(owner)) {
            if(activeBoost.isActive() && activeBoost.getPercentage() == boost.getPercentage()) {
                activeBoost.setDuration(activeBoost.getDuration() + boost.getDuration());
                plugin.executeQueued(() -> storage.saveBoost(activeBoost));
                return;
            }
        }

        boosts.get(owner).add(boost);
        saveBoost(boost);
        handleBoosts();
    }

    public void saveBoost(Boost boost) {
        plugin.executeQueued(() -> {
            storage.saveBoost(boost);
            plugin.getMessenger().sendMessage(MSBoostsPlugin.BOOSTS_SYNC_CHANNEL, boost.getOwner().toString());
        });
    }

    @Override
    public void removeBoost(Boost boost) {
        UUID owner = boost.getOwner();

        if (boosts.containsKey(owner)) {
            boosts.get(owner).remove(boost);
        }

        plugin.executeQueued(() -> {
            storage.removeBoost(boost);
            plugin.getMessenger().sendMessage(MSBoostsPlugin.BOOSTS_SYNC_CHANNEL, owner.toString());
        });
    }

    @Override
    public List<Boost> getBoosts(UUID uuid) {
        return boosts.getOrDefault(uuid, List.of());
    }

    @Override
    public List<Boost> getGlobalBoosts() {
        return boosts.getOrDefault(GLOBAL_OWNER_ID, List.of());
    }

    @Override
    public List<Boost> getActiveBoosts(UUID uuid) {
        return getBoosts(uuid).stream().filter(Boost::isActive).toList();
    }

    @Override
    public List<Boost> getActiveGlobalBoosts() {
        return getGlobalBoosts().stream().filter(Boost::isActive).toList();
    }

    @Override
    public double getMultiplier(UUID uuid) {
        return 1 + (getPercentage(uuid) / 100D);
    }

    @Override
    public double getGlobalMultiplier() {
        return 1 + (getGlobalPercentage() / 100D);
    }

    @Override
    public double getTotalMultiplier(UUID uuid) {
        return 1 + (getTotalPercentage(uuid) / 100D);
    }

    @Override
    public int getPercentage(UUID uuid) {
        return getBoosts(uuid).stream().filter(Boost::isActive).mapToInt(Boost::getPercentage).sum();
    }

    @Override
    public int getGlobalPercentage() {
        return getGlobalBoosts().stream().filter(Boost::isActive).mapToInt(Boost::getPercentage).sum();
    }

    @Override
    public int getTotalPercentage(UUID uuid) {
        return getPercentage(uuid) + getGlobalPercentage();
    }

    @Override
    public long getBoostTimeLeft(UUID uuid) {
        return getBoosts(uuid).stream().filter(Boost::isActive).mapToLong(Boost::getRemainingTime).max().orElse(0);
    }

    @Override
    public long getGlobalBoostTimeLeft() {
        return getBoostTimeLeft(GLOBAL_OWNER_ID);
    }

    @Override
    public void loadBoosts(UUID uuid) {
        plugin.executeQueued(() -> {
            List<Boost> boosts = storage.getBoosts(uuid);
            this.boosts.put(uuid, boosts);
        });
    }

    public boolean canStart(Boost boost) {
        boolean isGlobal = boost.getOwner().equals(GLOBAL_OWNER_ID);
        int maxPercentage = isGlobal ?
                plugin.getConfig().getInt("boosts.global.max-percentage") :
                plugin.getConfig().getInt("boosts.personal.max-percentage");

        return (getPercentage(boost.getOwner()) + boost.getPercentage()) <= maxPercentage;
    }

    public void startBoostStatusCheckTask() {
        plugin.getScheduler().runTimerAsync(this::handleBoosts, 0, 10, TimeUnit.SECONDS);
    }

    @Override
    public void pauseBoost(Boost boost, PauseReason reason) {
        boost.pauseBoost(reason);
        plugin.executeQueued(() -> storage.saveBoost(boost));
    }

    @Override
    public void resumeBoost(Boost boost) {
        boost.resumeBoost();
        plugin.executeQueued(() -> storage.saveBoost(boost));

        Player player = Bukkit.getPlayer(boost.getOwner());
        if(!boost.isGlobal() && player != null) {
            player.sendMessage(Lang.BOOST_PERSONAL_RESUMED.component(
                    "percentage", boost.getPercentage() + "",
                    "duration", TimeFormatter.formatTime(boost.getRemainingTime())
            ));
        }
    }

    @Override
    public void activateBoost(Boost boost) {
        boost.setStatus(BoostStatus.ACTIVE);
        boost.setStartTime(Instant.ofEpochMilli(System.currentTimeMillis()));
        plugin.executeQueued(() -> storage.saveBoost(boost));

        if(boost.isGlobal()) {
            plugin.getPlayerManager().sendMessageIfOnline(boost.getOwner(), Lang.BOOST_GLOBAL_ACTIVATED.component(
                    "percentage", boost.getPercentage() + "",
                    "duration", TimeFormatter.formatTime(boost.getDuration())
            ));
        }

        if(plugin.getPlayerManager().isOnline(boost.getOwner())) {
            plugin.getPlayerManager().sendMessageIfOnline(
                    boost.getOwner(),
                    Lang.BOOST_PERSONAL_ACTIVATED.component(
                    "percentage", boost.getPercentage() + "",
                    "duration", TimeFormatter.formatTime(boost.getDuration())
            ));
        }
    }

    @Override
    public void expireBoost(Boost boost) {
        boost.setStatus(BoostStatus.EXPIRED);
        plugin.executeQueued(() -> storage.saveBoost(boost));
    }

    @Override
    public void handleBoosts() {
        BukkitPlayerManager playerManager = plugin.getPlayerManager();

        for (List<Boost> boosts : this.boosts.values()) {
            for (Boost boost : boosts) {
                if (boost.getStatus() == BoostStatus.WAITING_TO_START) {
                    if(canStart(boost)) {
                        activateBoost(boost);
                    }
                }

                if (boost.isActive()) {
                    if (boost.getRemainingTime() <= 0) {
                        expireBoost(boost);
                    }
                }

                if(!boost.getOwner().equals(GLOBAL_OWNER_ID)) {
                    // Mise en pause lors de la déconnexion
                    if(boost.isActive() && !playerManager.isOnline(boost.getOwner())) {
                        pauseBoost(boost, PauseReason.DISCONNECTED);
                    }

                    // Reprise lors de la reconnexion
                    if(boost.isPaused() && playerManager.isOnline(boost.getOwner())) {
                        resumeBoost(boost);
                    }
                }
            }
        }
    }
}
