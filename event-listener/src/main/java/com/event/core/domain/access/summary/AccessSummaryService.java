package com.event.core.domain.access.summary;

import com.event.core.domain.access.category.AccessCategoryService;
import com.event.core.domain.access.category.inout.AccessCategoryDto;
import com.event.core.domain.access.event.AccessEventService;
import com.event.core.domain.access.event.inout.AccessEventDto;
import com.event.infra.util.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AccessSummaryService {

  private final AccessEventService accessEventService;

  private final AccessCategoryService accessCategoryService;

  @Autowired
  public AccessSummaryService(AccessEventService accessEventService,
                              AccessCategoryService accessCategoryService) {
    this.accessEventService = accessEventService;
    this.accessCategoryService = accessCategoryService;
  }

  public AccessSummary getAccessSummary(LocalDate date) {

    // 1. AccessCategory 조회
    List<AccessCategoryDto> categoryDtos = accessCategoryService.getAllCategoriesNotDeleted();
    OffsetDateTime startOfDay = DateTimeUtils.getStartOfDayKoreaOffset(date);
    OffsetDateTime endOfDay = DateTimeUtils.getEndOfDayKoreaOffset(date);

    // 2. AccessEvent 조회
    List<AccessEventDto> eventDtos = accessEventService.findByCreatedAtBetween(startOfDay, endOfDay);

    // 3. AccessEventDto를 categoryCode를 key로 하는 Map으로 변환
    Map<String, List<AccessEventDto>> eventsGroupedByCategory =
        eventDtos.stream().collect(Collectors.groupingBy(AccessEventDto::categoryCode));

    // 4. 각 AccessCategory를 순회하며 이벤트 데이터를 연결하고 AccessEventSummary 객체 생성
    List<AccessEventSummary> accessEventSummaries =
        categoryDtos.stream()
                    .map(category -> AccessEventSummary.builder()
                                                       .categoryCode(category.categoryCode())
                                                       .accessEvents(eventsGroupedByCategory.getOrDefault(category.categoryCode(),
                                                                                                    new ArrayList<>()))
                                                       .build())
                    .collect(Collectors.toList());

    // 5. AccessSummary 생성 및 반환
    return AccessSummary.builder()
                        .accessDate(date)
                        .totalAccessCount(eventDtos.size())
                        .accessEventSummaries(accessEventSummaries)
                        .build();

  }
}
