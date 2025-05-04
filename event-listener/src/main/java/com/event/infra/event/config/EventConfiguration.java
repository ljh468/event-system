package com.event.infra.event.config;

import com.event.infra.event.EventType;
import com.event.infra.event.handler.AccessEventHandler;
import com.event.infra.event.handler.EventHandler;
import com.event.infra.event.handler.EventHandlerResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class EventConfiguration {

  private final AccessEventHandler accessEventHandler;

  public EventConfiguration(AccessEventHandler accessEventHandler) {
    this.accessEventHandler = accessEventHandler;
  }

  @Bean
  public EventHandlerResolver eventHandlerResolver() {
    Map<EventType, EventHandler> eventHandlerMap = new HashMap<>();
    eventHandlerMap.put(EventType.ACCESS, accessEventHandler);
    return new EventHandlerResolver(eventHandlerMap);
  }
}
