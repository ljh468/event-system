package com.event.infra.event.monitoring;

public interface RunnableMonitor {

  void start();

  void stop();

  boolean isMonitoring();
}
