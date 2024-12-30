package fr.iban.msboosts.model;

import fr.iban.msboosts.enums.BoostStatus;
import fr.iban.msboosts.enums.PauseReason;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class Boost {

    private UUID id;
    private UUID owner;
    private int percentage;
    private Instant startTime;
    private long duration;
    private long usedTime;
    private BoostStatus status = BoostStatus.WAITING_TO_START;
    private PauseReason pauseReason;

    public Boost() {}

    public Boost(UUID id, UUID owner, int percentage, long duration) {
        this.id = id;
        this.owner = owner;
        this.percentage = percentage;
        this.duration = duration;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public boolean isGlobal() {
        return owner.equals(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    }

    public boolean isActive() {
        return status == BoostStatus.ACTIVE;
    }

    public boolean isPaused() {
        return status == BoostStatus.PAUSED;
    }

    public long getRemainingTime() {
        if (startTime == null) {
            return duration - usedTime;
        }
        return (duration - usedTime) - Duration.between(startTime, Instant.now()).toMillis();
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public BoostStatus getStatus() {
        return status;
    }

    public void setStatus(BoostStatus status) {
        this.status = status;

        if(status != BoostStatus.PAUSED) {
            this.pauseReason = null;
        }
    }

    public void pauseBoost(PauseReason reason) {
        if(isActive()) {
            this.pauseReason = reason;
            this.status = BoostStatus.PAUSED;
            this.usedTime += Duration.between(startTime, Instant.now()).toMillis();
            this.startTime = null;
        }
    }

    public void resumeBoost() {
        if(isPaused()) {
            this.status = BoostStatus.ACTIVE;
            this.startTime = Instant.now();
            this.pauseReason = null;
        }
    }

    public boolean isExpired() {
        return status == BoostStatus.EXPIRED || getRemainingTime() <= 0;
    }

    public long getUsedTime() {
        return usedTime;
    }

    public void setUsedTime(long usedTime) {
        this.usedTime = usedTime;
    }

    public PauseReason getPauseReason() {
        return pauseReason;
    }

    public void setPauseReason(PauseReason pauseReason) {
        this.pauseReason = pauseReason;
    }
}