const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:3000/api').replace(/\/+$/, '')

function clearLocalAuth() {
  localStorage.removeItem('myblog-token')
  localStorage.removeItem('myblog-user-role')
  window.dispatchEvent(new Event('myblog-auth-changed'))
}

async function parseApiResponse(response) {
  const text = await response.text()

  if (!text) {
    return { success: response.ok, data: null }
  }

  try {
    return JSON.parse(text)
  } catch {
    return { success: false, message: '服务器返回格式错误' }
  }
}

export async function requestApi(path, options = {}) {
  const token = localStorage.getItem('myblog-token')
  const headers = new Headers(options.headers || {})

  if (typeof options.body === 'string' && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  let response
  try {
    response = await fetch(`${apiBaseUrl}${path}`, { ...options, headers })
  } catch {
    throw new Error('无法连接到服务，请检查 Java 服务是否已启动以及 API 地址是否正确')
  }

  const result = await parseApiResponse(response)
  if (response.status === 401) clearLocalAuth()
  if (!response.ok || result.success === false) throw new Error(result.message || '服务暂时不可用')
  return result
}
export async function loginUser(account, password) { return requestApi('/auth/login', { method: 'POST', body: JSON.stringify({ account, password }) }) }
export async function getDrafts(keyword = '') { return requestApi(`/drafts?keyword=${encodeURIComponent(keyword)}`) }
export async function createDraft() { return requestApi('/drafts', { method: 'POST', body: JSON.stringify({ title: '', summary: '', content: '', riskInfo: {} }) }) }
export async function updateDraft(id, draft) { return requestApi(`/drafts/${id}`, { method: 'PUT', body: JSON.stringify(draft) }) }
export async function deleteDraft(id) { return requestApi(`/drafts/${id}`, { method: 'DELETE' }) }
export async function publishDraft(id) { return requestApi(`/drafts/${id}/publish`, { method: 'POST' }) }
export async function updateProfile(profile) { return requestApi('/profile', { method: 'PUT', body: JSON.stringify(profile) }) }
export async function createTimelineItem(item) { return requestApi('/timeline', { method: 'POST', body: JSON.stringify(item) }) }
export async function updateTimelineItem(id, item) { return requestApi(`/timeline/${id}`, { method: 'PUT', body: JSON.stringify(item) }) }
export async function deleteTimelineItem(id) { return requestApi(`/timeline/${id}`, { method: 'DELETE' }) }
export async function createInterest(item) { return requestApi('/interests', { method: 'POST', body: JSON.stringify(item) }) }
export async function updateInterest(id, item) { return requestApi(`/interests/${id}`, { method: 'PUT', body: JSON.stringify(item) }) }
export async function deleteInterest(id) { return requestApi(`/interests/${id}`, { method: 'DELETE' }) }
export async function createFriendLink(item) { return requestApi('/friend-links', { method: 'POST', body: JSON.stringify(item) }) }
export async function updateFriendLink(id, item) { return requestApi(`/friend-links/${id}`, { method: 'PUT', body: JSON.stringify(item) }) }
export async function deleteFriendLink(id) { return requestApi(`/friend-links/${id}`, { method: 'DELETE' }) }
export async function getPrivateSettings() { return requestApi('/private-content/settings') }
export async function updatePrivateSettings(settings) { return requestApi('/private-content/settings', { method: 'PUT', body: JSON.stringify(settings) }) }
