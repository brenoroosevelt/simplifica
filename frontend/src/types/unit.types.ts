export interface Unit {
  id: string
  institutionId: string
  institutionName: string
  institutionAcronym: string
  name: string
  acronym: string
  description?: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface UnitCreateRequest {
  name: string
  acronym: string
  description?: string
  active?: boolean
}

export interface UnitUpdateRequest {
  name?: string
  description?: string
  active?: boolean
  // Note: acronym is intentionally NOT included (immutable after creation)
}

export interface UnitListParams {
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
