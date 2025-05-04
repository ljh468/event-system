package com.event.infra.event.handler;

import com.event.infra.event.EventType;
import java.util.Map;

public class EventHandlerResolver {

  private final Map<EventType, EventHandler> handlerMap;

  public EventHandlerResolver(Map<EventType, EventHandler> handlerMap) {
    this.handlerMap = handlerMap;
  }

  public EventHandler getEventHandler(EventType eventType) {
    return handlerMap.get(eventType);
  }
}