package com.event.core.domain.access.event.inout;

import com.event.core.domain.access.event.entity.AccessEvent;
import com.event.infra.util.DateTimeUtils;
import lombok.Builder;

@Builder
public record AccessEventDto(
    Long id,
    String categoryCode,
    String userId,
    String endpoint,
    String httpMethod,
    Integer responseStatus,
    Double responseTime,
    String ipAddress,
    String userAgent,
    String inputs,
    String outputs,
    String createdAt
) {

  public AccessEvent to() {
    return AccessEvent.builder()
                      .id(this.id)
                      .categoryCode(this.categoryCode)
                      .userId(this.userId)
                      .endpoint(this.endpoint)
                      .httpMethod(this.httpMethod)
                      .responseStatus(this.responseStatus)
                      .responseTime(this.responseTime)
                      .ipAddress(this.ipAddress)
                      .userAgent(this.userAgent)
                      .createdAt(DateTimeUtils.parseToOffsetDateTime(this.createdAt()))
                      .build();
  }

  public static AccessEventDto from(AccessEvent accessEvent) {
    return AccessEventDto.builder()
                         .id(accessEvent.getId())
                         .categoryCode(accessEvent.getCategoryCode())
                         .userId(accessEvent.getUserId())
                         .endpoint(accessEvent.getEndpoint())
                         .httpMethod(accessEvent.getHttpMethod())
                         .responseStatus(accessEvent.getResponseStatus())
                         .responseTime(accessEvent.getResponseTime())
                         .ipAddress(accessEvent.getIpAddress())
                         .userAgent(accessEvent.getUserAgent())
                         .createdAt(DateTimeUtils.convertToKoreaTime(accessEvent.getCreatedAt()))
                         .build();
  }

}