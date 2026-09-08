import { createRouter, createWebHashHistory } from 'vue-router'
import HomePage from '../views/home-page.vue'
import ArticlePage from '../views/article-page.vue'
import ArchivePage from '../views/archive-page.vue'
import MessagePage from '../views/message-page.vue'
import AboutPage from '../views/about-page.vue'
import GamePage from '../views/game-page.vue'
import StudioPage from '../views/studio-page.vue'
import LoginPage from '../views/login-page.vue'
import RegisterPage from '../views/register-page.vue'
import TimelinePage from '../views/timeline-page.vue'
import InterestsPage from '../views/interests-page.vue'
import FriendsPage from '../views/friends-page.vue'
import PrivatePage from '../views/private-page.vue'
import SettingsPage from '../views/settings-page.vue'
import AnalyticsPage from '../views/analytics-page.vue'
import ContentManagementPage from '../views/content-management-page.vue'
const router=createRouter({history:createWebHashHistory(),routes:[
{path:'/',name:'homePage',component:HomePage},{path:'/article/:id',name:'articleDetailPage',component:ArticlePage},{path:'/archive',name:'articleArchivePage',component:ArchivePage},{path:'/messages',name:'messageBoardPage',component:MessagePage},{path:'/about',name:'personalProfilePage',component:AboutPage},{path:'/game',name:'miniGamePage',component:GamePage},{path:'/studio',name:'authorStudioPage',component:StudioPage,meta:{requiresAuthor:true}},{path:'/login',name:'loginPage',component:LoginPage},{path:'/register',name:'registerUser',component:RegisterPage},{path:'/timeline',name:'personalTimelinePage',component:TimelinePage},{path:'/interests',name:'personalInterestsPage',component:InterestsPage},{path:'/friends',name:'friendLinksPage',component:FriendsPage},{path:'/private',name:'privateContentPage',component:PrivatePage},{path:'/settings',name:'accountSettingsPage',component:SettingsPage,meta:{requiresLogin:true}},{path:'/analytics',name:'readingAnalyticsPage',component:AnalyticsPage,meta:{requiresAuthor:true}}
,{path:'/studio/content',name:'contentManagementPage',component:ContentManagementPage,meta:{requiresAuthor:true}}
]})
router.beforeEach(to=>{
  const token=localStorage.getItem('myblog-token')
  const isEditor=['author','admin'].includes(localStorage.getItem('myblog-user-role'))
  if(to.name==='loginPage'||to.name==='registerUser') return true
  if(!token) return {path:'/login',query:{redirect:to.fullPath}}
  if((to.meta.requiresAuthor||to.name==='contentManagementPage')&&!isEditor) return '/'
  return true
})
export default router
