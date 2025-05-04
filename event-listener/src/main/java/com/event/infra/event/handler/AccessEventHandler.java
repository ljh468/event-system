package com.event.infra.event.handler;

import com.event.core.domain.access.category.AccessCategoryService;
import com.event.core.domain.access.category.inout.AccessCategoryDto;
import com.event.core.domain.access.event.AccessEventService;
import com.event.core.domain.access.event.inout.AccessEventDto;
import com.event.infra.util.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class AccessEventHandler implements EventHandler {

  private final JsonParser jsonParser;

  private final AccessCategoryService accessCategoryService;

  private final AccessEventService accessEventService;

  @Autowired
  public AccessEventHandler(JsonParser jsonParser,
                            AccessCategoryService accessCategoryService,
                            AccessEventService accessEventService) {
    this.jsonParser = jsonParser;
    this.accessCategoryService = accessCategoryService;
    this.accessEventService = accessEventService;
  }

  @Override
  @Transactional
  public void handleEvent(MapRecord<String, String, String> record) {
    AccessEventDto accessEventDto = jsonParser.parseCamel(record.getValue(), AccessEventDto.class);
    AccessCategoryDto accessCategoryDto = accessCategoryService.getByCategoryCodeNotDeleted(accessEventDto.categoryCode());

    accessEventService.create(accessCategoryDto.to(),
                              accessEventDto.userId(),
                              accessEventDto.endpoint(),
                              accessEventDto.httpMethod(),
                              accessEventDto.responseStatus(),
                              accessEventDto.responseTime(),
                              accessEventDto.ipAddress(),
                              accessEventDto.userAgent(),
                              accessEventDto.inputs(),
                              accessEventDto.outputs(),
                              accessEventDto.createdAt());

    log.info("saved AccessEvent. record={}", record);
  }
}