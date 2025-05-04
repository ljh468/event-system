package com.event.infra.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

class DateTimeUtilsTest {

  @Test
  @Order(1)
  void 공백_포함_오프셋_포함_형식을_OffsetDateTime으로_변환한다() {
    String input = "2024-04-09 05:17:54.802889 +09:00";
    OffsetDateTime result = DateTimeUtils.parseToOffsetDateTime(input);
    assertNotNull(result);
    assertEquals(2024, result.getYear());
    assertEquals(4, result.getMonthValue());
    assertEquals(9, result.getDayOfMonth());
    assertEquals(5, result.getHour());
    assertEquals(9 * 3600, result.getOffset().getTotalSeconds());
  }

  @Test
  @Order(2)
  void 공백_포함_오프셋_없는_형식을_OffsetDateTime으로_변환한다() {
    String input = "2025-04-17 09:54:42.203927";
    OffsetDateTime result = DateTimeUtils.parseToOffsetDateTime(input);
    assertNotNull(result);
    assertEquals(2025, result.getYear());
    assertEquals(4, result.getMonthValue());
    assertEquals(17, result.getDayOfMonth());
    assertEquals(9, result.getHour());
    assertEquals(0, result.getOffset().getTotalSeconds()); // UTC 기준
  }

  @Test
  @Order(3)
  void ISO_오프셋_포맷을_OffsetDateTime으로_변환한다() {
    String input = "2025-04-10T17:45:00+09:00";
    OffsetDateTime result = DateTimeUtils.parseToOffsetDateTime(input);
    assertNotNull(result);
    assertEquals(2025, result.getYear());
    assertEquals(4, result.getMonthValue());
    assertEquals(10, result.getDayOfMonth());
    assertEquals(17, result.getHour());
    assertEquals(9 * 3600, result.getOffset().getTotalSeconds());
  }

  @Test
  @Order(4)
  void ISO_Local_포맷을_OffsetDateTime으로_변환한다() {
    String input = "2025-04-10T17:45:00";
    OffsetDateTime result = DateTimeUtils.parseToOffsetDateTime(input);
    assertNotNull(result);
    assertEquals(2025, result.getYear());
    assertEquals(4, result.getMonthValue());
    assertEquals(10, result.getDayOfMonth());
    assertEquals(17, result.getHour());
    assertEquals(0, result.getOffset().getTotalSeconds()); // UTC 오프셋 적용
  }

  @Test
  @Order(5)
  void UTC_표기된_ISO_포맷을_OffsetDateTime으로_변환한다() {
    String input = "2025-04-10T17:45:00Z";
    OffsetDateTime result = DateTimeUtils.parseToOffsetDateTime(input);
    assertNotNull(result);
    assertEquals(2025, result.getYear());
    assertEquals(4, result.getMonthValue());
    assertEquals(10, result.getDayOfMonth());
    assertEquals(17, result.getHour());
    assertEquals(0, result.getOffset().getTotalSeconds());
  }

  @Test
  @Order(6)
  void 빈_문자열_입력_시_null을_반환한다() {
    assertNull(DateTimeUtils.parseToOffsetDateTime(""));
    assertNull(DateTimeUtils.parseToOffsetDateTime("   "));
  }

  @Test
  @Order(7)
  void null_입력_시_null을_반환한다() {
    assertNull(DateTimeUtils.parseToOffsetDateTime(null));
  }

  @Test
  @Order(8)
  void 잘못된_포맷_입력_시_null을_반환한다() {
    assertNull(DateTimeUtils.parseToOffsetDateTime("invalid-date-time"));
    assertNull(DateTimeUtils.parseToOffsetDateTime("2025/04/10 17:45"));
  }

  @Test
  @Order(9)
  void 밀리초_및_나노초_포함된_오프셋_포맷을_OffsetDateTime으로_변환한다() {
    String input = "2025-04-10T17:45:00.123456+09:00";
    OffsetDateTime result = DateTimeUtils.parseToOffsetDateTime(input);
    assertNotNull(result);
    assertEquals(2025, result.getYear());
    assertEquals(4, result.getMonthValue());
    assertEquals(10, result.getDayOfMonth());
    assertEquals(17, result.getHour());
    assertEquals(9 * 3600, result.getOffset().getTotalSeconds());
    assertEquals(123456000, result.getNano()); // 나노초
  }

  @Test
  @Order(10)
  void 나노초_포함된_시간_포맷을_OffsetDateTime으로_변환한다() {
    String input = "2025-04-10T17:45:00.123456789+09:00";
    OffsetDateTime result = DateTimeUtils.parseToOffsetDateTime(input);
    assertNotNull(result);
    assertEquals(2025, result.getYear());
    assertEquals(4, result.getMonthValue());
    assertEquals(10, result.getDayOfMonth());
    assertEquals(17, result.getHour());
    assertEquals(9 * 3600, result.getOffset().getTotalSeconds());
    assertEquals(123456789, result.getNano()); // 나노초
  }

  @Test
  @Order(11)
  void 시간대_표기_형식이_다른_오프셋_포맷을_OffsetDateTime으로_변환한다() {
    String input1 = "2025-04-10T17:45:00+0900"; // +hhmm
    OffsetDateTime result1 = DateTimeUtils.parseToOffsetDateTime(input1);
    assertNotNull(result1);
    assertEquals(2025, result1.getYear());
    assertEquals(4, result1.getMonthValue());
    assertEquals(10, result1.getDayOfMonth());
    assertEquals(17, result1.getHour());
    assertEquals(9 * 3600, result1.getOffset().getTotalSeconds());

    String input2 = "2025-04-10T17:45:00+00:00"; // +hh:mm
    OffsetDateTime result2 = DateTimeUtils.parseToOffsetDateTime(input2);
    assertNotNull(result2);
    assertEquals(2025, result2.getYear());
    assertEquals(4, result2.getMonthValue());
    assertEquals(10, result2.getDayOfMonth());
    assertEquals(17, result2.getHour());
    assertEquals(0, result2.getOffset().getTotalSeconds()); // UTC

    String input3 = "2025-04-10T17:45:00Z"; // Z
    OffsetDateTime result3 = DateTimeUtils.parseToOffsetDateTime(input3);
    assertNotNull(result3);
    assertEquals(2025, result3.getYear());
    assertEquals(4, result3.getMonthValue());
    assertEquals(10, result3.getDayOfMonth());
    assertEquals(17, result3.getHour());
    assertEquals(0, result3.getOffset().getTotalSeconds()); // UTC
  }
}