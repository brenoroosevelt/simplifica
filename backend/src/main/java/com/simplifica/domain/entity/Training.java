package com.simplifica.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Training entity representing an institutional training/course.
 *
 * Each training belongs to a specific institution (tenant) and contains
 * a playlist of YouTube videos, a cover image, and descriptive information.
 * Videos are ordered and can be managed independently.
 */
@Entity
@Table(name = "trainings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "cover_image_url", length = 1024)
    private String coverImageUrl;

    @Column(name = "cover_image_thumbnail_url", length = 1024)
    private String coverImageThumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "training_type", nullable = false, length = 20)
    @Builder.Default
    private TrainingType trainingType = TrainingType.VIDEO_SEQUENCE;

    @Column(name = "external_link", length = 1024)
    private String externalLink;

    @OneToMany(mappedBy = "training", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TrainingVideo> videos = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Sets timestamps before persisting a new entity.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the updatedAt timestamp before updating the entity.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Adds a video to this training.
     *
     * @param video the video to add
     */
    public void addVideo(TrainingVideo video) {
        this.videos.add(video);
        video.setTraining(this);
    }

    /**
     * Removes a video from this training.
     *
     * @param video the video to remove
     */
    public void removeVideo(TrainingVideo video) {
        this.videos.remove(video);
        video.setTraining(null);
    }

    /**
     * Gets total count of videos in this training.
     *
     * @return number of videos
     */
    public int getVideoCount() {
        return this.videos != null ? this.videos.size() : 0;
    }

    /**
     * Gets total duration of all videos in minutes.
     *
     * @return total duration in minutes
     */
    public int getTotalDurationMinutes() {
        if (this.videos == null) {
            return 0;
        }
        return this.videos.stream()
                .filter(v -> v.getDurationMinutes() != null)
                .mapToInt(TrainingVideo::getDurationMinutes)
                .sum();
    }

    /**
     * Sets both cover image URLs.
     *
     * @param coverImageUrl the cover image URL
     * @param thumbnailUrl the thumbnail URL
     */
    public void setImageUrls(String coverImageUrl, String thumbnailUrl) {
        this.coverImageUrl = coverImageUrl;
        this.coverImageThumbnailUrl = thumbnailUrl;
    }
}
