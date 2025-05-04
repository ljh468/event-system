package com.event.infra.event;

import java.util.Arrays;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum EventType {
  ACCESS;

  public static Optional<EventType> safeParse(String type) {
    return Arrays.stream(values())
        .filter(v -> v.name().equalsIgnoreCase(type))
        .findFirst();
  }
}
