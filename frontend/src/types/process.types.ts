export interface ProcessMapping {
  id: string
  processId: string
  fileUrl: string
  filename: string
  fileSize?: number
  uploadedAt: string
}

const ProcessDocumentationStatusValues = {
  DOCUMENTED: 'DOCUMENTED',
  NOT_DOCUMENTED: 'NOT_DOCUMENTED',
  DOCUMENTED_WITH_PENDING: 'DOCUMENTED_WITH_PENDING'
} as const

export type ProcessDocumentationStatus = typeof ProcessDocumentationStatusValues[keyof typeof ProcessDocumentationStatusValues]
export { ProcessDocumentationStatusValues as ProcessDocumentationStatus }

const ProcessExternalGuidanceStatusValues = {
  AVAILABLE: 'AVAILABLE',
  NOT_AVAILABLE: 'NOT_AVAILABLE',
  AVAILABLE_WITH_PENDING: 'AVAILABLE_WITH_PENDING',
  NOT_NECESSARY: 'NOT_NECESSARY'
} as const

export type ProcessExternalGuidanceStatus = typeof ProcessExternalGuidanceStatusValues[keyof typeof ProcessExternalGuidanceStatusValues]
export { ProcessExternalGuidanceStatusValues as ProcessExternalGuidanceStatus }

const ProcessRiskManagementStatusValues = {
  PREPARED: 'PREPARED',
  PREPARED_WITH_PENDING: 'PREPARED_WITH_PENDING',
  NOT_PREPARED: 'NOT_PREPARED'
} as const

export type ProcessRiskManagementStatus = typeof ProcessRiskManagementStatusValues[keyof typeof ProcessRiskManagementStatusValues]
export { ProcessRiskManagementStatusValues as ProcessRiskManagementStatus }

const ProcessMappingStatusValues = {
  MAPPED: 'MAPPED',
  NOT_MAPPED: 'NOT_MAPPED',
  MAPPED_WITH_PENDING: 'MAPPED_WITH_PENDING'
} as const

export type ProcessMappingStatus = typeof ProcessMappingStatusValues[keyof typeof ProcessMappingStatusValues]
export { ProcessMappingStatusValues as ProcessMappingStatus }

export interface Process {
  id: string
  institutionId: string
  institutionName: string
  institutionAcronym: string
  name: string
  valueChainId?: string
  valueChainName?: string
  responsibleUnitId?: string
  responsibleUnitName?: string
  responsibleUnitAcronym?: string
  directUnitId?: string
  directUnitName?: string
  directUnitAcronym?: string
  description?: string
  isCritical: boolean
  documentationStatus?: ProcessDocumentationStatus
  documentationUrl?: string
  externalGuidanceStatus?: ProcessExternalGuidanceStatus
  externalGuidanceUrl?: string
  riskManagementStatus?: ProcessRiskManagementStatus
  riskManagementUrl?: string
  mappingStatus?: ProcessMappingStatus
  mappings?: ProcessMapping[]
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface ProcessCreateRequest {
  name: string
  valueChainId?: string
  responsibleUnitId?: string
  directUnitId?: string
  description?: string
  isCritical?: boolean
  documentationStatus?: ProcessDocumentationStatus
  documentationUrl?: string
  externalGuidanceStatus?: ProcessExternalGuidanceStatus
  externalGuidanceUrl?: string
  riskManagementStatus?: ProcessRiskManagementStatus
  riskManagementUrl?: string
  mappingStatus?: ProcessMappingStatus
  active?: boolean
}

export interface ProcessUpdateRequest extends Partial<ProcessCreateRequest> {
  // All fields from ProcessCreateRequest are optional in update
}

export interface ProcessListParams {
  page: number
  size: number
  sort?: string
  direction?: 'asc' | 'desc'
  search?: string
  active?: boolean
  valueChainId?: string
  isCritical?: boolean
  // Advanced filters
  documentationStatus?: ProcessDocumentationStatus
  externalGuidanceStatus?: ProcessExternalGuidanceStatus
  riskManagementStatus?: ProcessRiskManagementStatus
  mappingStatus?: ProcessMappingStatus
  responsibleUnitId?: string
  directUnitId?: string
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
