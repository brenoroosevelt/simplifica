-- ====================================
-- Training Videos Table Migration
-- ====================================
-- Creates the training_videos table for YouTube video playlists
-- Supports ordering and multiple videos per training
-- ====================================

CREATE TABLE training_videos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    training_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    youtube_url VARCHAR(512) NOT NULL,
    duration_minutes INTEGER,
    order_index INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_training_videos_training
        FOREIGN KEY (training_id)
        REFERENCES trainings(id)
        ON DELETE CASCADE,

    -- Unique constraint for order within a training
    CONSTRAINT uk_training_videos_order
        UNIQUE (training_id, order_index)
);

-- Create indexes for performance
CREATE INDEX idx_training_videos_training_id ON training_videos(training_id);
CREATE INDEX idx_training_videos_training_order ON training_videos(training_id, order_index);
CREATE INDEX idx_training_videos_created_at ON training_videos(created_at DESC);

-- Add comments for documentation
COMMENT ON TABLE training_videos IS 'Stores YouTube videos for training playlists';
COMMENT ON COLUMN training_videos.training_id IS 'Training that owns this video';
COMMENT ON COLUMN training_videos.title IS 'Video title (required, max 255 chars)';
COMMENT ON COLUMN training_videos.youtube_url IS 'YouTube video URL (required, max 512 chars)';
COMMENT ON COLUMN training_videos.duration_minutes IS 'Video duration in minutes (optional)';
COMMENT ON COLUMN training_videos.order_index IS 'Order position in playlist (required, unique per training)';
