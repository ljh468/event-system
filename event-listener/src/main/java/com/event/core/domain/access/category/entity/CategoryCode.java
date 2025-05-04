package com.event.core.domain.access.category.entity;

public enum CategoryCode {
  UNKNOWN,
  API_PUBLIC,
  API_ADMIN,
  API_EXTERNAL,
  API_INTERNAL,
  HEALTH_SYSTEM,
  SERVICE_QUEUE;

  public static String determineCategoryFromURI(String uri) {
    if (uri.startsWith("/admin")) {
      return API_ADMIN.name();
    }
    if (uri.startsWith("/external") || uri.startsWith("/external-api")) {
      return API_EXTERNAL.name();
    }
    if(uri.startsWith("/internal") || uri.startsWith("/internal-api")) {
      return API_INTERNAL.name();
    }
    if (uri.startsWith("/health")) {
      return HEALTH_SYSTEM.name();
    }
    if (uri.startsWith("/queue")) {
      return SERVICE_QUEUE.name();
    }
    if (uri.startsWith("/api")) {
      return API_PUBLIC.name();
    }
    return UNKNOWN.name();
  }
}