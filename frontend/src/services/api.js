const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:3000/api'
export async function requestApi(path, options = {}) {
  const token = localStorage.getItem('myblog-token')
  const response = await fetch(`${apiBaseUrl}${path}`, { headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}), ...(options.headers || {}) }, ...options })
  const result = await response.json().catch(() => ({ success: false, message: '服务器返回格式错误' }))
  if (response.status === 401) { localStorage.removeItem('myblog-token'); localStorage.removeItem('myblog-user-role'); window.dispatchEvent(new Event('myblog-auth-changed')) }
  if (!response.ok || result.success === false) throw new Error(result.message || '服务暂时不可用')
  return result
}
export async function loginUser(account, password) { return requestApi('/auth/login', { method: 'POST', body: JSON.stringify({ account, password }) }) }
export async function getDrafts(keyword = '') { return requestApi(`/drafts?keyword=${encodeURIComponent(keyword)}`) }
export async function createDraft() { return requestApi('/drafts', { method: 'POST', body: JSON.stringify({ title: '', summary: '', content: '', riskInfo: {} }) }) }
export async function updateDraft(id, draft) { return requestApi(`/drafts/${id}`, { method: 'PUT', body: JSON.stringify(draft) }) }
export async function deleteDraft(id) { return requestApi(`/drafts/${id}`, { method: 'DELETE' }) }
export async function publishDraft(id) { return requestApi(`/drafts/${id}/publish`, { method: 'POST' }) }
