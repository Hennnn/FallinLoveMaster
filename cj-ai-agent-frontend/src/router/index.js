import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ChatView from '../views/ChatView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: {
      title: '主页 - CJ AI Agent',
      description: '一站式切换 AI 情感大师与 AI 超级智能体应用。'
    }
  },
  {
    path: '/love-chat',
    name: 'love-chat',
    component: ChatView,
    meta: {
      title: 'AI 情感大师 - CJ AI Agent',
      description: 'AI 情感大师聊天室，支持会话 ID 与实时流式回复。'
    },
    props: {
      title: 'AI 情感大师',
      endpoint: '/ai/love_app/chat/sse',
      withChatId: true
    }
  },
  {
    path: '/manus-chat',
    name: 'manus-chat',
    component: ChatView,
    meta: {
      title: 'AI 超级智能体 - CJ AI Agent',
      description: 'AI 超级智能体聊天室，支持实时流式响应与连续对话。'
    },
    props: {
      title: 'AI 超级智能体',
      endpoint: '/ai/manus/chat',
      withChatId: false
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.afterEach((to) => {
  const title = to.meta.title || 'CJ AI Agent 智能体平台'
  const description =
    to.meta.description || 'CJ AI Agent 平台，支持实时流式 AI 对话。'
  document.title = title

  let descriptionMeta = document.querySelector('meta[name="description"]')
  if (!descriptionMeta) {
    descriptionMeta = document.createElement('meta')
    descriptionMeta.setAttribute('name', 'description')
    document.head.appendChild(descriptionMeta)
  }
  descriptionMeta.setAttribute('content', description)
})

export default router
