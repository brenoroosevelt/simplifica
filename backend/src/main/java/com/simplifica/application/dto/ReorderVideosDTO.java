package com.simplifica.application.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for reordering training videos.
 *
 * Contains a list of video IDs in the desired order.
 * The order in the list represents the new order_index (0-based).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderVideosDTO {

    @NotEmpty(message = "Video IDs list cannot be empty")
    private List<UUID> videoIds;
}
