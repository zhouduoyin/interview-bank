import { ElMessage } from 'element-plus'

const BASE = (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/$/, '')

function toQuery(params) {
  if (!params) return ''
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value === null || value === undefined || value === '') return
    if (Array.isArray(value)) {
      value.forEach((v) => search.append(key, v))
    } else {
      search.append(key, value)
    }
  })
  const q = search.toString()
  return q ? `?${q}` : ''
}

async function request(path, { method = 'GET', body, params, silent = false } = {}) {
  let res
  try {
    res = await fetch(BASE + path + toQuery(params), {
      method,
      headers: body !== undefined ? { 'Content-Type': 'application/json' } : undefined,
      body: body !== undefined ? JSON.stringify(body) : undefined
    })
  } catch (e) {
    if (!silent) ElMessage.error('无法连接后端服务，请确认 Spring Boot 已启动')
    throw e
  }
  const text = await res.text()
  let data = null
  if (text) {
    try {
      data = JSON.parse(text)
    } catch {
      data = text
    }
  }
  if (!res.ok) {
    const message = (data && data.message) || `请求失败（${res.status}）`
    if (!silent) ElMessage.error(message)
    const error = new Error(message)
    error.status = res.status
    throw error
  }
  return data
}

export const api = {
  dashboard: () => request('/stats/dashboard'),
  insights: () => request('/stats/insights'),
  categories: {
    list: () => request('/categories'),
    create: (body) => request('/categories', { method: 'POST', body }),
    update: (id, body) => request(`/categories/${id}`, { method: 'PUT', body }),
    remove: (id) => request(`/categories/${id}`, { method: 'DELETE' })
  },
  tags: {
    list: () => request('/tags'),
    rename: (from, to) => request('/tags/rename', { method: 'PUT', body: { from, to } }),
    remove: (name) => request(`/tags/${encodeURIComponent(name)}`, { method: 'DELETE' })
  },
  questions: {
    list: (params) => request('/questions', { params }),
    recent: (limit = 6) => request('/questions/recent', { params: { limit } }),
    get: (id) => request(`/questions/${id}`),
    create: (body) => request('/questions', { method: 'POST', body }),
    update: (id, body) => request(`/questions/${id}`, { method: 'PUT', body }),
    remove: (id) => request(`/questions/${id}`, { method: 'DELETE' }),
    star: (id) => request(`/questions/${id}/star`, { method: 'PATCH' }),
    mastery: (id, value) => request(`/questions/${id}/mastery`, { method: 'PATCH', params: { value } }),
    review: (id, rating) => request(`/questions/${id}/review`, { method: 'POST', body: { rating } }),
    reviews: (id) => request(`/questions/${id}/reviews`),
    interviews: (id) => request(`/questions/${id}/interviews`),
    batch: (body) => request('/questions/batch', { method: 'POST', body })
  },
  review: {
    queue: (limit = 20) => request('/review/queue', { params: { limit } })
  },
  interviews: {
    list: () => request('/interviews'),
    get: (id) => request(`/interviews/${id}`),
    create: (body) => request('/interviews', { method: 'POST', body }),
    update: (id, body) => request(`/interviews/${id}`, { method: 'PUT', body }),
    remove: (id) => request(`/interviews/${id}`, { method: 'DELETE' }),
    attach: (id, body) => request(`/interviews/${id}/questions`, { method: 'POST', body }),
    detach: (id, linkId) => request(`/interviews/${id}/questions/${linkId}`, { method: 'DELETE' })
  },
  data: {
    import: (body) => request('/data/import', { method: 'POST', body }),
    clearDemo: () => request('/data/demo', { method: 'DELETE' }),
    clearAll: () => request('/data/all', { method: 'DELETE' })
  }
}
