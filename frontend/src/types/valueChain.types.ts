export interface ValueChain {
  id: string
  institutionId: string
  institutionName: string
  institutionAcronym: string
  name: string
  description?: string
  imageUrl?: string
  imageThumbnailUrl?: string
  imageUploadedAt?: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface ValueChainCreateRequest {
  name: string
  description?: string
  active?: boolean
  image?: File
}

export interface ValueChainUpdateRequest {
  name?: string
  description?: string
  active?: boolean
  image?: File
}

export interface ValueChainListParams {
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
