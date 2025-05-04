package com.event.core.domain.access.category.inout;

import com.event.core.domain.access.category.entity.AccessCategory;
import com.event.infra.util.DateTimeUtils;
import lombok.Builder;

@Builder
public record AccessCategoryDto(
    Long id,
    String categoryCode,
    String name,
    String description,
    boolean isDeleted,
    String createdAt,
    String updatedAt
) {

  public AccessCategory to() {
    return AccessCategory.builder()
                         .id(this.id)
                         .categoryCode(this.categoryCode)
                         .name(this.name)
                         .description(this.description)
                         .isDeleted(this.isDeleted)
                         .createdAt(DateTimeUtils.parseToOffsetDateTime(this.createdAt))
                         .updatedAt(DateTimeUtils.parseToOffsetDateTime(this.updatedAt))
                         .build();
  }

  public static AccessCategoryDto from(AccessCategory accessCategory) {
    return AccessCategoryDto.builder()
                            .id(accessCategory.getId())
                            .categoryCode(accessCategory.getCategoryCode())
                            .name(accessCategory.getName())
                            .description(accessCategory.getDescription())
                            .isDeleted(accessCategory.isDeleted())
                            .createdAt(DateTimeUtils.convertToKoreaTime(accessCategory.getCreatedAt()))
                            .updatedAt(DateTimeUtils.convertToKoreaTime(accessCategory.getUpdatedAt()))
                            .build();
  }
}