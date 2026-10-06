package fr.iban.msboosts.listener;

import fr.iban.lands.model.land.GuildLand;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GuildClaimBonusTest {

    private final UUID knights = UUID.randomUUID();

    @Test
    void appliesInsideTheOwnGuildClaim() {
        assertEquals(7, GuildClaimBonus.percent(knights, new GuildLand(UUID.randomUUID(), knights, "base"), 7));
    }

    @Test
    void doesNotApplyInAnotherGuildClaim() {
        assertEquals(0, GuildClaimBonus.percent(knights, new GuildLand(UUID.randomUUID(), UUID.randomUUID(), "other"), 7));
    }

    @Test
    void doesNotApplyWithoutAGuildOrOutsideAnyGuildClaim() {
        assertEquals(0, GuildClaimBonus.percent(null, new GuildLand(UUID.randomUUID(), knights, "base"), 7));
        assertEquals(0, GuildClaimBonus.percent(knights, null, 7));
    }

    @Test
    void theActionLocationIsTheBlockThenTheMobThenThePlayer() {
        Location blockAt = mock(Location.class);
        Location mobAt = mock(Location.class);
        Location playerAt = mock(Location.class);
        Block block = mock(Block.class);
        when(block.getLocation()).thenReturn(blockAt);
        LivingEntity mob = mock(LivingEntity.class);
        when(mob.getLocation()).thenReturn(mobAt);
        Player player = mock(Player.class);
        when(player.getLocation()).thenReturn(playerAt);

        assertSame(blockAt, GuildClaimBonus.actionLocation(block, null, mob, player));
        assertSame(mobAt, GuildClaimBonus.actionLocation(null, null, mob, player));
        assertSame(playerAt, GuildClaimBonus.actionLocation(null, null, null, player));
    }
}
