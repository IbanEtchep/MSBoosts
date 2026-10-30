package fr.iban.msboosts;

import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

final class ConfigUpdate {

    /**
     * Keeps the keys the bundled config.yml does not have: optional sections that ship only as
     * comments ({@code guild-claim-bonus}) would otherwise be erased by {@code update()} on every start.
     */
    static final UpdaterSettings SETTINGS = UpdaterSettings.builder().setKeepAll(true).build();

    private ConfigUpdate() {
    }
}
