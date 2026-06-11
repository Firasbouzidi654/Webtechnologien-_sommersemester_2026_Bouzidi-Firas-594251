package de.htw_berlin.KinderCareConnect.model;

public record DailyStatsResponse(
    long pendingCount,
    long takenCount,
    long missedCount
) {
}
