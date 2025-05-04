package com.event.core.domain.access.category.inout;

import lombok.Builder;

@Builder
public record AccessCategoryResponse(
    Long id,
    String categoryCode,
    String name,
    String description,
    boolean isDeleted,
    String createdAt,
    String updatedAt
) {

  public static AccessCategoryResponse from(AccessCategoryDto dto) {
    return AccessCategoryResponse.builder()
                                 .id(dto.id())
                                 .categoryCode(dto.categoryCode())
                                 .name(dto.name())
                                 .description(dto.description())
                                 .isDeleted(dto.isDeleted())
                                 .createdAt(dto.createdAt())
                                 .updatedAt(dto.updatedAt())
                                 .build();
  }
}