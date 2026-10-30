package fr.iban.msboosts;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigUpdateTest {

    @Test
    void updateKeepsTheGuildClaimBonusSectionTheDefaultsOnlyComment() throws IOException {
        InputStream onDisk = new ByteArrayInputStream("""
                config-version: 1
                language: fr_FR
                guild-claim-bonus:
                  levels:
                    1: { jobs-xp: 2 }
                """.getBytes(StandardCharsets.UTF_8));
        InputStream defaults = Objects.requireNonNull(getClass().getResourceAsStream("/config.yml"));

        YamlDocument config = YamlDocument.create(onDisk, defaults,
                GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.OBJECT).build(),
                LoaderSettings.DEFAULT, DumperSettings.DEFAULT, ConfigUpdate.SETTINGS);
        config.update();

        YamlDocument reloaded = YamlDocument.create(
                new ByteArrayInputStream(config.dump().getBytes(StandardCharsets.UTF_8)));
        assertEquals(2, reloaded.getInt("guild-claim-bonus.levels.1.jobs-xp"));
        assertEquals("fr_FR", reloaded.getString("language"));
    }
}
