package com.event.infra.event.handler;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.event.core.domain.access.category.AccessCategoryService;
import com.event.core.domain.access.category.entity.AccessCategory;
import com.event.core.domain.access.category.inout.AccessCategoryDto;
import com.event.core.domain.access.event.AccessEventService;
import com.event.core.domain.access.event.inout.AccessEventDto;
import com.event.infra.util.JsonParser;
import com.event.infra.util.JsonParsingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.MapRecord;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class AccessEventHandlerTest {

  @Mock
  private JsonParser jsonParser;

  @Mock
  private AccessCategoryService accessCategoryService;

  @Mock
  private AccessEventService accessEventService;

  @Captor
  private ArgumentCaptor<AccessCategory> accessCategoryCaptor;

  @InjectMocks
  private AccessEventHandler handler;

  private static final String DUMMY_STREAM_KEY = "EVENT_STREAM";
  private static final String DUMMY_CATEGORY_CODE = "API_CATEGORY_CODE";
  private static final String INVALID_CATEGORY_CODE = "INVALID_CATEGORY_CODE";
  private static final String DUMMY_USER_ID = "user123";
  private static final String DUMMY_ENDPOINT = "/api/test";
  private static final String DUMMY_METHOD = "GET";
  private static final int DUMMY_STATUS = 200;
  private static final double DUMMY_RESPONSE_TIME = 100.5;
  private static final String DUMMY_IP = "127.0.0.1";
  private static final String DUMMY_USER_AGENT = "PostmanRuntime";
  private static final String DUMMY_INPUTS = "{\"key\": \"value\"}";
  private static final String DUMMY_OUTPUTS = "{\"result\": \"success\"}";
  private static final String DUMMY_CREATED_AT = "2025-05-18T12:34:56+09:00";

  @Test
  void handleEvent_성공적으로_저장된다() {
    // given
    Map<String, String> valueMap = new HashMap<>();
    valueMap.put("categoryCode", DUMMY_CATEGORY_CODE);
    valueMap.put("userId", DUMMY_USER_ID);
    valueMap.put("endpoint", DUMMY_ENDPOINT);
    valueMap.put("httpMethod", DUMMY_METHOD);
    valueMap.put("responseStatus", String.valueOf(DUMMY_STATUS));
    valueMap.put("responseTime", String.valueOf(DUMMY_RESPONSE_TIME));
    valueMap.put("ipAddress", DUMMY_IP);
    valueMap.put("userAgent", DUMMY_USER_AGENT);
    valueMap.put("inputs", DUMMY_INPUTS);
    valueMap.put("outputs", DUMMY_OUTPUTS);
    valueMap.put("createdAt", DUMMY_CREATED_AT);

    MapRecord<String, String, String> record = MapRecord.create(DUMMY_STREAM_KEY, valueMap);

    AccessEventDto accessEventDto = AccessEventDto.builder()
                                                  .categoryCode(DUMMY_CATEGORY_CODE)
                                                  .userId(DUMMY_USER_ID)
                                                  .endpoint(DUMMY_ENDPOINT)
                                                  .httpMethod(DUMMY_METHOD)
                                                  .responseStatus(DUMMY_STATUS)
                                                  .responseTime(DUMMY_RESPONSE_TIME)
                                                  .ipAddress(DUMMY_IP)
                                                  .userAgent(DUMMY_USER_AGENT)
                                                  .inputs(DUMMY_INPUTS)
                                                  .outputs(DUMMY_OUTPUTS)
                                                  .createdAt(DUMMY_CREATED_AT)
                                                  .build();

    AccessCategoryDto accessCategoryDto = AccessCategoryDto.builder()
                                                           .categoryCode(DUMMY_CATEGORY_CODE)
                                                           .name("API Category")
                                                           .description("API category description")
                                                           .build();

    // when
    when(jsonParser.parseCamel(valueMap, AccessEventDto.class)).thenReturn(accessEventDto);
    when(accessCategoryService.getByCategoryCodeNotDeleted(DUMMY_CATEGORY_CODE)).thenReturn(accessCategoryDto);

    // then
    assertDoesNotThrow(() -> handler.handleEvent(record));

    // ArgumentCaptor로 실제 호출된 값을 캡처
    verify(accessEventService, times(1)).create(
        accessCategoryCaptor.capture(),
        eq(DUMMY_USER_ID),
        eq(DUMMY_ENDPOINT),
        eq(DUMMY_METHOD),
        eq(DUMMY_STATUS),
        eq(DUMMY_RESPONSE_TIME),
        eq(DUMMY_IP),
        eq(DUMMY_USER_AGENT),
        eq(DUMMY_INPUTS),
        eq(DUMMY_OUTPUTS),
        eq(DUMMY_CREATED_AT)
    );

    AccessCategory capturedCategory = accessCategoryCaptor.getValue();
    assertAll(
        () -> assertEquals(DUMMY_CATEGORY_CODE, capturedCategory.getCategoryCode()),
        () -> assertEquals("API Category", capturedCategory.getName()),
        () -> assertEquals("API category description", capturedCategory.getDescription())
    );
  }

  @Test
  void handleEvent_카테고리가_존재하지_않는_경우() {
    // given
    Map<String, String> valueMap = new HashMap<>();
    valueMap.put("categoryCode", INVALID_CATEGORY_CODE);

    MapRecord<String, String, String> record = MapRecord.create(DUMMY_STREAM_KEY, valueMap);

    AccessEventDto accessEventDto = AccessEventDto.builder()
                                                  .categoryCode(INVALID_CATEGORY_CODE)
                                                  .build();

    // when
    when(jsonParser.parseCamel(valueMap, AccessEventDto.class)).thenReturn(accessEventDto);
    when(accessCategoryService.getByCategoryCodeNotDeleted(INVALID_CATEGORY_CODE))
        .thenThrow(new IllegalArgumentException("Category not found: " + INVALID_CATEGORY_CODE));

    // then
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                      () -> handler.handleEvent(record));

    assertEquals("Category not found: " + INVALID_CATEGORY_CODE, exception.getMessage());
    verify(accessEventService, never()).create(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void handleEvent_파싱에_실패하면_예외발생() {
    // given
    Map<String, String> valueMap = new HashMap<>();
    valueMap.put("categoryCode", DUMMY_CATEGORY_CODE);

    MapRecord<String, String, String> record = MapRecord.create(DUMMY_STREAM_KEY, valueMap);

    // when
    when(jsonParser.parseCamel(anyMap(), eq(AccessEventDto.class)))
        .thenThrow(new JsonParsingException("Failed to parse Map to class"));

    // then
    JsonParsingException exception = assertThrows(JsonParsingException.class,
                                                  () -> handler.handleEvent(record));

    assertEquals("Failed to parse Map to class", exception.getMessage());
    verify(accessEventService, never()).create(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
  }
}