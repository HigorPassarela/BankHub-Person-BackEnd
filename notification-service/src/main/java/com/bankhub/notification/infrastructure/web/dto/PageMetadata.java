package com.bankhub.notification.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pagination metadata")
public record PageMetadata(
        @Schema(description = "Total number of elements across all pages", example = "125")
        Long totalElements,

        @Schema(description = "Total number of pages", example = "7")
        Integer totalPages,

        @Schema(description = "Current page number (0-indexed)", example = "0")
        Integer currentPage,

        @Schema(description = "Number of elements per page", example = "20")
        Integer pageSize
) {
}
