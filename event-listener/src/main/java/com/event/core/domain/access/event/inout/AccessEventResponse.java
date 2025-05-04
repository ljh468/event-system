package com.event.core.domain.access.event.inout;

import com.event.infra.util.DateTimeUtils;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record AccessEventResponse(
    Long id,
    String categoryCode,
    String userId,
    String endpoint,
    String httpMethod,
    Integer responseStatus,
    Double responseTime,
    String ipAddress,
    String userAgent,
    OffsetDateTime createdAt
) {

  public static AccessEventResponse from(AccessEventDto dto) {
    return AccessEventResponse.builder()
                              .id(dto.id())
                              .categoryCode(dto.categoryCode())
                              .userId(dto.userId())
                              .endpoint(dto.endpoint())
                              .httpMethod(dto.httpMethod())
                              .responseStatus(dto.responseStatus())
                              .responseTime(dto.responseTime())
                              .ipAddress(dto.ipAddress())
                              .userAgent(dto.userAgent())
                              .createdAt(DateTimeUtils.parseToOffsetDateTime(dto.createdAt()))
                              .build();
  }
}