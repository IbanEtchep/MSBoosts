package fr.iban.msboosts.papi;

import fr.iban.msboosts.MSBoostsPlugin;
import fr.iban.msboosts.util.TimeFormatter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoostPlaceholderExpansion extends PlaceholderExpansion {

    private final MSBoostsPlugin plugin;

    public BoostPlaceholderExpansion(MSBoostsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "msboosts";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Iban";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {

        if(player == null) {
            return "";
        }

        if(params.equalsIgnoreCase("personal_percentage")) {
            return String.valueOf(plugin.getBoostManager().getPercentage(player.getUniqueId()));
        }

        if(params.equalsIgnoreCase("personal_time")) {
            return TimeFormatter.formatTime(plugin.getBoostManager().getBoostTimeLeft(player.getUniqueId()));
        }

        if(params.equalsIgnoreCase("global_percentage")) {
            return String.valueOf(plugin.getBoostManager().getGlobalPercentage());
        }

        if(params.equalsIgnoreCase("global_time")) {
            return TimeFormatter.formatTime(plugin.getBoostManager().getGlobalBoostTimeLeft());
        }

        if(params.equalsIgnoreCase("total_percentage")) {
            return String.valueOf(plugin.getBoostManager().getPercentage(player.getUniqueId()));
        }

        return null;
    }
}
