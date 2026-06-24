import { useUserStore } from '@/stores/user'

export function chatWithAi(message, sessionId, onToken, onDone, onError) {
  const userStore = useUserStore()
  const url = `http://localhost:8080/api/ai/chat?message=${encodeURIComponent(message)}&sessionId=${sessionId}`

  fetch(url, {
    headers: { Authorization: `Bearer ${userStore.accessToken}` },
  }).then(async (response) => {
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''
      for (const line of lines) {
        if (line.startsWith('data:')) {
          onToken(line.substring(5).trim())
        }
      }
    }
    onDone()
  }).catch(onError)
}

export function getChatHistory() {
  const { default: request } = require('@/utils/request')
  return request.get('/ai/history')
}
