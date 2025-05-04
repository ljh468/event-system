package com.event.core.domain.access.category.inout;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UpdateAccessCategoryRequest(
    @NotNull(message = "ID is required for update.")
    Long id,
    String name,
    String description,
    Boolean isDeleted
) {
}