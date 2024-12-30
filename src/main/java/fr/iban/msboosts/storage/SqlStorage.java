package fr.iban.msboosts.storage;

import fr.iban.common.data.sql.DbAccess;
import fr.iban.msboosts.enums.BoostStatus;
import fr.iban.msboosts.enums.PauseReason;
import fr.iban.msboosts.model.Boost;
import org.jdbi.v3.core.Jdbi;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class SqlStorage implements Storage {

    private final Jdbi jdbi;

    public SqlStorage() {
        this.jdbi = DbAccess.getJdbi();
    }

    @Override
    public void init() {
        jdbi.useHandle(handle -> handle.execute("""
        CREATE TABLE IF NOT EXISTS ms_boosts (
            id UUID PRIMARY KEY, 
            owner UUID, 
            percentage INTEGER,
            start_time TIMESTAMP DEFAULT NULL,
            duration BIGINT,
            used_time BIGINT DEFAULT 0,
            status VARCHAR(255),
            pause_reason VARCHAR(255)
        )"""));
    }

    @Override
    public List<Boost> getBoosts(UUID uuid) {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT * FROM ms_boosts WHERE owner = :owner")
                .bind("owner", uuid.toString())
                .registerColumnMapper(Instant.class, (rs, col, ctx) ->
                        rs.getTimestamp(col) != null ? rs.getTimestamp(col).toInstant() : null)
                .registerColumnMapper(BoostStatus.class, (rs, col, ctx) ->
                        rs.getString(col) != null ? BoostStatus.valueOf(rs.getString(col)) : null)
                .registerColumnMapper(PauseReason.class, (rs, col, ctx) ->
                        rs.getString(col) != null ? PauseReason.valueOf(rs.getString(col)) : null)
                .mapToBean(Boost.class)
                .list());
    }

    @Override
    public void saveBoost(Boost boost) {
        String sql = """
        INSERT INTO ms_boosts (id, owner, percentage, start_time, duration, used_time, status, pause_reason)
        VALUES (:id, :owner, :percentage, :start_time, :duration, :used_time, :status, :pause_reason)
        ON DUPLICATE KEY UPDATE
        owner = :owner,
        percentage = :percentage,
        start_time = :start_time,
        duration = :duration,
        used_time = :used_time,
        status = :status,
        pause_reason = :pause_reason
        """;

        Timestamp startTime = boost.getStartTime() != null ? new Timestamp(boost.getStartTime().toEpochMilli()) : null;

        jdbi.useHandle(handle -> handle.createUpdate(sql)
                .bind("id", boost.getId().toString())
                .bind("owner", boost.getOwner().toString())
                .bind("percentage", boost.getPercentage())
                .bind("start_time", startTime)
                .bind("duration", boost.getDuration())
                .bind("used_time", boost.getUsedTime())
                .bind("status", boost.getStatus().toString())
                .bind("pause_reason", boost.getPauseReason() != null ? boost.getPauseReason().toString() : null)
                .execute());
    }

    @Override
    public void removeBoost(Boost boost) {
        jdbi.useHandle(handle -> handle.execute("DELETE FROM ms_boosts WHERE id = ?", boost.getId().toString()));
    }
}
