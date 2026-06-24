<template>
  <div class="ai-chat">
    <el-container>
      <el-header class="nav">
        <span class="logo" @click="$router.push('/')">🏋️ Fitness</span>
        <h3 style="margin:0">🤖 AI 健身教练</h3>
        <el-button text @click="$router.push('/')">返回</el-button>
      </el-header>
      <el-main class="chat-main">
        <div class="messages" ref="msgContainer">
          <div v-for="(msg, i) in messages" :key="i" :class="['msg', msg.role]">
            <div class="msg-content" v-html="formatMsg(msg.content)"></div>
          </div>
          <div v-if="streaming" class="msg assistant">
            <div class="msg-content" v-html="formatMsg(streamText)"></div>
            <span class="cursor">|</span>
          </div>
        </div>
      </el-main>
      <el-footer class="input-area">
        <el-input
          v-model="input"
          placeholder="向 AI 教练提问...（例如：帮我制定一个减脂计划）"
          size="large"
          @keyup.enter="sendMessage"
          :disabled="streaming"
        >
          <template #append>
            <el-button :icon="'Promotion'" @click="sendMessage" :loading="streaming">发送</el-button>
          </template>
        </el-input>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { chatWithAi } from '@/api/ai'

const messages = ref([
  { role: 'assistant', content: '你好！我是你的 AI 健身教练 🤖\n\n我可以帮你：\n- 📊 分析你的训练和身体数据\n- 🍎 提供饮食和营养建议\n- 📋 制定个性化训练计划\n- 💡 回答健身知识问题\n\n有什么我可以帮你的？' },
])
const input = ref('')
const streamText = ref('')
const streaming = ref(false)
const msgContainer = ref(null)

function sendMessage() {
  if (!input.value.trim() || streaming.value) return
  const msg = input.value.trim()
  messages.value.push({ role: 'user', content: msg })
  input.value = ''
  streamText.value = ''
  streaming.value = true

  chatWithAi(msg, 'default',
    (token) => { streamText.value += token; scrollToBottom() },
    () => {
      if (streamText.value) {
        messages.value.push({ role: 'assistant', content: streamText.value })
      }
      streamText.value = ''
      streaming.value = false
      scrollToBottom()
    },
    (err) => {
      messages.value.push({ role: 'assistant', content: '抱歉，AI 服务暂时不可用，请稍后重试。' })
      streaming.value = false
    },
  )
}

function formatMsg(text) {
  return text.replace(/\n/g, '<br>').replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
}

function scrollToBottom() {
  nextTick(() => {
    if (msgContainer.value) msgContainer.value.scrollTop = msgContainer.value.scrollHeight
  })
}
</script>

<style scoped>
.nav { display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #eee; padding: 0 20px; background: white; }
.logo { font-size: 20px; font-weight: bold; cursor: pointer; }
.chat-main { flex: 1; overflow-y: auto; padding: 20px; background: #f5f5f5; }
.messages { max-width: 800px; margin: 0 auto; display: flex; flex-direction: column; gap: 12px; }
.msg { display: flex; }
.msg.user { justify-content: flex-end; }
.msg.user .msg-content { background: #667eea; color: white; border-radius: 16px 16px 4px 16px; }
.msg.assistant .msg-content { background: white; border-radius: 16px 16px 16px 4px; }
.msg-content { max-width: 70%; padding: 12px 16px; line-height: 1.6; }
.cursor { animation: blink 1s infinite; }
@keyframes blink { 50% { opacity: 0; } }
.input-area { padding: 16px; background: white; border-top: 1px solid #eee; max-width: 800px; margin: 0 auto; }
</style>
