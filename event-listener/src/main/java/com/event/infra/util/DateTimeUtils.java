package com.event.infra.util;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Objects.isNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DateTimeUtils {

  private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
  private static final ZoneOffset UTC_OFFSET = ZoneOffset.UTC;

  // 정규식 상수: 공백 포함 오프셋/비오프셋
  public static final String REGEX_WITH_SPACE_OFFSET =
      "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{6} [+-]\\d{2}:\\d{2}$";

  private static final String REGEX_NO_OFFSET =
      "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{6}$";

  private static final DateTimeFormatter FORMAT_WITH_SPACE_OFFSET =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS xxx");

  private static final DateTimeFormatter FORMAT_NO_OFFSET =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

  private static final DateTimeFormatter FORMAT_LOCAL_DATE_TIME =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private static final DateTimeFormatter FORMAT_KOREA_DATE_TIME_OFFSET =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'+09:00'");

  private DateTimeUtils() {
  }

  /**
   * 다양한 형식의 날짜 문자열을 OffsetDateTime으로 파싱
   *
   * <pre>
   * 지원 포맷:
   * - "2024-05-21 07:07:08.076578 +00:00" (공백 + 오프셋)
   * - "2025-04-17 09:54:42.203927" (공백 + 오프셋 없음 → UTC 간주)
   * - "2025-04-10T17:45:00+09:00" (ISO + 오프셋)
   * - "2025-04-10T17:45:00" (ISO Local → UTC 간주)
   * - "2025-04-10T17:45:00Z" (ISO UTC Z 포맷)
   * </pre>
   *
   * @param dateTimeStr 문자열 형태의 날짜
   * @return OffsetDateTime 객체 또는 파싱 실패 시 null
   */
  public static OffsetDateTime parseToOffsetDateTime(String dateTimeStr) {
    if (isNull(dateTimeStr) || dateTimeStr.isBlank()) {
      return null;
    }

    try {
      // ISO 형식 (오프셋 또는 Z 포함)
      if (isZonedFormat(dateTimeStr) || isIsoOffsetFormat(dateTimeStr)) {
        // +0900 -> +09:00 형식으로 변환
        String formattedDateTime = dateTimeStr.replaceAll("([+-]\\d{2})(\\d{2})$", "$1:$2");
        return OffsetDateTime.parse(formattedDateTime, ISO_DATE_TIME);
      }

      // 공백 포함 + 오프셋 포함 (yyyy-MM-dd HH:mm:ss.SSSSSS X) - 수정된 형식
      if (dateTimeStr.contains(" ") && dateTimeStr.matches(REGEX_WITH_SPACE_OFFSET)) {
        return OffsetDateTime.parse(dateTimeStr, FORMAT_WITH_SPACE_OFFSET);
      }

      // 공백 포함 + 오프셋 없음 → UTC 기준으로 OffsetDateTime 생성
      if (dateTimeStr.matches(REGEX_NO_OFFSET)) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, FORMAT_NO_OFFSET);
        return localDateTime.atZone(UTC_OFFSET).toOffsetDateTime();
      }

      // ISO_LOCAL_DATE_TIME → UTC 기준 처리
      LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, ISO_LOCAL_DATE_TIME);
      return localDateTime.atZone(UTC_OFFSET).toOffsetDateTime();
    } catch (Exception exception) {
      log.error("Failed to parse dateTime: {}, {}", dateTimeStr, exception.getMessage());
      return null;
    }
  }

  /**
   * 문자열에 오프셋(+hh:mm / -hh:mm) 또는 'Z'가 포함되어 있는지 여부
   */
  private static boolean isZonedFormat(String str) {
    return str.endsWith("Z") || str.matches("^.*T.*[+-]\\d{2}:\\d{2}$");
  }

  /**
   * ISO_OFFSET_DATE_TIME 형식 검증용 예: 2025-04-10T17:45:00+09:00, 2025-04-10T17:45:00Z
   */
  private static boolean isIsoOffsetFormat(String str) {
    return str.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?([+-]\\d{4}|Z)$");
  }


  // KST 기준 시간 유틸들
  public static OffsetDateTime nowKoreaOffset() {
    return OffsetDateTime.now(KOREA_ZONE);
  }

  public static OffsetDateTime getStartOfDayKoreaOffset(LocalDate date) {
    return date.atStartOfDay(KOREA_ZONE).toOffsetDateTime();
  }

  public static OffsetDateTime getEndOfDayKoreaOffset(LocalDate date) {
    LocalDateTime endOfDay = date.atTime(23, 59, 59, 999999000);
    return endOfDay.atZone(KOREA_ZONE).toOffsetDateTime();
  }

  public static LocalDate nowKoreaLocalDate() {
    return LocalDate.now(KOREA_ZONE);
  }

  public static LocalDateTime nowKoreaLocalDateTime() {
    return LocalDateTime.now(KOREA_ZONE);
  }

  public static ZonedDateTime nowKoreaZonedDateTime() {
    return ZonedDateTime.now(KOREA_ZONE);
  }

  public static String convertToKoreaTime(OffsetDateTime offsetDateTime) {
    if (isNull(offsetDateTime)) {
      return null;
    }
    return offsetDateTime.atZoneSameInstant(KOREA_ZONE).format(FORMAT_KOREA_DATE_TIME_OFFSET);
  }
}