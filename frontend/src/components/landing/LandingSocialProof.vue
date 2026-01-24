<template>
  <section class="social-proof-section">
    <v-container>
      <div class="section-header">
        <div class="section-tag">Prova Social</div>
        <h2 class="section-title">
          Confiança de instituições em todo o Brasil
        </h2>
        <p class="section-subtitle">
          Dezenas de instituições públicas e privadas já transformaram sua gestão de processos com o Simplifica
        </p>
      </div>

      <!-- Stats -->
      <div class="stats-container">
        <div class="stat-card">
          <v-icon icon="mdi-domain" size="40" color="primary" />
          <div class="stat-number">150+</div>
          <div class="stat-label">Instituições atendidas</div>
        </div>
        <div class="stat-card">
          <v-icon icon="mdi-file-tree" size="40" color="success" />
          <div class="stat-number">2.500+</div>
          <div class="stat-label">Processos mapeados</div>
        </div>
        <div class="stat-card">
          <v-icon icon="mdi-account-group" size="40" color="info" />
          <div class="stat-number">5.000+</div>
          <div class="stat-label">Usuários ativos</div>
        </div>
        <div class="stat-card">
          <v-icon icon="mdi-star" size="40" color="warning" />
          <div class="stat-number">4.8/5</div>
          <div class="stat-label">Avaliação média</div>
        </div>
      </div>

      <!-- Testimonials -->
      <div class="testimonials-container">
        <v-row>
          <v-col
            v-for="(testimonial, index) in testimonials"
            :key="index"
            cols="12"
            md="4"
          >
            <div class="testimonial-card">
              <div class="testimonial-stars">
                <v-icon
                  v-for="i in 5"
                  :key="i"
                  icon="mdi-star"
                  size="20"
                  color="warning"
                />
              </div>

              <p class="testimonial-text">
                "{{ testimonial.text }}"
              </p>

              <div class="testimonial-author">
                <div class="author-avatar">
                  <v-icon :icon="testimonial.icon" size="28" color="primary" />
                </div>
                <div class="author-info">
                  <div class="author-name">{{ testimonial.name }}</div>
                  <div class="author-role">{{ testimonial.role }}</div>
                  <div class="author-institution">{{ testimonial.institution }}</div>
                </div>
              </div>
            </div>
          </v-col>
        </v-row>
      </div>

      <!-- Certifications -->
      <div class="certifications">
        <div class="certification-item">
          <v-icon icon="mdi-shield-check" size="32" color="success" />
          <div class="certification-text">
            <div class="certification-title">ISO 9001:2015</div>
            <div class="certification-subtitle">Certificado de Qualidade</div>
          </div>
        </div>
        <div class="certification-divider" />
        <div class="certification-item">
          <v-icon icon="mdi-lock-check" size="32" color="primary" />
          <div class="certification-text">
            <div class="certification-title">LGPD Compliant</div>
            <div class="certification-subtitle">Proteção de Dados</div>
          </div>
        </div>
        <div class="certification-divider" />
        <div class="certification-item">
          <v-icon icon="mdi-security" size="32" color="info" />
          <div class="certification-text">
            <div class="certification-title">SSL/TLS</div>
            <div class="certification-subtitle">Segurança Avançada</div>
          </div>
        </div>
        <div class="certification-divider" />
        <div class="certification-item">
          <v-icon icon="mdi-server-security" size="32" color="warning" />
          <div class="certification-text">
            <div class="certification-title">Backup Diário</div>
            <div class="certification-subtitle">Dados Protegidos</div>
          </div>
        </div>
      </div>
    </v-container>
  </section>
</template>

<script setup lang="ts">
const testimonials = [
  {
    icon: 'mdi-account-tie',
    text: 'O Simplifica transformou nossa gestão de processos. Conseguimos mapear todos os processos críticos e reduzir drasticamente nossos riscos operacionais.',
    name: 'Carlos Eduardo Silva',
    role: 'Diretor de Processos',
    institution: 'Tribunal Regional Federal',
  },
  {
    icon: 'mdi-account-circle',
    text: 'A facilidade de uso e a qualidade dos relatórios gerenciais nos permitiu atender todas as exigências do TCU com muito mais eficiência e transparência.',
    name: 'Maria Fernanda Costa',
    role: 'Gerente de Governança',
    institution: 'Prefeitura Municipal',
  },
  {
    icon: 'mdi-account-star',
    text: 'Implementamos o sistema em toda a organização e o retorno foi imediato. A maturidade dos nossos processos aumentou significativamente em apenas 6 meses.',
    name: 'Roberto Almeida',
    role: 'CIO - Chief Information Officer',
    institution: 'Empresa de Grande Porte',
  },
]
</script>

<style scoped lang="scss">
.social-proof-section {
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
  background: rgba(var(--v-theme-accent), 0.08);
  color: rgb(var(--v-theme-accent));
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

.stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
  margin-bottom: 80px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 40px 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.08);
  }
}

.stat-number {
  font-size: 36px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
  text-align: center;
}

.testimonials-container {
  margin-bottom: 80px;
}

.testimonial-card {
  height: 100%;
  padding: 32px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  gap: 24px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.08);
  }
}

.testimonial-stars {
  display: flex;
  gap: 4px;
}

.testimonial-text {
  font-size: 16px;
  line-height: 1.7;
  color: #475569;
  font-style: italic;
  flex-grow: 1;
}

.testimonial-author {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-top: 24px;
  border-top: 2px solid #f1f5f9;
}

.author-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: rgba(var(--v-theme-primary), 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.author-role {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.author-institution {
  font-size: 13px;
  color: #94a3b8;
  font-weight: 500;
}

.certifications {
  display: flex;
  align-items: center;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 24px;
  padding: 32px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}

.certification-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.certification-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.certification-title {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.certification-subtitle {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.certification-divider {
  width: 1px;
  height: 48px;
  background: #e2e8f0;
}

@media (max-width: 960px) {
  .social-proof-section {
    padding: 80px 0;
  }

  .section-header {
    margin-bottom: 48px;
  }

  .stats-container {
    margin-bottom: 48px;
  }

  .testimonials-container {
    margin-bottom: 48px;
  }

  .certification-divider {
    display: none;
  }
}

@media (max-width: 600px) {
  .stat-card {
    padding: 32px 24px;
  }

  .testimonial-card {
    padding: 24px;
  }

  .certifications {
    padding: 24px;
    gap: 16px;
  }

  .certification-item {
    flex-direction: column;
    text-align: center;
  }
}
</style>
