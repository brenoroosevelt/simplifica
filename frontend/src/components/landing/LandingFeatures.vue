<template>
  <section id="funcionalidades" class="features-section">
    <v-container>
      <div class="section-header">
        <div class="section-tag">Funcionalidades</div>
        <h2 class="section-title">
          Recursos poderosos para gestão completa
        </h2>
        <p class="section-subtitle">
          Ferramentas profissionais integradas para modelar, analisar e otimizar processos institucionais
        </p>
      </div>

      <div class="features-list">
        <div
          v-for="(feature, index) in features"
          :key="feature.title"
          class="feature-row"
          :class="{ 'feature-reverse': index % 2 !== 0 }"
        >
          <div class="feature-content">
            <div class="feature-badge">
              <v-icon :icon="feature.icon" size="20" />
              <span>{{ feature.badge }}</span>
            </div>

            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-description">{{ feature.description }}</p>

            <div class="feature-list">
              <div
                v-for="item in feature.items"
                :key="item"
                class="feature-list-item"
              >
                <v-icon icon="mdi-check-circle" size="20" color="primary" />
                <span>{{ item }}</span>
              </div>
            </div>
          </div>

          <div class="feature-visual">
            <div class="visual-card" :class="`visual-${index + 1}`">
              <div class="visual-header">
                <v-icon :icon="feature.icon" size="48" color="white" />
              </div>
              <div class="visual-content">
                <div
                  v-for="(bar, i) in feature.visualBars"
                  :key="i"
                  class="visual-bar"
                  :style="{ width: bar + '%', animationDelay: i * 0.1 + 's' }"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </v-container>
  </section>
</template>

<script setup lang="ts">
const features = [
  {
    icon: 'mdi-pencil-ruler',
    badge: 'Modelagem',
    title: 'Editor Visual de Processos BPMN',
    description: 'Crie diagramas de processos com notação BPMN 2.0 através de um editor intuitivo, com validação automática e biblioteca de templates.',
    items: [
      'Interface drag-and-drop profissional',
      'Validação automática de conformidade BPMN',
      'Biblioteca com 50+ templates pré-configurados',
      'Exportação em múltiplos formatos (PDF, PNG, XML)',
    ],
    visualBars: [85, 72, 95, 68, 88],
  },
  {
    icon: 'mdi-target',
    badge: 'Análise',
    title: 'Mapa de Riscos Interativo',
    description: 'Visualize e gerencie riscos em uma matriz dinâmica, com análise de probabilidade, impacto e priorização automática de ações.',
    items: [
      'Matriz de riscos personalizável',
      'Análise de probabilidade e impacto',
      'Cadastro de controles e ações mitigadoras',
      'Alertas automáticos para riscos críticos',
    ],
    visualBars: [78, 92, 65, 88, 74],
  },
  {
    icon: 'mdi-speedometer',
    badge: 'Métricas',
    title: 'Indicadores e KPIs em Tempo Real',
    description: 'Acompanhe o desempenho institucional através de dashboards personalizáveis com indicadores estratégicos e operacionais.',
    items: [
      'Dashboards personalizáveis por perfil',
      'KPIs estratégicos e operacionais',
      'Atualização em tempo real',
      'Alertas de metas e desvios',
    ],
    visualBars: [90, 75, 82, 95, 70],
  },
  {
    icon: 'mdi-file-chart',
    badge: 'Relatórios',
    title: 'Relatórios Gerenciais Avançados',
    description: 'Gere relatórios executivos, operacionais e de auditoria com dados consolidados e visualizações profissionais.',
    items: [
      'Relatórios executivos para alta gestão',
      'Documentos para auditorias e órgãos de controle',
      'Exportação em PDF, Excel e Word',
      'Agendamento automático de envio',
    ],
    visualBars: [88, 72, 78, 85, 92],
  },
  {
    icon: 'mdi-history',
    badge: 'Auditoria',
    title: 'Trilha de Auditoria Completa',
    description: 'Registre automaticamente todas as ações, alterações e acessos ao sistema com histórico completo e rastreabilidade total.',
    items: [
      'Registro automático de todas as ações',
      'Histórico completo de alterações',
      'Rastreabilidade por usuário e data',
      'Conformidade com requisitos de auditoria',
    ],
    visualBars: [95, 88, 92, 78, 85],
  },
]
</script>

<style scoped lang="scss">
.features-section {
  padding: 120px 0;
  background: white;
}

.section-header {
  text-align: center;
  margin-bottom: 80px;
}

.section-tag {
  display: inline-block;
  padding: 6px 16px;
  background: rgba(var(--v-theme-info), 0.08);
  color: rgb(var(--v-theme-info));
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 16px;
}

.section-title {
  font-size: clamp(32px, 4vw, 44px);
  font-weight: 800;
  line-height: 1.2;
  color: #0f172a;
  margin-bottom: 16px;
  letter-spacing: -0.5px;
}

.section-subtitle {
  font-size: 18px;
  line-height: 1.6;
  color: #64748b;
  max-width: 640px;
  margin: 0 auto;
}

.features-list {
  display: flex;
  flex-direction: column;
  gap: 120px;
}

.feature-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 80px;
  align-items: center;

  &.feature-reverse {
    .feature-content {
      order: 2;
    }

    .feature-visual {
      order: 1;
    }
  }
}

.feature-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.feature-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(var(--v-theme-primary), 0.08);
  border-radius: 24px;
  width: fit-content;
  color: rgb(var(--v-theme-primary));
  font-weight: 600;
  font-size: 14px;
}

.feature-title {
  font-size: clamp(24px, 3vw, 32px);
  font-weight: 800;
  line-height: 1.2;
  color: #0f172a;
  letter-spacing: -0.5px;
}

.feature-description {
  font-size: 16px;
  line-height: 1.6;
  color: #64748b;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 8px;
}

.feature-list-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;

  span {
    font-size: 15px;
    line-height: 1.5;
    color: #475569;
    font-weight: 500;
  }
}

.feature-visual {
  display: flex;
  align-items: center;
  justify-content: center;
}

.visual-card {
  width: 100%;
  max-width: 480px;
  padding: 32px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.12);
  border: 1px solid #e2e8f0;

  &.visual-1 .visual-header {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  }

  &.visual-2 .visual-header {
    background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  }

  &.visual-3 .visual-header {
    background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  }

  &.visual-4 .visual-header {
    background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  }

  &.visual-5 .visual-header {
    background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%);
  }
}

.visual-header {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32px;
}

.visual-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.visual-bar {
  height: 32px;
  background: linear-gradient(90deg, rgb(var(--v-theme-primary)), rgba(var(--v-theme-primary), 0.6));
  border-radius: 8px;
  animation: growBar 1s ease-out forwards;
  opacity: 0;
}

@keyframes growBar {
  from {
    width: 0 !important;
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@media (max-width: 960px) {
  .features-section {
    padding: 80px 0;
  }

  .section-header {
    margin-bottom: 48px;
  }

  .features-list {
    gap: 80px;
  }

  .feature-row {
    grid-template-columns: 1fr;
    gap: 40px;

    &.feature-reverse {
      .feature-content,
      .feature-visual {
        order: unset;
      }
    }
  }

  .visual-card {
    max-width: 100%;
  }
}

@media (max-width: 600px) {
  .visual-card {
    padding: 24px;
  }

  .visual-header {
    width: 64px;
    height: 64px;
  }
}
</style>
