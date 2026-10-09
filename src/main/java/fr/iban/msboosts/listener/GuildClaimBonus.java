package fr.iban.msboosts.listener;

import fr.iban.guilds.GuildsPlugin;
import fr.iban.guilds.model.Guild;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.model.SChunk;
import fr.iban.lands.model.land.GuildLand;
import fr.iban.lands.model.land.Land;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;

/**
 * The jobs bonus of a guild level ({@code guild-claim-bonus.levels} in this plugin's config),
 * earned for actions done inside the player's own guild claims (MSLands). Only built when both
 * MSGuilds and MSLands are present.
 */
public class GuildClaimBonus {

    public enum Kind { XP, MONEY }

    record Bonus(int xp, int money) {}

    private final GuildsPlugin guilds;
    private final LandsPlugin lands;
    private final NavigableMap<Integer, Bonus> levels;

    public GuildClaimBonus(GuildsPlugin guilds, LandsPlugin lands, NavigableMap<Integer, Bonus> levels) {
        this.guilds = guilds;
        this.lands = lands;
        this.levels = levels;
    }

    /** Kept here so MSBoosts' own classes never reference MSGuilds / MSLands types. */
    public static GuildClaimBonus create(@Nullable ConfigurationSection levels) {
        return new GuildClaimBonus(GuildsPlugin.getInstance(), LandsPlugin.getInstance(), parseLevels(levels));
    }

    /** A level that does not set {@code jobs-xp} or {@code jobs-money} keeps the previous level's. */
    static NavigableMap<Integer, Bonus> parseLevels(@Nullable ConfigurationSection section) {
        NavigableMap<Integer, Bonus> parsed = new TreeMap<>();
        if (section == null) return parsed;
        NavigableMap<Integer, ConfigurationSection> byLevel = new TreeMap<>();
        for (String key : section.getKeys(false)) {
            try {
                byLevel.put(Integer.parseInt(key), section.getConfigurationSection(key));
            } catch (NumberFormatException ignored) {
                // not a level
            }
        }
        int xp = 0;
        int money = 0;
        for (Map.Entry<Integer, ConfigurationSection> e : byLevel.entrySet()) {
            if (e.getValue() != null) {
                xp = e.getValue().getInt("jobs-xp", xp);
                money = e.getValue().getInt("jobs-money", money);
            }
            parsed.put(e.getKey(), new Bonus(xp, money));
        }
        return parsed;
    }

    static int levelPercent(NavigableMap<Integer, Bonus> levels, int level, Kind kind) {
        Map.Entry<Integer, Bonus> entry = levels.floorEntry(level);
        if (entry == null) return 0;
        return kind == Kind.XP ? entry.getValue().xp() : entry.getValue().money();
    }

    /** Percentage for an action done at {@code location}; 0 outside the player's guild claims. */
    public int at(Player player, @Nullable Location location, Kind kind) {
        if (location == null) return 0;
        Guild guild = guilds.getGuildManager().getGuildByPlayerId(player.getUniqueId());
        if (guild == null) return 0;
        Land land = lands.getLandRepository().getLandAt(new SChunk(location));
        return percent(guild.getId(), land, levelPercent(guild, kind));
    }

    /** What the player's guild level grants inside its claims, wherever the player stands — for display. */
    public int ofPlayer(UUID playerId, Kind kind) {
        Guild guild = guilds.getGuildManager().getGuildByPlayerId(playerId);
        return guild == null ? 0 : levelPercent(guild, kind);
    }

    private int levelPercent(Guild guild, Kind kind) {
        return levelPercent(levels, guilds.getGuildLevelingManager().getLevel(guild), kind);
    }

    static int percent(@Nullable UUID playerGuildId, @Nullable Land land, int levelPercent) {
        boolean ownClaim = playerGuildId != null && land instanceof GuildLand guildLand
                && playerGuildId.equals(guildLand.getOwner());
        return ownClaim ? levelPercent : 0;
    }

    /** Where the action happened: the block, else the mob, else the player. */
    static Location actionLocation(@Nullable Block block, @Nullable Entity entity,
                                   @Nullable LivingEntity livingEntity, Player player) {
        if (block != null) return block.getLocation();
        if (livingEntity != null) return livingEntity.getLocation();
        if (entity != null) return entity.getLocation();
        return player.getLocation();
    }
}
