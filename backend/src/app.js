import express from 'express'
import cors from 'cors'
import rateLimit from 'express-rate-limit'
import 'dotenv/config'
import apiRoutes from './routes/api-routes.js'
const app = express()
app.use(cors({ origin: process.env.FRONTEND_URL?.split(',') || true }))
app.use(express.json({ limit: '1mb' }))
app.use(rateLimit({ windowMs: 15 * 60 * 1000, limit: 300, standardHeaders: true }))
app.use('/api', apiRoutes)
app.use((error, request, response, next) => { console.error(error); response.status(500).json({ success: false, message: '服务器内部错误' }) })
export default app
