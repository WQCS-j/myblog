import { Router } from 'express'
import { changePassword, getCurrentUser, loginUser, registerUser } from '../controllers/auth-controller.js'
import { getArticleById, getArticles, getArchive, getCategories, getTags } from '../controllers/article-controller.js'
import { createDraft, deleteDraft, getDraftById, getDrafts, publishDraft, updateDraft } from '../controllers/draft-controller.js'
import { accessPrivateContent } from '../controllers/private-controller.js'
import { getReadingHeatmap, recordReadingData } from '../controllers/reading-controller.js'
import { getFriendLinks, getInterests, getProfile, getTimeline } from '../controllers/profile-controller.js'
import { createMessage, createReply, getLatestReplies, getMessages, getReplies } from '../controllers/message-controller.js'
import { authenticateUser, requireRole } from '../middleware/auth.js'
const router=Router()
router.post('/auth/register',registerUser);router.post('/auth/change-password',authenticateUser,changePassword);router.post('/auth/login',loginUser);router.post('/auth/logout',(_,res)=>res.json({success:true,message:'已退出登录'}));router.get('/auth/me',authenticateUser,getCurrentUser)
router.get('/articles',getArticles);router.get('/articles/search',getArticles);router.get('/articles/:id',getArticleById);router.get('/categories',getCategories);router.get('/tags',getTags);router.get('/archive',getArchive)
router.post('/private-content',accessPrivateContent);router.post('/reading-data',recordReadingData);router.get('/reading-heatmap',authenticateUser,requireRole('author','admin'),getReadingHeatmap);router.get('/profile',getProfile);router.get('/timeline',getTimeline);router.get('/interests',getInterests);router.get('/friend-links',getFriendLinks)
router.get('/messages',getMessages);router.post('/messages',createMessage);router.get('/messages/:id/replies',getReplies);router.post('/messages/:id/replies',createReply);router.get('/replies/latest',getLatestReplies)
router.get('/drafts',authenticateUser,requireRole('author','admin'),getDrafts);router.post('/drafts',authenticateUser,requireRole('author','admin'),createDraft);router.get('/drafts/:id',authenticateUser,requireRole('author','admin'),getDraftById);router.put('/drafts/:id',authenticateUser,requireRole('author','admin'),updateDraft);router.delete('/drafts/:id',authenticateUser,requireRole('author','admin'),deleteDraft);router.post('/drafts/:id/publish',authenticateUser,requireRole('author','admin'),publishDraft)
router.get('/health',(_,res)=>res.json({success:true,data:{service:'myblog-api',status:'ok'}}));export default router



