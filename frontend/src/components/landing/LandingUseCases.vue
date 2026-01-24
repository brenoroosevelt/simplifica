<template>
  <section id="casos-de-uso" class="use-cases-section">
    <v-container>
      <div class="section-header">
        <div class="section-tag">Casos de Uso</div>
        <h2 class="section-title">
          Solução completa para diversos segmentos
        </h2>
        <p class="section-subtitle">
          Atendemos instituições públicas e privadas com necessidades específicas de gestão de processos e riscos
        </p>
      </div>

      <v-row class="d-flex justify-center mb-12">
        <v-col
          v-for="(useCase, index) in useCases"
          :key="useCase.title"
          cols="12"
          sm="6"
          md="3"
        >
          <div
            class="use-case-card"
            :class="`use-case-${index + 1}`"
          >
            <div class="use-case-icon-wrapper">
              <v-icon :icon="useCase.icon" size="40" color="white" />
            </div>

            <h3 class="use-case-title">{{ useCase.title }}</h3>
            <p class="use-case-description">{{ useCase.description }}</p>

            <div class="use-case-tags">
              <span
                v-for="tag in useCase.tags"
                :key="tag"
                class="use-case-tag"
              >
                {{ tag }}
              </span>
            </div>

            <div class="use-case-benefits">
              <div
                v-for="benefit in useCase.benefits"
                :key="benefit"
                class="benefit-item"
              >
                <v-icon icon="mdi-chevron-right" size="16" color="primary" />
                <span>{{ benefit }}</span>
              </div>
            </div>
          </div>
        </v-col>
      </v-row>

      <!-- Industry Logos -->
      <div class="industry-logos">
        <div class="industry-logo-item">
          <v-icon icon="mdi-city" size="48" color="primary" />
          <span>Prefeituras</span>
        </div>
        <div class="industry-logo-item">
          <v-icon icon="mdi-gavel" size="48" color="primary" />
          <span>Tribunais</span>
        </div>
        <div class="industry-logo-item">
          <v-icon icon="mdi-domain" size="48" color="primary" />
          <span>Autarquias</span>
        </div>
        <div class="industry-logo-item">
          <v-icon icon="mdi-office-building" size="48" color="primary" />
          <span>Empresas</span>
        </div>
        <div class="industry-logo-item">
          <v-icon icon="mdi-bank" size="48" color="primary" />
          <span>Instituições Financeiras</span>
        </div>
        <div class="industry-logo-item">
          <v-icon icon="mdi-hospital-building" size="48" color="primary" />
          <span>Saúde</span>
        </div>
      </div>
    </v-container>
  </section>
</template>

<script setup lang="ts">
const useCases = [
  {
    icon: 'mdi-city-variant',
    title: 'Instituições Públicas',
    description: 'Gestão de processos administrativos, conformidade com TCU/CGU e transparência institucional para órgãos governamentais.',
    tags: ['Governo', 'Autarquias', 'Fundações'],
    benefits: [
      'Conformidade com órgãos de controle',
      'Transparência e accountability',
      'Gestão de riscos institucionais',
    ],
  },
  {
    icon: 'mdi-office-building',
    title: 'Empresas Privadas',
    description: 'Otimização de processos corporativos, gestão de riscos operacionais e compliance para empresas de médio e grande porte.',
    tags: ['Corporativo', 'Indústria', 'Serviços'],
    benefits: [
      'Redução de custos operacionais',
      'Aumento de eficiência',
      'Conformidade regulatória',
    ],
  },
  {
    icon: 'mdi-cog-sync',
    title: 'Escritórios de Processos',
    description: 'Centralização da gestão de processos institucionais, modelagem BPMN e governança para escritórios BPM e PMO.',
    tags: ['BPM Office', 'PMO', 'Gestão'],
    benefits: [
      'Centralização de processos',
      'Padronização metodológica',
      'Indicadores consolidados',
    ],
  },
  {
    icon: 'mdi-shield-check',
    title: 'Compliance e Auditoria',
    description: 'Controles internos, gestão de riscos corporativos, trilhas de auditoria e conformidade regulatória para áreas de compliance.',
    tags: ['GRC', 'Auditoria', 'Controles'],
    benefits: [
      'Controles automatizados',
      'Rastreabilidade completa',
      'Evidências para auditorias',
    ],
  },
]
</script>

<style scoped lang="scss">
.use-cases-section {
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
  background: rgba(var(--v-theme-warning), 0.08);
  color: rgb(var(--v-theme-warning));
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

.use-case-card {
  width: 100%;
}

.use-case-card {
  height: 100%;
  padding: 32px;
  background: white;
  border-radius: 16px;
  border: 2px solid #e2e8f0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;

  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
    border-color: rgb(var(--v-theme-primary));
  }

  &.use-case-1 .use-case-icon-wrapper {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  }

  &.use-case-2 .use-case-icon-wrapper {
    background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  }

  &.use-case-3 .use-case-icon-wrapper {
    background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  }

  &.use-case-4 .use-case-icon-wrapper {
    background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  }
}

.use-case-icon-wrapper {
  width: 72px;
  height: 72px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px auto;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.use-case-title {
  font-size: 20px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 12px;
  line-height: 1.3;
}

.use-case-description {
  font-size: 15px;
  line-height: 1.6;
  color: #64748b;
  margin-bottom: 20px;
}

.use-case-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-bottom: 24px;
}

.use-case-tag {
  padding: 4px 12px;
  background: #f1f5f9;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  color: #475569;
}

.use-case-benefits {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-top: 24px;
  border-top: 2px solid #f1f5f9;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #475569;
  font-weight: 500;

  span {
    line-height: 1.4;
  }
}

.industry-logos {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 32px;
  padding: 48px 0;
  border-top: 2px solid #f1f5f9;
}

.industry-logo-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  opacity: 0.7;
  transition: opacity 0.3s ease;

  &:hover {
    opacity: 1;
  }

  span {
    font-size: 14px;
    font-weight: 600;
    color: #475569;
    text-align: center;
  }
}

@media (max-width: 960px) {
  .use-cases-section {
    padding: 80px 0;
  }

  .section-header {
    margin-bottom: 48px;
  }

  .use-cases-grid {
    margin-bottom: 48px;
  }

  .industry-logos {
    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
    gap: 24px;
  }
}

@media (max-width: 600px) {
  .use-case-card {
    padding: 24px;
  }

  .industry-logos {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
