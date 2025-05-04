package com.event.core.domain.access.event;

import com.event.core.domain.access.category.entity.AccessCategory;
import com.event.core.domain.access.event.entity.AccessEvent;
import com.event.core.domain.access.event.inout.AccessEventDto;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

import static java.util.Objects.isNull;

@Service
public class AccessEventService {

  private final AccessEventRepository accessEventRepository;

  @Autowired
  public AccessEventService(AccessEventRepository accessEventRepository) {
    this.accessEventRepository = accessEventRepository;
  }

  public List<AccessEventDto> findByCreatedAtBetween(OffsetDateTime startDateTime,
                                                     OffsetDateTime endDateTime) {
    validateDateRange(startDateTime, endDateTime);
    return accessEventRepository.findByCreatedAtBetween(startDateTime, endDateTime).stream()
                                .map(AccessEventDto::from)
                                .toList();
  }

  public void create(AccessCategory accessCategory,
                     @NotBlank(message = "userId is null or empty") String userId,
                     @NotBlank(message = "endpoint is null or empty") String endpoint,
                     @NotBlank(message = "httpMethod is null or empty") String httpMethod,
                     @NotBlank(message = "responseStatus is null") Integer responseStatus,
                     @NotBlank(message = "responseTime is null") Double responseTime,
                     @NotBlank(message = "ipAddress is null or empty") String ipAddress,
                     String userAgent,
                     String inputs,
                     String outputs,
                     String createdAt) {

    AccessEvent newEvent = AccessEvent.newEvent(accessCategory,
                                              userId,
                                              endpoint,
                                              httpMethod,
                                              responseStatus,
                                              responseTime,
                                              ipAddress,
                                              userAgent,
                                              inputs,
                                              outputs,
                                              createdAt);
    accessEventRepository.save(newEvent);
  }

  private void validateDateRange(OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
    if (isNull(startDateTime) || isNull(endDateTime)) {
      throw new IllegalArgumentException("시작 날짜와 종료 날짜는 null일 수 없습니다.");
    }

    if (startDateTime.isAfter(endDateTime)) {
      throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이후일 수 없습니다.");
    }
  }
}
