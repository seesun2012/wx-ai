<template>
  <div class="layout" @click="menuIndex = -1">
    <aside class="sidebar">
      <div class="sidebar-header">
        <h2>◈ 万象AI</h2>
        <button class="new-btn" @click="newChat" :disabled="creating">+ 新对话</button>
      </div>

      <div class="session-list">
        <div
            v-for="(session, index) in sessions"
            :key="session.memoryId"
            :class="['session-item', { active: currentSessionId === session.memoryId, 'menu-open': menuIndex === index }]"
            @click="switchSession(session.memoryId)"
        >
          <span class="session-title">{{ session.title }}</span>
          <span class="session-menu" @click.stop="menuIndex = menuIndex === index ? -1 : index">⋯</span>
          <div v-if="menuIndex === index" class="menu-dropdown">
            <div class="menu-item" @click.stop="startRename(session)">✏️ 重命名</div>
            <div class="menu-item danger" @click.stop="deleteSession(session.memoryId)">🗑️ 删除</div>
          </div>
        </div>
        <div v-if="sessions.length === 0" class="no-sessions">暂无对话记录</div>
      </div>

      <div class="sidebar-footer">
        <span class="user-info">👤 {{ username }}</span>
        <button class="logout-btn" @click="logout">退出</button>
      </div>
    </aside>

    <main class="main-content">
      <ChatRoom :session-id="currentSessionId" @session-updated="onSessionUpdated" @session-created="onSessionCreated" />
    </main>

    <div v-if="renameModal" class="modal-mask" @click="renameModal = false">
      <div class="modal-box" @click.stop>
        <h3>重命名会话</h3>
        <input v-model="editTitle" class="modal-input" @keydown.enter="confirmRename" @keydown.escape="renameModal = false" ref="modalInput" />
        <div class="modal-actions">
          <button class="modal-cancel" @click="renameModal = false">取消</button>
          <button class="modal-confirm" @click="confirmRename">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import apiClient from '../api/chat'
import ChatRoom from './ChatRoom.vue'

const router = useRouter()
const sessions = ref([])
const currentSessionId = ref('')
const username = ref(localStorage.getItem('username') || '')
const creating = ref(false)

const menuIndex = ref(-1)
const renameModal = ref(false)
const editTitle = ref('')
const renameTarget = ref(null)
const modalInput = ref(null)

onMounted(() => {
  loadSessions()
})

function closeMenu() {
  menuIndex.value = -1
}

async function loadSessions() {
  try {
    const res = await apiClient.get('/session/list')
    if (res.data.code === 200) {
      sessions.value = res.data.data
      if (sessions.value.length > 0 && !currentSessionId.value) {
        currentSessionId.value = sessions.value[0].memoryId
      }
    }
  } catch (e) {
    console.error('加载会话列表失败', e)
  }
}

async function newChat() {
  if (creating.value) return
  creating.value = true
  try {
    const res = await apiClient.post('/session/create')
    if (res.data.code === 200) {
      const session = res.data.data
      sessions.value.unshift(session)
      currentSessionId.value = session.memoryId
    }
  } catch (e) {
    console.error('创建会话失败', e)
  } finally {
    creating.value = false
  }
}

function switchSession(memoryId) {
  menuIndex.value = -1
  currentSessionId.value = memoryId
}

// function toggleMenu(memoryId) {
//   if (menuId.value && menuId.value !== memoryId) {
//     menuId.value = ''
//   }
//   menuId.value = menuId.value === memoryId ? '' : memoryId
// }

function startRename(session) {
  menuIndex.value = -1
  renameTarget.value = session
  editTitle.value = session.title
  renameModal.value = true
  nextTick(() => {
    if (modalInput.value) modalInput.value.focus()
  })
}

async function confirmRename() {
  const title = editTitle.value.trim()
  if (!title || title.length > 30 || !renameTarget.value) {
    renameModal.value = false
    return
  }
  try {
    await apiClient.put('/session/' + renameTarget.value.memoryId + '/title', { title })
    renameTarget.value.title = title
  } catch (e) {
    console.error('重命名失败', e)
  }
  renameModal.value = false
}

async function deleteSession(memoryId) {
  menuIndex.value = -1
  if (!memoryId) return
  if (!confirm('确定删除这个对话吗？')) return
  try {
    await apiClient.delete('/session/' + memoryId)
    sessions.value = sessions.value.filter(s => s.memoryId !== memoryId)
    if (currentSessionId.value === memoryId) {
      currentSessionId.value = sessions.value.length > 0 ? sessions.value[0].memoryId : ''
    }
  } catch (e) {
    console.error('删除失败', e)
  }
}

function onSessionUpdated(memoryId, title) {
  const session = sessions.value.find(s => s.memoryId === memoryId)
  if (session && title) {
    session.title = title
  }
}

function onSessionCreated(session) {
  sessions.value.unshift(session)
  currentSessionId.value = session.memoryId
}

async function logout() {
  try {
    await apiClient.post('/user/logout')
  } catch (e) {}
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 280px;
  background: #1a1a2e;
  color: white;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #2a2a4a;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 18px;
}

.new-btn {
  padding: 6px 14px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.new-btn:hover { background: #5a6fd6; }
.new-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.session-item {
  padding: 12px 14px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
  transition: background 0.2s;
  position: relative;
  .session-item:hover { background: #2a2a4a; }
  .session-item.active { background: #667eea; }
  .session-item.menu-open { z-index: 50; }
}

.session-item:hover { background: #2a2a4a; }
.session-item.active { background: #667eea; }

.session-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.session-menu {
  opacity: 0;
  cursor: pointer;
  padding: 2px 8px;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 2px;
  border-radius: 4px;
}

.session-item:hover .session-menu { opacity: 0.7; }
.session-menu:hover { opacity: 1 !important; background: rgba(255,255,255,0.1); }

.menu-dropdown {
  position: absolute;
  right: 10px;
  top: 100%;
  background: #2a2a4a;
  border-radius: 8px;
  padding: 4px 0;
  z-index: 100;
  min-width: 120px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.3);
}

.menu-item {
  padding: 8px 16px;
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
}

.menu-item:hover { background: #3a3a5a; }
.menu-item.danger:hover { background: #5a2a2a; color: #ef4444; }

.no-sessions {
  text-align: center;
  color: #666;
  padding: 40px 0;
  font-size: 14px;
}

.sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid #2a2a4a;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info { font-size: 14px; }

.logout-btn {
  padding: 6px 14px;
  background: transparent;
  color: #aaa;
  border: 1px solid #555;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.logout-btn:hover { color: white; border-color: #888; }

.main-content {
  flex: 1;
  overflow: hidden;
}

.modal-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal-box {
  background: white;
  border-radius: 12px;
  padding: 24px;
  width: 360px;
  color: #333;
}

.modal-box h3 {
  margin: 0 0 16px;
  font-size: 18px;
}

.modal-input {
  width: 100%;
  padding: 10px 14px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 15px;
  outline: none;
  box-sizing: border-box;
}

.modal-input:focus { border-color: #667eea; }

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}

.modal-cancel {
  padding: 8px 20px;
  background: #f3f4f6;
  color: #666;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.modal-confirm {
  padding: 8px 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.modal-confirm:hover { background: #5a6fd6; }
</style>