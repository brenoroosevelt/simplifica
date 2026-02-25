package com.simplifica.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TrainingVideo entity representing a YouTube video within a training playlist.
 *
 * Each video belongs to a training and has an order position.
 * The order_index must be unique within a training to ensure proper playlist sequencing.
 */
@Entity
@Table(name = "training_videos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_id", nullable = false)
    private Training training;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "youtube_url", nullable = false, length = 512)
    private String youtubeUrl;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Sets timestamp before persisting a new entity.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Extracts YouTube video ID from URL.
     *
     * Supports formats:
     * - https://www.youtube.com/watch?v=VIDEO_ID
     * - https://youtu.be/VIDEO_ID
     * - https://www.youtube.com/embed/VIDEO_ID
     *
     * @return the YouTube video ID or null if not found
     */
    public String extractVideoId() {
        if (this.youtubeUrl == null || this.youtubeUrl.isEmpty()) {
            return null;
        }

        String url = this.youtubeUrl;

        // Standard watch URL: https://www.youtube.com/watch?v=VIDEO_ID
        if (url.contains("youtube.com/watch?v=")) {
            int startIndex = url.indexOf("v=") + 2;
            int endIndex = url.indexOf("&", startIndex);
            if (endIndex == -1) {
                return url.substring(startIndex);
            }
            return url.substring(startIndex, endIndex);
        }

        // Short URL: https://youtu.be/VIDEO_ID
        if (url.contains("youtu.be/")) {
            int startIndex = url.indexOf("youtu.be/") + 9;
            int endIndex = url.indexOf("?", startIndex);
            if (endIndex == -1) {
                return url.substring(startIndex);
            }
            return url.substring(startIndex, endIndex);
        }

        // Embed URL: https://www.youtube.com/embed/VIDEO_ID
        if (url.contains("youtube.com/embed/")) {
            int startIndex = url.indexOf("embed/") + 6;
            int endIndex = url.indexOf("?", startIndex);
            if (endIndex == -1) {
                return url.substring(startIndex);
            }
            return url.substring(startIndex, endIndex);
        }

        return null;
    }
}
