package fr.iban.msboosts.listener;

import fr.iban.guilds.GuildsPlugin;
import fr.iban.guilds.model.Guild;
import fr.iban.guilds.model.GuildLevelInfo;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.model.SChunk;
import fr.iban.lands.model.land.GuildLand;
import fr.iban.lands.model.land.Land;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * The jobs bonus of a guild level (MSGuilds {@code jobs-xp-bonus} / {@code jobs-money-bonus}),
 * earned for actions done inside the player's own guild claims (MSLands). Only built when both
 * plugins are present.
 */
public class GuildClaimBonus {

    public enum Kind { XP, MONEY }

    private final GuildsPlugin guilds;
    private final LandsPlugin lands;

    public GuildClaimBonus(GuildsPlugin guilds, LandsPlugin lands) {
        this.guilds = guilds;
        this.lands = lands;
    }

    /** Kept here so MSBoosts' own classes never reference MSGuilds / MSLands types. */
    public static GuildClaimBonus create() {
        return new GuildClaimBonus(GuildsPlugin.getInstance(), LandsPlugin.getInstance());
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
        GuildLevelInfo info = guilds.getGuildLevelingManager().getLevelInfo(guild);
        if (info == null) return 0;
        return kind == Kind.XP ? info.jobsXpBonus() : info.jobsMoneyBonus();
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
