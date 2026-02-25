export interface TrainingVideo {
  id: string
  trainingId?: string
  title: string
  youtubeUrl: string
  videoId?: string
  content?: string
  durationMinutes?: number
  orderIndex: number
  createdAt: string
  updatedAt?: string
}

export interface Training {
  id: string
  institutionId: string
  institutionName: string
  institutionAcronym: string
  title: string
  description?: string
  content?: string
  coverImageUrl?: string
  coverImageThumbnailUrl?: string
  videos: TrainingVideo[]
  videoCount: number
  totalDurationMinutes: number
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface TrainingVideoPayload {
  title: string
  youtubeUrl: string
  content?: string
  durationMinutes?: number
  orderIndex: number
}

export interface TrainingCreateRequest {
  title: string
  description?: string
  content?: string
  videos: TrainingVideoPayload[]
  active?: boolean
}

export interface TrainingUpdateRequest {
  title: string
  description?: string
  content?: string
  active?: boolean
}

export interface TrainingVideoCreateRequest {
  title: string
  youtubeUrl: string
  content?: string
  durationMinutes?: number
  orderIndex: number
}

export interface TrainingVideoUpdateRequest {
  title: string
  youtubeUrl: string
  content?: string
  durationMinutes?: number
}

export interface ReorderVideosRequest {
  videoIds: string[]
}

export interface TrainingListParams {
  page: number
  size: number
  sort?: string
  direction?: 'asc' | 'desc'
  search?: string
  active?: boolean
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}
