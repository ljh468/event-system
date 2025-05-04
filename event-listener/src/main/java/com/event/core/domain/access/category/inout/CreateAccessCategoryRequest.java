package com.event.core.domain.access.category.inout;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateAccessCategoryRequest(
    @NotNull @NotBlank(message = "Category code is required.")
    String categoryCode,

    @NotNull @NotBlank(message = "Name is required.")
    String name,

    String description
) {}