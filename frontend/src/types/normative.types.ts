export interface Normative {
  id: string
  institutionId: string
  institutionName: string
  institutionAcronym: string
  title: string
  description?: string
  fileUrl?: string
  fileOriginalName?: string
  externalLink?: string
  createdAt: string
  updatedAt: string
}

export interface NormativeCreateRequest {
  title: string
  description?: string
  externalLink?: string
  file?: File
}

export interface NormativeUpdateRequest {
  title?: string
  description?: string
  externalLink?: string
  file?: File
  removeFile?: boolean
}

export interface NormativeListParams {
  page: number
  size: number
  sort?: string
  direction?: 'asc' | 'desc'
  search?: string
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
