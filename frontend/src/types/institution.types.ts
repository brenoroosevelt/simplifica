export enum InstitutionType {
  // eslint-disable-next-line no-unused-vars
  FEDERAL = 'FEDERAL',
  // eslint-disable-next-line no-unused-vars
  ESTADUAL = 'ESTADUAL',
  // eslint-disable-next-line no-unused-vars
  MUNICIPAL = 'MUNICIPAL',
  // eslint-disable-next-line no-unused-vars
  PRIVADA = 'PRIVADA',
}

export enum InstitutionStatus {
  // eslint-disable-next-line no-unused-vars
  ACTIVE = 'ACTIVE',
  // eslint-disable-next-line no-unused-vars
  INACTIVE = 'INACTIVE',
}

export enum UserInstitutionRole {
  // eslint-disable-next-line no-unused-vars
  ADMIN = 'ADMIN',
  // eslint-disable-next-line no-unused-vars
  MANAGER = 'MANAGER',
  // eslint-disable-next-line no-unused-vars
  VIEWER = 'VIEWER',
}

export interface Institution {
  id: string
  name: string
  acronym: string
  logoUrl?: string
  logoThumbnailUrl?: string
  type: InstitutionType
  domain?: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface InstitutionCreateRequest {
  name: string
  acronym: string
  type: InstitutionType
  domain?: string
  active?: boolean
  logo?: File
}

export interface InstitutionUpdateRequest {
  name?: string
  type?: InstitutionType
  domain?: string
  active?: boolean
  logo?: File
}

export interface UserInstitution {
  id: string
  userId: string
  institutionId: string
  institution: Institution
  roles: UserInstitutionRole[]
  active: boolean
  linkedAt: string
}

export interface InstitutionListParams {
  page: number
  size: number
  sort?: string
  direction?: 'asc' | 'desc'
  search?: string
  type?: InstitutionType
  active?: boolean
}

export interface AssignUserToInstitutionRequest {
  userId: string
  institutionId: string
  roles: UserInstitutionRole[]
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
