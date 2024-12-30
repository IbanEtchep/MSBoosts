package fr.iban.msboosts.api;

import fr.iban.msboosts.enums.PauseReason;
import fr.iban.msboosts.model.Boost;

import java.util.List;
import java.util.UUID;

public interface BoostManager {

    void addBoost(Boost boost);

    void removeBoost(Boost boost);

    /**
     * @param uuid - owner's UUID
     * @return a list of all personal boosts of the player
     */
    List<Boost> getBoosts(UUID uuid);

    List<Boost> getGlobalBoosts();

    List<Boost> getActiveBoosts(UUID uuid);

    List<Boost> getActiveGlobalBoosts();

    /**
     * @param uuid - owner's UUID
     * @return the total multiplier of all personal boosts
     */
    double getMultiplier(UUID uuid);

    /**
     * @return the total multiplier of all global boosts
     */
    double getGlobalMultiplier();

    /**
     * @param uuid - owner's UUID
     * @return the total multiplier of all boosts (global and personal)
     */
    double getTotalMultiplier(UUID uuid);


    /**
     * @param uuid - owner's UUID
     * @return the total percentage of all personal boosts
     */
    int getPercentage(UUID uuid);

    /**
     * @return the total percentage of all global boosts
     */
    int getGlobalPercentage();

    /**
     * @param uuid - owner's UUID
     * @return the total percentage of all boosts (global and personal)
     */
    int getTotalPercentage(UUID uuid);

    /**
     * @param uuid - player's UUID
     * @return active boost time left in seconds
     */
    long getBoostTimeLeft(UUID uuid);

    /**
     * @return global boost active time left in seconds
     */
    long getGlobalBoostTimeLeft();

    void loadBoosts(UUID uuid);

    void pauseBoost(Boost boost, PauseReason reason);

    void resumeBoost(Boost boost);

    void activateBoost(Boost boost);

    void expireBoost(Boost boost);

    void handleBoosts();
}