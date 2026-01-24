<template>
  <section id="beneficios" class="benefits-section">
    <v-container>
      <div class="section-header">
        <div class="section-tag">Benefícios</div>
        <h2 class="section-title">
          Por que escolher o Simplifica?
        </h2>
        <p class="section-subtitle">
          Impacto mensurável em eficiência, governança e tomada de decisão estratégica
        </p>
      </div>

      <v-row class="d-flex justify-center">
        <v-col
          v-for="benefit in benefits"
          :key="benefit.title"
          cols="12"
          sm="6"
          md="4"
        >
          <div class="benefit-card">
            <div class="benefit-icon-container" :class="benefit.iconClass">
              <v-icon :icon="benefit.icon" size="32" />
            </div>

            <h3 class="benefit-title">{{ benefit.title }}</h3>
            <p class="benefit-description">{{ benefit.description }}</p>

            <div class="benefit-metric">
              <span class="metric-value">{{ benefit.metric }}</span>
              <span class="metric-label">{{ benefit.metricLabel }}</span>
            </div>
          </div>
        </v-col>
      </v-row>

      <!-- Additional Value Props -->
      <div class="value-props">
        <div class="value-prop-item">
          <v-icon icon="mdi-shield-check" size="24" color="success" />
          <span>Certificação ISO 9001</span>
        </div>
        <div class="value-prop-divider" />
        <div class="value-prop-item">
          <v-icon icon="mdi-lock" size="24" color="primary" />
          <span>LGPD Compliant</span>
        </div>
        <div class="value-prop-divider" />
        <div class="value-prop-item">
          <v-icon icon="mdi-cloud-check" size="24" color="info" />
          <span>Cloud & On-premise</span>
        </div>
        <div class="value-prop-divider" />
        <div class="value-prop-item">
          <v-icon icon="mdi-account-group" size="24" color="warning" />
          <span>Suporte dedicado</span>
        </div>
      </div>
    </v-container>
  </section>
</template>

<script setup lang="ts">
const benefits = [
  {
    icon: 'mdi-chart-timeline-variant',
    iconClass: 'icon-primary',
    title: 'Padronização de Processos',
    description: 'Estabeleça padrões claros e consistentes para todos os processos institucionais, eliminando variações e retrabalho.',
    metric: '60%',
    metricLabel: 'Redução no tempo de execução',
  },
  {
    icon: 'mdi-alert-decagram',
    iconClass: 'icon-error',
    title: 'Redução de Riscos',
    description: 'Identifique e mitigue riscos operacionais e estratégicos antes que se tornem problemas críticos para a instituição.',
    metric: '85%',
    metricLabel: 'Menos incidentes críticos',
  },
  {
    icon: 'mdi-eye',
    iconClass: 'icon-info',
    title: 'Transparência Total',
    description: 'Visibilidade completa sobre todos os processos, riscos e indicadores para gestores e órgãos de controle.',
    metric: '100%',
    metricLabel: 'Rastreabilidade de ações',
  },
  {
    icon: 'mdi-chart-bar',
    iconClass: 'icon-success',
    title: 'Decisões Baseadas em Dados',
    description: 'Tome decisões estratégicas fundamentadas em indicadores, métricas e análises confiáveis e em tempo real.',
    metric: '3x',
    metricLabel: 'Mais velocidade nas decisões',
  },
  {
    icon: 'mdi-check-decagram',
    iconClass: 'icon-warning',
    title: 'Conformidade e Auditoria',
    description: 'Atenda requisitos de órgãos de controle, auditorias externas e legislação com trilhas completas e documentação.',
    metric: '100%',
    metricLabel: 'Aprovação em auditorias',
  },
  {
    icon: 'mdi-rocket-launch',
    iconClass: 'icon-accent',
    title: 'Melhoria Contínua',
    description: 'Evolua constantemente o nível de maturidade institucional com ciclos estruturados de análise e aprimoramento.',
    metric: '+40%',
    metricLabel: 'Aumento de maturidade/ano',
  },
]
</script>

<style scoped lang="scss">
.benefits-section {
  padding: 120px 0;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  position: relative;
}

.section-header {
  text-align: center;
  margin-bottom: 80px;
}

.section-tag {
  display: inline-block;
  padding: 6px 16px;
  background: rgba(var(--v-theme-success), 0.08);
  color: rgb(var(--v-theme-success));
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
  max-width: 640px;
  margin: 0 auto;
}

.benefit-card {
  width: 100%;
}

.benefit-card {
  height: 100%;
  padding: 32px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  text-align: center;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.08);
  }
}

.benefit-icon-container {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px auto;

  &.icon-primary {
    background: rgba(var(--v-theme-primary), 0.1);
    color: rgb(var(--v-theme-primary));
  }

  &.icon-error {
    background: rgba(var(--v-theme-error), 0.1);
    color: rgb(var(--v-theme-error));
  }

  &.icon-info {
    background: rgba(var(--v-theme-info), 0.1);
    color: rgb(var(--v-theme-info));
  }

  &.icon-success {
    background: rgba(var(--v-theme-success), 0.1);
    color: rgb(var(--v-theme-success));
  }

  &.icon-warning {
    background: rgba(var(--v-theme-warning), 0.1);
    color: rgb(var(--v-theme-warning));
  }

  &.icon-accent {
    background: rgba(var(--v-theme-accent), 0.1);
    color: rgb(var(--v-theme-accent));
  }
}

.benefit-title {
  font-size: 20px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 12px;
  line-height: 1.3;
}

.benefit-description {
  font-size: 15px;
  line-height: 1.6;
  color: #64748b;
  margin-bottom: 24px;
  flex-grow: 1;
}

.benefit-metric {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 8px;
  padding-top: 24px;
  border-top: 2px solid #f1f5f9;
}

.metric-value {
  font-size: 32px;
  font-weight: 800;
  color: rgb(var(--v-theme-primary));
  line-height: 1;
}

.metric-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.value-props {
  margin-top: 80px;
  padding: 32px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 24px;
}

.value-prop-item {
  display: flex;
  align-items: center;
  gap: 12px;

  span {
    font-size: 15px;
    font-weight: 600;
    color: #475569;
  }
}

.value-prop-divider {
  width: 1px;
  height: 40px;
  background: #e2e8f0;
}

@media (max-width: 960px) {
  .benefits-section {
    padding: 80px 0;
  }

  .section-header {
    margin-bottom: 48px;
  }

  .value-props {
    margin-top: 48px;
  }

  .value-prop-divider {
    display: none;
  }
}

@media (max-width: 600px) {
  .benefit-card {
    padding: 24px;
  }

  .value-props {
    padding: 24px;
    gap: 16px;
  }

  .value-prop-item {
    flex-direction: column;
    text-align: center;
    gap: 8px;
  }
}
</style>
