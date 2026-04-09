<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const apps = [
  {
    title: 'AI 情感大师',
    description: '基于情感心理与多轮记忆能力，给你温柔且有洞察的回应。',
    path: '/love-chat',
    tag: 'Love'
  },
  {
    title: 'AI 超级智能体',
    description: '更强的任务理解与推理能力，适合复杂问题快速拆解。',
    path: '/manus-chat',
    tag: 'Manus'
  }
]

function goTo(path) {
  router.push(path)
}
</script>

<template>
  <div class="home-container">
    <section class="hero">
      <p class="hero-kicker">CJ AI Agent Platform</p>
      <h1>极客灵感与温柔对话，<br />在这里同时发生</h1>
      <p class="subtitle">
        一个入口，连接两种 AI 体验。无论是情感咨询，还是复杂任务推理，都能实时响应。
      </p>
    </section>

    <div class="app-grid">
      <div
        v-for="app in apps"
        :key="app.path"
        class="app-card"
        @click="goTo(app.path)"
      >
        <div class="card-top">
          <span class="tag">{{ app.tag }}</span>
        </div>
        <h2>{{ app.title }}</h2>
        <p>{{ app.description }}</p>
        <button>进入应用</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 64px 24px 48px;
}

.hero {
  margin-bottom: 34px;
}

.hero-kicker {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  letter-spacing: 0.1em;
  color: #c4b5fd;
  background: rgba(109, 124, 255, 0.16);
  border: 1px solid rgba(109, 124, 255, 0.35);
}

.hero h1 {
  margin: 0;
  margin-top: 14px;
  font-size: clamp(28px, 4vw, 46px);
  line-height: 1.2;
  color: #f8fafc;
  text-wrap: balance;
}

.subtitle {
  max-width: 760px;
  margin: 16px 0 0;
  color: #cbd5e1;
  font-size: clamp(15px, 2vw, 18px);
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.app-card {
  padding: 24px;
  border-radius: 16px;
  background: linear-gradient(160deg, rgba(25, 33, 62, 0.88), rgba(15, 23, 42, 0.82));
  border: 1px solid rgba(148, 163, 184, 0.25);
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 18px 40px rgba(3, 7, 18, 0.32);
}

.app-card:hover {
  transform: translateY(-4px) scale(1.005);
  border-color: rgba(212, 107, 255, 0.45);
  box-shadow: 0 22px 50px rgba(16, 26, 59, 0.45);
}

.card-top {
  margin-bottom: 8px;
}

.tag {
  display: inline-block;
  padding: 4px 10px;
  font-size: 12px;
  border-radius: 999px;
  color: #f5d0fe;
  background: rgba(212, 107, 255, 0.2);
  border: 1px solid rgba(212, 107, 255, 0.45);
}

.app-card h2 {
  margin: 0 0 12px;
  font-size: 22px;
  color: #f8fafc;
}

.app-card p {
  margin: 0 0 16px;
  min-height: 48px;
  color: #cbd5e1;
}

button {
  border: 0;
  border-radius: 10px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #6d7cff, #d46bff);
  color: #ffffff;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s ease;
}

button:hover {
  transform: translateY(-1px);
}

@media (max-width: 1024px) {
  .home-container {
    padding: 52px 20px 38px;
  }
}

@media (max-width: 768px) {
  .app-grid {
    grid-template-columns: 1fr;
  }

  .home-container {
    padding: 44px 16px 30px;
  }

  .subtitle {
    font-size: 15px;
  }

  .app-card {
    padding: 20px;
  }
}

@media (max-width: 420px) {
  .hero h1 br {
    display: none;
  }

  .app-card p {
    min-height: 0;
  }
}
</style>
