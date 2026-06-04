<template>
  <div class="chat-container">
    <div class="chat-messages" ref="messagesContainer" @scroll="onScroll">
      <div v-if="loadingHistory" class="loading-history">加载中...</div>
      <div v-if="!hasMore && messages.length > 0" class="no-more">— 没有更多了 —</div>

      <div v-if="messages.length === 0 && !loadingHistory" class="welcome-message">
        <p>👋 欢迎使用 万象AI！</p>
        <p>我可以帮助你解答编程学习和求职面试相关的问题。</p>
      </div>

      <div
          v-for="(msg, index) in messages"
          :key="msg.id || index"
          :class="['message', msg.role]"
      >
        <div class="message-avatar">
          {{ msg.role === 'user' ? '👤' : '◈' }}
        </div>
        <div class="message-content">
          <div class="message-text" v-html="formatMessage(msg.content)"></div>
        </div>
      </div>

      <div v-if="isLoading" class="message assistant">
        <div class="message-avatar">◈</div>
        <div class="message-content">
          <div class="message-text loading">
            <span class="loading-dot"></span>
            <span class="loading-dot"></span>
            <span class="loading-dot"></span>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input">
      <textarea
          v-model="inputMessage"
          @keydown.enter.exact.prevent="sendMessage"
          placeholder="请输入您的问题..."
          :disabled="isLoading"
          rows="1"
      ></textarea>
      <button @click="sendMessage" :disabled="isLoading || !inputMessage.trim()">
        发送
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { chatStream } from '../api/chat'
import apiClient from '../api/chat'

const props = defineProps({ sessionId: String })
const emit = defineEmits(['session-updated', 'session-created'])

const messages = ref([])
const inputMessage = ref('')
const isLoading = ref(false)
const loadingHistory = ref(false)
const messagesContainer = ref(null)
const hasMore = ref(true)
const oldestId = ref(0)
const currentTitle = ref('新对话')
const isFirstMessage = ref(true)

watch(() => props.sessionId, async (newId) => {
  if (newId) {
    messages.value = []
    hasMore.value = true
    oldestId.value = 0
    isFirstMessage.value = true
    currentTitle.value = '新对话'
    await loadHistory(newId, 0)
  } else {
    messages.value = []
    hasMore.value = true
    oldestId.value = 0
    currentTitle.value = ''
  }
}, { immediate: true })

async function loadHistory(memoryId, lastId) {
  if (loadingHistory.value) return
  loadingHistory.value = true
  try {
    const res = await apiClient.get('/session/' + memoryId + '/messages', {
      params: { lastId, limit: 20 }
    })
    if (res.data.code === 200) {
      const data = res.data.data
      const older = data.messages || []
      hasMore.value = data.hasMore

      if (older.length > 0) {
        oldestId.value = older[0].id

        const container = messagesContainer.value
        const prevScrollHeight = container ? container.scrollHeight : 0

        if (lastId === 0) {
          messages.value = older
        } else {
          messages.value = [...older, ...messages.value]
        }

        await nextTick()
        if (lastId === 0) {
          scrollToBottom()
        } else if (container) {
          container.scrollTop = container.scrollHeight - prevScrollHeight
        }
      }

      if (lastId === 0 && older.length > 0) {
        const firstUser = older.find(m => m.role === 'user')
        if (firstUser) {
          isFirstMessage.value = false
          const t = firstUser.content.length > 50 ? firstUser.content.substring(0, 50) + '...' : firstUser.content
          currentTitle.value = t
        }
      }
    }
  } catch (e) {
    console.error('加载历史失败', e)
  } finally {
    loadingHistory.value = false
  }
}

function onScroll() {
  const container = messagesContainer.value
  if (!container || loadingHistory.value || !hasMore.value || !props.sessionId) return
  if (container.scrollTop < 100) {
    loadHistory(props.sessionId, oldestId.value)
  }
}

function formatMessage(content) {
  return content
      .replace(/\n/g, '<br>')
      .replace(/`([^`]+)`/g, '<code>$1</code>')
      .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

async function sendMessage() {
  const message = inputMessage.value.trim()
  if (!message || isLoading.value) return

  let sid = props.sessionId
  if (!sid) {
    try {
      const res = await apiClient.post('/session/create')
      if (res.data.code === 200) {
        sid = res.data.data.memoryId
        emit('session-created', res.data.data)
      } else {
        return
      }
    } catch (e) {
      console.error('创建会话失败', e)
      return
    }
  }

  const userMsg = { role: 'user', content: message }
  messages.value.push(userMsg)
  inputMessage.value = ''
  isLoading.value = true
  scrollToBottom()

  if (isFirstMessage.value) {
    isFirstMessage.value = false
    const title = message.length > 30 ? message.substring(0, 30) + '...' : message
    currentTitle.value = title
    emit('session-updated', sid, title)
  }

  let fullResponse = ''
  let assistantMessageIndex = messages.value.length

  messages.value.push({ role: 'assistant', content: '' })
  scrollToBottom()

  chatStream(
      sid,
      message,
      (token) => {
        fullResponse += token
        if (assistantMessageIndex < messages.value.length) {
          messages.value[assistantMessageIndex].content = fullResponse
          scrollToBottom()
        }
      },
      (error) => {
        console.error('SSE Error:', error)
        isLoading.value = false
        if (assistantMessageIndex < messages.value.length) {
          messages.value[assistantMessageIndex].content = '抱歉，发生了错误，请重试。'
        }
      },
      () => {
        isLoading.value = false
      }
  )
}
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 1200px;
  margin: 0 auto;
  background-color: #fff;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #f9fafb;
}

.loading-history {
  text-align: center;
  color: #999;
  padding: 10px;
  font-size: 14px;
}

.no-more {
  text-align: center;
  color: #ccc;
  padding: 10px;
  font-size: 13px;
}

.welcome-message {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.welcome-message p {
  margin: 10px 0;
  font-size: 16px;
}

.message {
  display: flex;
  margin-bottom: 20px;
  align-items: flex-start;
}

.message.user {
  flex-direction: row-reverse;
}

.message.assistant {
  flex-direction: row;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: #667eea;
  margin-left: 12px;
}

.message.assistant .message-avatar {
  background: #10b981;
  margin-right: 12px;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
}

.message.user .message-content {
  background: #667eea;
  color: white;
  border-bottom-right-radius: 4px;
}

.message.assistant .message-content {
  background: white;
  color: #333;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message-text {
  word-wrap: break-word;
  white-space: pre-wrap;
}

.message-text :deep(code) {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
}

.message.user .message-text :deep(code) {
  background: rgba(255, 255, 255, 0.2);
}

.loading {
  display: flex;
  gap: 4px;
}

.loading-dot {
  width: 8px;
  height: 8px;
  background: #999;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.loading-dot:nth-child(1) { animation-delay: -0.32s; }
.loading-dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.chat-input {
  padding: 20px;
  background: white;
  border-top: 1px solid #e5e7eb;
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.chat-input textarea {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 24px;
  resize: none;
  font-size: 16px;
  font-family: inherit;
  outline: none;
  transition: border-color 0.3s;
  max-height: 120px;
  min-height: 48px;
}

.chat-input textarea:focus { border-color: #667eea; }
.chat-input textarea:disabled { background: #f3f4f6; cursor: not-allowed; }

.chat-input button {
  padding: 12px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.3s, transform 0.3s;
}

.chat-input button:hover:not(:disabled) { opacity: 0.9; transform: translateY(-2px); }
.chat-input button:disabled { opacity: 0.5; cursor: not-allowed; }

@media (max-width: 768px) {
  .message-content { max-width: 85%; }
}
</style>