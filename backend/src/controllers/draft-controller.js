import { z } from 'zod'
import { databasePool } from '../config/database.js'
import { errorResponse, successResponse } from '../utils/response.js'

const draftSchema = z.object({
  title: z.string().max(200).default(''),
  summary: z.string().max(2000).optional().default(''),
  content: z.string().max(100000).optional().default(''),
  categoryId: z.number().int().positive().nullable().optional(),
  riskInfo: z.record(z.any()).optional().default({})
})

function normalizeDraft(row) {
  return { id: row.id, title: row.title, summary: row.summary || '', content: row.content || '', categoryId: row.category_id, status: row.status, riskInfo: row.risk_info || {}, createdAt: row.created_at, updatedAt: row.updated_at }
}

export async function getDrafts(request, response, next) {
  try {
    const keyword = String(request.query.keyword || '').trim()
    const order = request.query.order === 'createdAt' ? 'created_at' : 'updated_at'
    const [rows] = await databasePool.query(`SELECT id,title,summary,content,category_id,status,risk_info,created_at,updated_at FROM drafts WHERE author_id = ? AND status = 'draft' AND (title LIKE ? OR summary LIKE ?) ORDER BY ${order} DESC`, [request.user.id, `%${keyword}%`, `%${keyword}%`])
    response.json(successResponse(rows.map(normalizeDraft)))
  } catch (error) { next(error) }
}

export async function createDraft(request, response, next) {
  try {
    const input = draftSchema.parse(request.body || {})
    const [result] = await databasePool.query('INSERT INTO drafts (title,summary,content,author_id,category_id,status,risk_info) VALUES (?,?,?,?,?,?,?)', [input.title, input.summary, input.content, request.user.id, input.categoryId || null, 'draft', JSON.stringify(input.riskInfo)])
    const [rows] = await databasePool.query('SELECT id,title,summary,content,category_id,status,risk_info,created_at,updated_at FROM drafts WHERE id = ? AND author_id = ?', [result.insertId, request.user.id])
    response.status(201).json(successResponse(normalizeDraft(rows[0]), '草稿已创建'))
  } catch (error) { next(error) }
}

export async function getDraftById(request, response, next) {
  try {
    const [rows] = await databasePool.query('SELECT id,title,summary,content,category_id,status,risk_info,created_at,updated_at FROM drafts WHERE id = ? AND author_id = ?', [request.params.id, request.user.id])
    if (!rows[0]) return response.status(404).json(errorResponse('草稿不存在或无权访问'))
    response.json(successResponse(normalizeDraft(rows[0])))
  } catch (error) { next(error) }
}

export async function updateDraft(request, response, next) {
  try {
    const input = draftSchema.parse(request.body || {})
    const [result] = await databasePool.query('UPDATE drafts SET title = ?, summary = ?, content = ?, category_id = ?, risk_info = ? WHERE id = ? AND author_id = ? AND status = \'draft\'', [input.title, input.summary, input.content, input.categoryId || null, JSON.stringify(input.riskInfo), request.params.id, request.user.id])
    if (!result.affectedRows) return response.status(404).json(errorResponse('草稿不存在或无权修改'))
    const [rows] = await databasePool.query('SELECT id,title,summary,content,category_id,status,risk_info,created_at,updated_at FROM drafts WHERE id = ? AND author_id = ?', [request.params.id, request.user.id])
    response.json(successResponse(normalizeDraft(rows[0]), '草稿已保存'))
  } catch (error) { next(error) }
}

export async function deleteDraft(request, response, next) {
  try {
    const [result] = await databasePool.query('DELETE FROM drafts WHERE id = ? AND author_id = ? AND status = \'draft\'', [request.params.id, request.user.id])
    if (!result.affectedRows) return response.status(404).json(errorResponse('草稿不存在或无权删除'))
    response.json(successResponse(null, '草稿已删除'))
  } catch (error) { next(error) }
}

export async function publishDraft(request, response, next) {
  const connection = await databasePool.getConnection()
  try {
    const [draftRows] = await connection.query('SELECT * FROM drafts WHERE id = ? AND author_id = ? AND status = \'draft\' FOR UPDATE', [request.params.id, request.user.id])
    const draft = draftRows[0]
    if (!draft) return response.status(404).json(errorResponse('草稿不存在或无权发布'))
    if (draft.title.trim().length < 2 || !draft.content.trim()) return response.status(400).json(errorResponse('发布前请填写标题和正文'))
    await connection.beginTransaction()
    const slug = `${draft.title.trim().toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '') || 'article'}-${Date.now()}`
    const [articleResult] = await connection.query('INSERT INTO articles (title,slug,summary,content,author_id,category_id,status,published_at) VALUES (?,?,?,?,?,?,?,NOW())', [draft.title, slug, draft.summary, draft.content, request.user.id, draft.category_id, 'published'])
    await connection.query('UPDATE drafts SET status = \'published\' WHERE id = ? AND author_id = ?', [draft.id, request.user.id])
    await connection.commit()
    response.status(201).json(successResponse({ articleId: articleResult.insertId }, '文章已发布'))
  } catch (error) { await connection.rollback(); next(error) } finally { connection.release() }
}
