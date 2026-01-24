<template>
  <section id="inicio" class="hero-section">
    <v-container class="hero-container">
      <v-row align="center" class="hero-row">
        <v-col cols="12" md="6" class="hero-content">
          <div class="hero-badge">
            <v-icon icon="mdi-star-four-points" size="16" color="primary" />
            <span>Gestão de processos de alto nível</span>
          </div>

          <h1 class="hero-title">
            Transforme a gestão de processos da sua instituição
          </h1>

          <p class="hero-subtitle">
            Simplifique a modelagem BPMN, gerencie riscos estratégicos e operacionais,
            avalie a maturidade institucional e garanta governança com o sistema BPM
            mais completo e profissional do mercado.
          </p>

          <div class="hero-actions">
            <v-btn
              color="primary"
              size="x-large"
              variant="tonal"
              class="hero-cta-primary"
              @click="handleDemoClick"
            >
              <v-icon icon="mdi-calendar-check" start />
              Demonstração
            </v-btn>

            <v-btn
              color="primary"
              size="x-large"
              variant="outlined"
              class="hero-cta-secondary"
              @click="handleContactClick"
            >
              <v-icon icon="mdi-chat-processing" start />
              Falar com especialista
            </v-btn>
          </div>

          <div class="hero-stats">
            <div class="stat-item">
              <div class="stat-number">500+</div>
              <div class="stat-label">Processos mapeados</div>
            </div>
            <div class="stat-divider" />
            <div class="stat-item">
              <div class="stat-number">98%</div>
              <div class="stat-label">Satisfação dos usuários</div>
            </div>
            <div class="stat-divider" />
            <div class="stat-item">
              <div class="stat-number">24/7</div>
              <div class="stat-label">Suporte dedicado</div>
            </div>
          </div>
        </v-col>

        <v-col cols="12" md="6" class="hero-visual">
          <div class="mockup-container">
            <div class="mockup-window">
              <div class="mockup-header">
                <div class="mockup-dots">
                  <span class="dot dot-red" />
                  <span class="dot dot-yellow" />
                  <span class="dot dot-green" />
                </div>
                <div class="mockup-title">Dashboard BPM</div>
              </div>
              <div class="mockup-content">
                <div class="mockup-grid">
                  <div class="mockup-card card-primary">
                    <v-icon icon="mdi-file-tree" size="32" color="white" />
                    <div class="card-label">Processos</div>
                  </div>
                  <div class="mockup-card card-success">
                    <v-icon icon="mdi-shield-check" size="32" color="white" />
                    <div class="card-label">Riscos</div>
                  </div>
                  <div class="mockup-card card-info">
                    <v-icon icon="mdi-chart-line" size="32" color="white" />
                    <div class="card-label">Maturidade</div>
                  </div>
                  <div class="mockup-card card-warning">
                    <v-icon icon="mdi-gavel" size="32" color="white" />
                    <div class="card-label">Governança</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </v-col>
      </v-row>
    </v-container>

    <!-- Decorative Elements -->
    <div class="hero-decoration hero-decoration-1" />
    <div class="hero-decoration hero-decoration-2" />
  </section>
</template>

<script setup lang="ts">
const handleDemoClick = () => {
  const element = document.getElementById('contato')
  if (element) {
    const headerOffset = 72
    const elementPosition = element.getBoundingClientRect().top
    const offsetPosition = elementPosition + window.pageYOffset - headerOffset

    window.scrollTo({
      top: offsetPosition,
      behavior: 'smooth'
    })
  }
}

const handleContactClick = () => {
  handleDemoClick()
}
</script>

<style scoped lang="scss">
.hero-section {
  position: relative;
  min-height: calc(100vh - 72px);
  padding: 80px 0;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  overflow: hidden;
}

.hero-container {
  max-width: 1280px;
}

.hero-row {
  min-height: calc(100vh - 232px);
}

.hero-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 40px 24px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(var(--v-theme-primary), 0.08);
  border-radius: 24px;
  width: fit-content;

  span {
    font-size: 14px;
    font-weight: 600;
    color: rgb(var(--v-theme-primary));
  }
}

.hero-title {
  font-size: clamp(36px, 5vw, 56px);
  font-weight: 600;
  line-height: 1.15;
  color: #0f172a;
  letter-spacing: -1px;
  margin: 0;
}

.hero-subtitle {
  font-size: clamp(16px, 2vw, 20px);
  line-height: 1.6;
  color: #475569;
  margin: 0;
  font-weight: 400;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 8px;
}

.hero-cta-primary,
.hero-cta-secondary {
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0;
  padding: 0 32px;
  height: 56px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  }
}

.hero-stats {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-top: 24px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: rgb(var(--v-theme-primary));
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: #e2e8f0;
}

.hero-visual {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}

.mockup-container {
  width: 100%;
  max-width: 540px;
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

.mockup-window {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.12);
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.mockup-header {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.mockup-dots {
  display: flex;
  gap: 8px;
  margin-right: 16px;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;

  &.dot-red { background: #f87171; }
  &.dot-yellow { background: #fbbf24; }
  &.dot-green { background: #4ade80; }
}

.mockup-title {
  font-size: 14px;
  font-weight: 600;
  color: #475569;
}

.mockup-content {
  padding: 32px;
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
}

.mockup-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.mockup-card {
  padding: 24px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  transition: transform 0.3s ease;

  &:hover {
    transform: translateY(-4px);
  }

  &.card-primary { background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); }
  &.card-success { background: linear-gradient(135deg, #10b981 0%, #059669 100%); }
  &.card-info { background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%); }
  &.card-warning { background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%); }
}

.card-label {
  color: white;
  font-size: 14px;
  font-weight: 600;
}

.hero-decoration {
  position: absolute;
  border-radius: 50%;
  opacity: 0.3;
  filter: blur(80px);
  pointer-events: none;
}

.hero-decoration-1 {
  width: 400px;
  height: 400px;
  background: rgb(var(--v-theme-primary));
  top: -200px;
  right: -200px;
}

.hero-decoration-2 {
  width: 300px;
  height: 300px;
  background: rgb(var(--v-theme-success));
  bottom: -150px;
  left: -150px;
}

@media (max-width: 960px) {
  .hero-section {
    padding: 40px 0;
  }

  .hero-row {
    min-height: auto;
  }

  .hero-stats {
    justify-content: space-between;
  }

  .stat-divider {
    display: none;
  }

  .mockup-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .hero-actions {
    flex-direction: column;

    button {
      width: 100%;
    }
  }
}
</style>
