package de.htw_berlin.KinderCareConnect.rest.model;

public record DailyStatsResponse(
    long pendingCount,
    long takenCount,
    long missedCount
) {
}
