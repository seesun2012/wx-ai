<template>
  <div class="login-container">
    <div class="login-box">
      <h1>◈ 万象AI</h1>
      <p class="subtitle">{{ isLogin ? '登录' : '注册' }}</p>

      <div class="form-group">
        <input v-model="username" type="text" placeholder="用户名" @keydown.enter="submit" />
      </div>
      <div class="form-group">
        <input v-model="password" type="password" placeholder="密码（至少6位）" @keydown.enter="submit" />
      </div>

      <p class="error-msg" v-if="errorMsg">{{ errorMsg }}</p>

      <button class="submit-btn" @click="submit" :disabled="loading">
        {{ loading ? '请稍候...' : (isLogin ? '登 录' : '注 册') }}
      </button>

      <p class="switch-link" @click="toggleMode">
        {{ isLogin ? '没有账号？点击注册' : '已有账号？点击登录' }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import apiClient from '../api/chat'

const router = useRouter()
const username = ref('')
const password = ref('')
const isLogin = ref(true)
const errorMsg = ref('')
const loading = ref(false)

function toggleMode() {
  isLogin.value = !isLogin.value
  errorMsg.value = ''
}

async function submit() {
  errorMsg.value = ''
  if (!username.value.trim() || !password.value) {
    errorMsg.value = '请输入用户名和密码'
    return
  }
  if (!isLogin.value && password.value.length < 6) {
    errorMsg.value = '密码至少6位'
    return
  }

  loading.value = true
  try {
    const url = isLogin.value ? '/user/login' : '/user/register'
    const res = await apiClient.post(url, {
      username: username.value.trim(),
      password: password.value
    })
    if (res.data.code === 200) {
      const user = res.data.data
      localStorage.setItem('token', user.token)
      localStorage.setItem('userId', user.id)
      localStorage.setItem('username', user.username)
      if (isLogin.value) {
        router.push('/')
      } else {
        isLogin.value = true
        errorMsg.value = ''
      }
    } else {
      errorMsg.value = res.data.message
    }
  } catch (e) {
    errorMsg.value = '网络错误，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  background: white;
  padding: 40px;
  border-radius: 16px;
  width: 360px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  text-align: center;
}

.login-box h1 {
  margin: 0 0 8px;
  font-size: 28px;
}

.subtitle {
  color: #888;
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 16px;
  outline: none;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

.form-group input:focus {
  border-color: #667eea;
}

.error-msg {
  color: #ef4444;
  font-size: 14px;
  margin: -8px 0 12px;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.3s;
}

.submit-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.switch-link {
  margin-top: 20px;
  color: #667eea;
  cursor: pointer;
  font-size: 14px;
}

.switch-link:hover {
  text-decoration: underline;
}
</style>