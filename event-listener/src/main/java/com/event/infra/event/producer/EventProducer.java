package com.event.infra.event.producer;

public interface EventProducer {

  void publishEvent(String type,
                    String categoryCode,
                    String userId,
                    String endpoint,
                    String httpMethod,
                    String methodName,
                    int responseStatus,
                    double responseTime,
                    String ipAddress,
                    String userAgent,
                    String inputs,
                    String outputs);
}
