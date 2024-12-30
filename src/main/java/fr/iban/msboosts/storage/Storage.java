package fr.iban.msboosts.storage;

import fr.iban.msboosts.model.Boost;

import java.util.List;
import java.util.UUID;

public interface Storage {

    void init();
    List<Boost> getBoosts(UUID uuid);
    void saveBoost(Boost boost);
    void removeBoost(Boost boost);
}
