<script setup>
const currentYear = new Date().getFullYear()
</script>

<template>
  <div class="app-shell">
    <main class="app-main">
      <router-view />
    </main>

    <footer class="site-footer">
      <div class="site-footer-inner">
        <p>Copyright © {{ currentYear }} CJ AI Agent. All Rights Reserved.</p>
        <p>备案号：粤ICP备2026000000号-1</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-main {
  flex: 1;
}

.site-footer {
  border-top: 1px solid rgba(148, 163, 184, 0.25);
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
}

.site-footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 18px 20px 22px;
  text-align: center;
  color: #cbd5e1;
  font-size: 13px;
  line-height: 1.7;
}

.site-footer-inner p {
  margin: 0;
}

@media (max-width: 768px) {
  .site-footer-inner {
    font-size: 12px;
    padding: 14px 16px 16px;
  }
}
</style>
