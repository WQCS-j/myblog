import { databasePool } from '../config/database.js'
import { successResponse } from '../utils/response.js'
export async function getProfile(request,response,next){try{const [rows]=await databasePool.query('SELECT * FROM personal_profile ORDER BY id LIMIT 1');response.json(successResponse(rows[0]||null))}catch(error){next(error)}}
export async function getTimeline(request,response,next){try{const [rows]=await databasePool.query('SELECT id,event_date eventDate,title,content FROM personal_timeline ORDER BY event_date DESC');response.json(successResponse(rows))}catch(error){next(error)}}
export async function getInterests(request,response,next){try{const [rows]=await databasePool.query('SELECT id,category,title,summary,link FROM personal_interests ORDER BY category,id');response.json(successResponse(rows))}catch(error){next(error)}}
export async function getFriendLinks(request,response,next){try{const [rows]=await databasePool.query('SELECT id,name,description,url FROM friend_links ORDER BY id');response.json(successResponse(rows))}catch(error){next(error)}}
