package com.event.core.domain.access.summary;

import com.event.core.domain.access.event.inout.AccessEventDto;
import lombok.Builder;

import java.util.List;

@Builder
public record AccessEventSummary(
    String categoryCode,
    List<AccessEventDto> accessEvents) {

}