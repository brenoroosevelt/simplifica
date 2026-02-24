import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'

// Limpar valores inválidos do localStorage na inicialização
const cleanupLocalStorage = () => {
  const institutionId = localStorage.getItem('active_institution_id')
  if (institutionId === 'undefined' || institutionId === 'null' || !institutionId) {
    localStorage.removeItem('active_institution_id')
  }
}

// Executar limpeza imediatamente
cleanupLocalStorage()

const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor - adicionar token de autenticação e instituição ativa
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('auth_token')

    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // Adicionar header X-Institution-Id se houver instituição ativa
    const institutionId = localStorage.getItem('active_institution_id')

    // Limpar valores inválidos do localStorage
    if (institutionId === 'undefined' || institutionId === 'null') {
      localStorage.removeItem('active_institution_id')
    } else if (institutionId && config.headers) {
      config.headers['X-Institution-Id'] = institutionId
    }

    // Log detalhado para debug (remover depois)
    if (config.url?.includes('/trainings') && config.method === 'post') {
      console.log('=== REQUEST DEBUG ===')
      console.log('URL:', config.url)
      console.log('Method:', config.method)
      console.log('Headers:', JSON.stringify(config.headers, null, 2))
      console.log('Data:', JSON.stringify(config.data, null, 2))
      console.log('====================')
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor - tratamento de erros
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    return response
  },
  (error) => {
    // 401 - Token inválido ou expirado
    if (error.response?.status === 401) {
      localStorage.removeItem('auth_token')
      window.location.href = '/login'
    }

    // 403 - Acesso negado
    if (error.response?.status === 403) {
      console.error('Access denied:', error.response.data)
    }

    // Erros genéricos
    if (error.response?.status >= 500) {
      console.error('Server error:', error.response.data)
    }

    return Promise.reject(error)
  }
)

export default apiClient
