package com.event.infra.event.handler;

import org.springframework.data.redis.connection.stream.MapRecord;

public interface EventHandler {

  void handleEvent(MapRecord<String, String, String> record);
}