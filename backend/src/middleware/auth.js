import jwt from 'jsonwebtoken'
export function authenticateUser(request, response, next) { const token = request.headers.authorization?.replace('Bearer ', ''); if (!token) return response.status(401).json({ success: false, message: '请先登录' }); try { request.user = jwt.verify(token, process.env.JWT_SECRET); next() } catch { response.status(401).json({ success: false, message: '登录状态已过期' }) } }
export function requireRole(...roles) { return (request, response, next) => roles.includes(request.user?.role) ? next() : response.status(403).json({ success: false, message: '没有访问权限' }) }
