<template>
  <section id="contato" class="final-cta-section">
    <v-container>
      <div class="cta-card">
        <div class="cta-decoration cta-decoration-1" />
        <div class="cta-decoration cta-decoration-2" />

        <div class="cta-content">
          <div class="cta-badge">
            <v-icon icon="mdi-rocket-launch" size="20" />
            <span>Comece agora</span>
          </div>

          <h2 class="cta-title">
            Leve sua instituição para o próximo nível de maturidade
          </h2>

          <p class="cta-subtitle">
            Agende uma demonstração personalizada e descubra como o Simplifica pode transformar a gestão de processos, riscos e governança da sua instituição.
          </p>

          <div class="cta-actions">
            <v-btn
              color="primary"
              size="x-large"
              variant="tonal"
              class="cta-button-primary"
              @click="handleDemoClick"
            >
              <v-icon icon="mdi-calendar-check" start />
              Demonstração
            </v-btn>

            <v-btn
              color="white"
              size="x-large"
              variant="outlined"
              class="cta-button-secondary"
              @click="handleContactClick"
            >
              <v-icon icon="mdi-chat-processing" start />
              Falar com especialista
            </v-btn>
          </div>

          <div class="cta-features">
            <div class="cta-feature-item">
              <v-icon icon="mdi-check-circle" size="20" color="success" />
              <span>Demonstração gratuita e sem compromisso</span>
            </div>
            <div class="cta-feature-item">
              <v-icon icon="mdi-check-circle" size="20" color="success" />
              <span>Implementação assistida por especialistas</span>
            </div>
            <div class="cta-feature-item">
              <v-icon icon="mdi-check-circle" size="20" color="success" />
              <span>Suporte técnico dedicado e treinamento incluído</span>
            </div>
          </div>
        </div>

        <!-- Contact Form -->
        <div class="contact-form-wrapper">
          <div class="form-header">
            <v-icon icon="mdi-message-text" size="32" color="primary" />
            <h3 class="form-title">Solicite contato</h3>
            <p class="form-subtitle">Preencha os dados e retornaremos em até 24h</p>
          </div>

          <v-form ref="contactForm" @submit.prevent="handleSubmit">
            <v-text-field
              v-model="formData.name"
              label="Nome completo"
              variant="outlined"
              :rules="[rules.required]"
              required
            />

            <v-text-field
              v-model="formData.email"
              label="E-mail institucional"
              variant="outlined"
              type="email"
              :rules="[rules.required, rules.email]"
              required
            />

            <v-text-field
              v-model="formData.phone"
              label="Telefone"
              variant="outlined"
              :rules="[rules.required]"
              required
            />

            <v-text-field
              v-model="formData.institution"
              label="Instituição"
              variant="outlined"
              :rules="[rules.required]"
              required
            />

            <v-select
              v-model="formData.segment"
              label="Segmento"
              variant="outlined"
              :items="segments"
              :rules="[rules.required]"
              required
            />

            <v-textarea
              v-model="formData.message"
              label="Mensagem (opcional)"
              variant="outlined"
              rows="3"
            />

            <v-btn
              type="submit"
              color="primary"
              size="large"
              block
              :loading="isSubmitting"
              class="submit-button"
            >
              <v-icon icon="mdi-send" start />
              Enviar solicitação
            </v-btn>
          </v-form>

          <div class="form-footer">
            <v-icon icon="mdi-lock" size="16" color="success" />
            <span>Seus dados estão protegidos e não serão compartilhados</span>
          </div>
        </div>
      </div>
    </v-container>
  </section>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'

const contactForm = ref<any>(null)
const isSubmitting = ref(false)

const formData = reactive({
  name: '',
  email: '',
  phone: '',
  institution: '',
  segment: '',
  message: '',
})

const segments = [
  'Prefeitura / Município',
  'Tribunal / Judiciário',
  'Autarquia / Fundação',
  'Empresa Privada',
  'Instituição Financeira',
  'Saúde',
  'Educação',
  'Outro',
]

const rules = {
  required: (v: any) => !!v || 'Campo obrigatório',
  email: (v: string) => {
    const pattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    return pattern.test(v) || 'E-mail inválido'
  },
}

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

const handleSubmit = async () => {
  const { valid } = await contactForm.value.validate()

  if (valid) {
    isSubmitting.value = true

    // Simular envio (aqui você integraria com sua API)
    setTimeout(() => {
      alert('Solicitação enviada com sucesso! Entraremos em contato em breve.')
      contactForm.value.reset()
      isSubmitting.value = false
    }, 1500)
  }
}
</script>

<style scoped lang="scss">
.final-cta-section {
  padding: 120px 0;
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  position: relative;
  overflow: hidden;
}

.cta-card {
  position: relative;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 80px;
  align-items: center;
  padding: 80px 60px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(37, 99, 235, 0.05) 100%);
  border-radius: 24px;
  border: 2px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  overflow: hidden;
}

.cta-decoration {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
  filter: blur(60px);
  pointer-events: none;
}

.cta-decoration-1 {
  width: 400px;
  height: 400px;
  background: rgb(var(--v-theme-primary));
  top: -200px;
  right: -100px;
}

.cta-decoration-2 {
  width: 300px;
  height: 300px;
  background: rgb(var(--v-theme-success));
  bottom: -150px;
  left: -100px;
}

.cta-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
  position: relative;
  z-index: 1;
}

.cta-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(var(--v-theme-primary), 0.2);
  border-radius: 24px;
  width: fit-content;
  border: 1px solid rgba(var(--v-theme-primary), 0.3);

  span {
    font-size: 14px;
    font-weight: 600;
    color: white;
  }
}

.cta-title {
  font-size: clamp(32px, 4vw, 48px);
  font-weight: 600;
  line-height: 1.15;
  color: white;
  letter-spacing: -1px;
  margin: 0;
}

.cta-subtitle {
  font-size: clamp(16px, 2vw, 18px);
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
}

.cta-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 8px;
}

.cta-button-primary,
.cta-button-secondary {
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0;
  padding: 0 32px;
  height: 56px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.3);
  }
}

.cta-button-secondary {
  border-color: rgba(255, 255, 255, 0.3);
  color: white;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.5);
  }
}

.cta-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 16px;
}

.cta-feature-item {
  display: flex;
  align-items: center;
  gap: 12px;

  span {
    font-size: 15px;
    color: rgba(255, 255, 255, 0.9);
    font-weight: 500;
    line-height: 1.4;
  }
}

.contact-form-wrapper {
  position: relative;
  z-index: 1;
  padding: 40px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.form-header {
  text-align: center;
  margin-bottom: 32px;
}

.form-title {
  font-size: 24px;
  font-weight: 600;
  color: #0f172a;
  margin-top: 16px;
  margin-bottom: 8px;
}

.form-subtitle {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.submit-button {
  margin-top: 8px;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0;
  height: 48px;
}

.form-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 24px;

  span {
    font-size: 12px;
    color: #64748b;
  }
}

@media (max-width: 960px) {
  .final-cta-section {
    padding: 80px 0;
  }

  .cta-card {
    grid-template-columns: 1fr;
    gap: 48px;
    padding: 48px 32px;
  }

  .cta-actions {
    flex-direction: column;

    button {
      width: 100%;
    }
  }
}

@media (max-width: 600px) {
  .cta-card {
    padding: 32px 24px;
  }

  .contact-form-wrapper {
    padding: 32px 24px;
  }
}
</style>
