<template>
  <div class="ai-chat">
    <div class="chat-header">
      <h3>🤖 AI 健身教练</h3>
      <span class="subtitle">随时为你提供健身建议</span>
    </div>
    <div class="messages" ref="msgContainer">
      <div v-for="(msg, i) in messages" :key="i" :class="['msg', msg.role]">
        <div class="msg-content" v-html="formatMsg(msg.content)"></div>
      </div>
      <div v-if="streaming" class="msg assistant">
        <div class="msg-content" v-html="formatMsg(streamText)"></div>
        <span class="cursor">|</span>
      </div>
    </div>
    <div class="input-area">
      <el-input
        v-model="input"
        placeholder="向 AI 教练提问...（例如：帮我制定一个减脂计划）"
        size="large"
        @keyup.enter="sendMessage"
        :disabled="streaming"
      >
        <template #append>
          <el-button @click="sendMessage" :loading="streaming">发送</el-button>
        </template>
      </el-input>
    </div>
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
      if (streamText.value) messages.value.push({ role: 'assistant', content: streamText.value })
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
  nextTick(() => { if (msgContainer.value) msgContainer.value.scrollTop = msgContainer.value.scrollHeight })
}
</script>

<style scoped>
.ai-chat { display: flex; flex-direction: column; height: calc(100vh - 104px); }
.chat-header {
  text-align: center;
  padding: 16px 0;
  border-bottom: 1px solid #eee;
  background: #fff;
  border-radius: 12px;
  margin-bottom: 12px;
}
.chat-header h3 { margin: 0; font-size: 18px; }
.subtitle { font-size: 12px; color: #999; }

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 12px;
}

.msg { display: flex; }
.msg.user { justify-content: flex-end; }
.msg.user .msg-content {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 16px 16px 4px 16px;
}
.msg.assistant .msg-content {
  background: #f0f2f5;
  border-radius: 16px 16px 16px 4px;
}
.msg-content { max-width: 70%; padding: 12px 16px; line-height: 1.6; font-size: 14px; }
.cursor { animation: blink 1s infinite; color: #667eea; }
@keyframes blink { 50% { opacity: 0; } }

.input-area {
  background: #fff;
  padding: 16px;
  border-radius: 12px;
}
</style>
