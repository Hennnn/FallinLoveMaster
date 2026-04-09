<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { buildSseUrl } from '../api/http'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  endpoint: {
    type: String,
    required: true
  },
  withChatId: {
    type: Boolean,
    default: false
  }
})

const router = useRouter()
const messages = ref([])
const inputValue = ref('')
const loading = ref(false)
const currentEventSource = ref(null)
const chatPanelRef = ref(null)
const expandedBlocks = ref({})

const STEP_PREVIEW_LIMIT = 260

const conversationId = ref('')

const conversationIdText = computed(() => {
  if (!props.withChatId) {
    return '当前应用不需要 conversationId'
  }
  return conversationId.value
})

function generateConversationId() {
  return `conversation_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`
}

function scrollToBottom() {
  nextTick(() => {
    if (chatPanelRef.value) {
      chatPanelRef.value.scrollTop = chatPanelRef.value.scrollHeight
    }
  })
}

function closeCurrentStream() {
  if (currentEventSource.value) {
    currentEventSource.value.close()
    currentEventSource.value = null
  }
}

function appendAiContent(text) {
  const lastMessage = messages.value[messages.value.length - 1]
  if (!lastMessage || lastMessage.role !== 'ai') {
    messages.value.push({ role: 'ai', content: text })
  } else {
    lastMessage.content += text
  }
  scrollToBottom()
}

function startSse(message) {
  loading.value = true
  messages.value.push({ role: 'ai', content: '' })

  const params = { message }
  if (props.withChatId) {
    params.conversationId = conversationId.value
  }

  const requestUrl = buildSseUrl(props.endpoint, params)
  const eventSource = new EventSource(requestUrl)
  currentEventSource.value = eventSource

  eventSource.onmessage = (event) => {
    if (!event.data || event.data === '[DONE]') {
      return
    }
    appendAiContent(event.data)
  }

  eventSource.onerror = () => {
    loading.value = false
    closeCurrentStream()
    const lastMessage = messages.value[messages.value.length - 1]
    if (lastMessage && lastMessage.role === 'ai' && !lastMessage.content) {
      lastMessage.content = '连接中断，请稍后重试。'
    }
  }
}

function sendMessage() {
  const content = inputValue.value.trim()
  if (!content || loading.value) {
    return
  }

  closeCurrentStream()
  messages.value.push({ role: 'user', content })
  inputValue.value = ''
  scrollToBottom()
  startSse(content)
}

function goBackHome() {
  closeCurrentStream()
  router.push('/')
}

function parseStepBlocks(content) {
  if (!content) {
    return []
  }
  const stepRegex = /(Step\s+\d+\s*:)/g
  const parts = content.split(stepRegex)
  if (parts.length <= 1) {
    return [
      {
        title: 'AI 回复',
        body: content.trim()
      }
    ]
  }

  const blocks = []
  for (let i = 1; i < parts.length; i += 2) {
    const title = (parts[i] || '').trim()
    const body = (parts[i + 1] || '').trim()
    blocks.push({
      title,
      body
    })
  }
  return blocks
}

function getBlockKey(messageIndex, blockIndex) {
  return `${messageIndex}-${blockIndex}`
}

function isBlockExpanded(messageIndex, blockIndex) {
  return !!expandedBlocks.value[getBlockKey(messageIndex, blockIndex)]
}

function toggleBlock(messageIndex, blockIndex) {
  const key = getBlockKey(messageIndex, blockIndex)
  expandedBlocks.value[key] = !expandedBlocks.value[key]
}

function getPreviewText(text) {
  if (!text) {
    return ''
  }
  if (text.length <= STEP_PREVIEW_LIMIT) {
    return text
  }
  return `${text.slice(0, STEP_PREVIEW_LIMIT)}...`
}

function needTruncate(text) {
  return !!text && text.length > STEP_PREVIEW_LIMIT
}

onMounted(() => {
  if (props.withChatId) {
    conversationId.value = generateConversationId()
  }
  messages.value.push({
    role: 'ai',
    content: `你好，我是${props.title}，请告诉我你的问题。`
  })
  scrollToBottom()
})

onBeforeUnmount(() => {
  closeCurrentStream()
})
</script>

<template>
  <div class="chat-page">
    <div class="chat-header">
      <button class="back-btn" @click="goBackHome">返回主页</button>
      <div class="chat-header-info">
        <h1>{{ title }}</h1>
        <p>会话 ID：{{ conversationIdText }}</p>
      </div>
    </div>

    <div ref="chatPanelRef" class="chat-panel">
      <div
        v-for="(item, index) in messages"
        :key="index"
        class="message-row"
        :class="item.role === 'user' ? 'message-user' : 'message-ai'"
      >
        <div v-if="item.role === 'ai'" class="ai-avatar" aria-label="AI 头像">
          AI
        </div>
        <div class="message-bubble">
          <template v-if="item.role === 'ai'">
            <div
              v-for="(block, blockIndex) in parseStepBlocks(item.content || '...')"
              :key="`${index}-${blockIndex}`"
              class="step-card"
            >
              <p class="step-title">{{ block.title }}</p>
              <p class="step-body">
                {{
                  isBlockExpanded(index, blockIndex)
                    ? block.body
                    : getPreviewText(block.body)
                }}
              </p>
              <button
                v-if="needTruncate(block.body)"
                class="ellipsis-btn"
                @click="toggleBlock(index, blockIndex)"
              >
                {{ isBlockExpanded(index, blockIndex) ? '收起' : '...展开全部' }}
              </button>
            </div>
          </template>
          <template v-else>
            {{ item.content || '...' }}
          </template>
        </div>
      </div>
    </div>

    <div class="chat-input-area">
      <input
        v-model="inputValue"
        type="text"
        placeholder="请输入消息，按 Enter 发送"
        @keydown.enter="sendMessage"
      />
      <button :disabled="loading" @click="sendMessage">
        {{ loading ? '回复中...' : '发送' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.chat-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 20px 30px;
}

.chat-header {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  margin-bottom: 14px;
  justify-content: space-between;
}

.chat-header-info {
  flex: 1;
}

.chat-header h1 {
  margin: 0;
  font-size: clamp(22px, 2.8vw, 30px);
  color: #f8fafc;
}

.chat-header p {
  margin: 4px 0 0;
  color: #cbd5e1;
  font-size: 14px;
  word-break: break-all;
}

.back-btn {
  border: 1px solid rgba(148, 163, 184, 0.45);
  background: rgba(15, 23, 42, 0.8);
  color: #e2e8f0;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
}

.chat-panel {
  height: 62vh;
  overflow-y: auto;
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.25);
  background: rgba(9, 13, 32, 0.78);
  backdrop-filter: blur(10px);
  padding: 18px 14px;
  box-shadow: inset 0 0 0 1px rgba(148, 163, 184, 0.1);
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 12px;
}

.message-ai {
  justify-content: flex-start;
}

.message-user {
  justify-content: flex-end;
}

.message-bubble {
  max-width: 76%;
  border-radius: 14px;
  padding: 10px 12px;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
}

.message-ai .message-bubble {
  text-align: left;
  background: rgba(109, 124, 255, 0.2);
  border: 1px solid rgba(109, 124, 255, 0.45);
  color: #e2e8f0;
}

.message-user .message-bubble {
  text-align: left;
  background: linear-gradient(135deg, #6d7cff, #d46bff);
  color: #ffffff;
}

.step-card + .step-card {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed rgba(203, 213, 225, 0.35);
}

.step-title {
  margin: 0 0 6px;
  font-size: 13px;
  font-weight: 700;
  color: #c7d2fe;
}

.step-body {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.ellipsis-btn {
  margin-top: 8px;
  border: none;
  background: transparent;
  color: #f5d0fe;
  font-size: 12px;
  cursor: pointer;
  padding: 0;
}

.ai-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 700;
  color: #f8fafc;
  background: linear-gradient(135deg, #60a5fa, #a78bfa);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 12px rgba(96, 165, 250, 0.35);
}

.chat-input-area {
  margin-top: 12px;
  display: flex;
  gap: 10px;
}

.chat-input-area input {
  flex: 1;
  border: 1px solid rgba(148, 163, 184, 0.45);
  border-radius: 10px;
  padding: 11px 12px;
  font-size: 15px;
  outline: none;
  color: #f8fafc;
  background: rgba(15, 23, 42, 0.8);
}

.chat-input-area input:focus {
  border-color: rgba(109, 124, 255, 0.85);
}

.chat-input-area button {
  border: 0;
  border-radius: 10px;
  padding: 0 18px;
  background: linear-gradient(135deg, #6d7cff, #d46bff);
  color: #ffffff;
  font-weight: 600;
  cursor: pointer;
}

.chat-input-area button:disabled {
  background: #7c86ca;
  cursor: not-allowed;
}

@media (max-width: 992px) {
  .chat-page {
    padding: 20px 16px 26px;
  }

  .chat-panel {
    height: 58vh;
  }
}

@media (max-width: 768px) {
  .chat-header {
    flex-direction: column;
    align-items: stretch;
  }

  .back-btn {
    width: fit-content;
  }

  .message-bubble {
    max-width: 84%;
  }

  .chat-input-area {
    flex-direction: column;
  }

  .chat-input-area button {
    height: 42px;
  }
}

@media (max-width: 420px) {
  .chat-page {
    padding: 16px 12px 22px;
  }

  .chat-panel {
    height: 55vh;
    padding: 14px 10px;
  }

  .message-bubble {
    max-width: 90%;
    font-size: 14px;
  }
}
</style>
