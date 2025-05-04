package com.event.core.domain.access.summary;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record AccessSummary(
    LocalDate accessDate,
    int totalAccessCount,
    List<AccessEventSummary> accessEventSummaries) {

}
