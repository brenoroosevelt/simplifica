<template>
  <section id="solucoes" class="solutions-section">
    <v-container>
      <div class="section-header">
        <div class="section-tag">Soluções</div>
        <h2 class="section-title">
          Uma plataforma completa para gestão institucional
        </h2>
        <p class="section-subtitle">
          Integre todos os aspectos da gestão de processos em um único sistema inteligente
        </p>
      </div>

      <v-row class="d-flex justify-center">
        <v-col
          v-for="(solution, index) in solutions"
          :key="solution.title"
          cols="12"
          sm="6"
          md="3"
        >
          <div
            class="solution-card"
            :class="`solution-${index + 1}`"
            @mouseenter="hoveredCard = index"
            @mouseleave="hoveredCard = null"
          >
            <div class="solution-icon-wrapper">
              <v-icon :icon="solution.icon" size="40" color="white" />
            </div>

            <h3 class="solution-title">{{ solution.title }}</h3>
            <p class="solution-description">{{ solution.description }}</p>

            <div class="solution-features">
              <div
                v-for="feature in solution.features"
                :key="feature"
                class="feature-item"
              >
                <v-icon icon="mdi-check-circle" size="16" color="success" />
                <span>{{ feature }}</span>
              </div>
            </div>
          </div>
        </v-col>
      </v-row>
    </v-container>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const hoveredCard = ref<number | null>(null)

const solutions = [
  {
    icon: 'mdi-file-tree',
    title: 'Modelagem BPM',
    description: 'Mapeie e documente processos com notação BPMN 2.0 profissional',
    features: [
      'Editor visual intuitivo',
      'Biblioteca de templates',
      'Validação automática',
    ],
  },
  {
    icon: 'mdi-shield-alert',
    title: 'Gestão de Riscos',
    description: 'Identifique, analise e mitigue riscos estratégicos e operacionais',
    features: [
      'Matriz de riscos',
      'Análise de impacto',
      'Planos de mitigação',
    ],
  },
  {
    icon: 'mdi-chart-line',
    title: 'Maturidade Institucional',
    description: 'Avalie e evolua o nível de maturidade dos processos',
    features: [
      'Diagnóstico completo',
      'Métricas personalizadas',
      'Planos de evolução',
    ],
  },
  {
    icon: 'mdi-gavel',
    title: 'Governança',
    description: 'Garanta conformidade, auditoria e transparência institucional',
    features: [
      'Trilhas de auditoria',
      'Relatórios gerenciais',
      'Controles de acesso',
    ],
  },
]
</script>

<style scoped lang="scss">
.solutions-section {
  padding: 120px 0;
  background: white;
  position: relative;
}

.section-header {
  text-align: center;
  margin-bottom: 80px;
}

.section-tag {
  display: inline-block;
  padding: 6px 16px;
  background: rgba(var(--v-theme-primary), 0.08);
  color: rgb(var(--v-theme-primary));
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 16px;
}

.section-title {
  font-size: clamp(32px, 4vw, 44px);
  font-weight: 600;
  line-height: 1.2;
  color: #0f172a;
  margin-bottom: 16px;
  letter-spacing: -0.5px;
}

.section-subtitle {
  font-size: 18px;
  line-height: 1.6;
  color: #64748b;
  max-width: 600px;
  margin: 0 auto;
}

.solution-card {
  width: 100%;
}

.solution-card {
  height: 100%;
  padding: 32px;
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;

  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
    border-color: rgb(var(--v-theme-primary));
  }

  &.solution-1 .solution-icon-wrapper {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  }

  &.solution-2 .solution-icon-wrapper {
    background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  }

  &.solution-3 .solution-icon-wrapper {
    background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  }

  &.solution-4 .solution-icon-wrapper {
    background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  }
}

.solution-icon-wrapper {
  width: 72px;
  height: 72px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px auto;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.solution-title {
  font-size: 22px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 12px;
  line-height: 1.3;
}

.solution-description {
  font-size: 15px;
  line-height: 1.6;
  color: #64748b;
  margin-bottom: 24px;
}

.solution-features {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 24px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #475569;
  font-weight: 500;

  span {
    line-height: 1.4;
  }
}

@media (max-width: 960px) {
  .solutions-section {
    padding: 80px 0;
  }

  .section-header {
    margin-bottom: 48px;
  }
}

@media (max-width: 600px) {
  .solution-card {
    padding: 24px;
  }

  .solution-icon-wrapper {
    width: 64px;
    height: 64px;
  }
}
</style>
