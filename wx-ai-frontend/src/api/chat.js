import axios from 'axios'

const apiClient = axios.create({
  baseURL: '/api',
  timeout: 300000
})

apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = 'Bearer ' + token
  }
  return config
})

apiClient.interceptors.response.use(
    res => res,
    err => {
      if (err.response && err.response.status === 401) {
        localStorage.clear()
        window.location.href = '/login'
      }
      return Promise.reject(err)
    }
)

export function chatStream(memoryId, message, onMessage, onError, onComplete) {

    const token = localStorage.getItem('token') || ''
    const params = new URLSearchParams()
    params.append('memoryId', String(memoryId))
    params.append('message', message)

    fetch('/api/ai/chat', {
      method: 'POST',
      headers: {
        'Accept': 'text/event-stream',
        'Content-Type': 'application/x-www-form-urlencoded',
        'Authorization': 'Bearer ' + token
      },
      body: params.toString()
    })
        .then(response => {
          if (!response.ok) {
            throw new Error(`HTTP ${response.status}`)
          }
          const reader = response.body.getReader()
          const decoder = new TextDecoder()
          let buffer = ''
          let currentData = ''

          function read() {
            reader.read().then(({ done, value }) => {
              if (done) {
                if (currentData && currentData !== '[DONE]') {
                  onMessage(currentData)
                }
                currentData = ''
                onComplete()
                return
              }
              buffer += decoder.decode(value, { stream: true })
              const parts = buffer.split('\n\n')
              buffer = parts.pop() || ''
              for (const part of parts) {
                const lines = part.split('\n')
                let eventData = ''
                for (const line of lines) {
                  const trimmed = line.trim()
                  if (trimmed.startsWith('data:')) {
                    let d = trimmed.substring(5)
                    d = d.replace(/^\}/, '').replace(/\}$/, '')
                    if (eventData) eventData += '\n'
                    eventData += d
                  }
                }
                if (eventData && eventData !== '[DONE]') {
                  onMessage(eventData)
                }
              }
              read()
            }).catch(err => {
              onError(err)
            })
          }
          read()
        })
    .catch(err => {
      onError(err)
    })
}

export default apiClient
