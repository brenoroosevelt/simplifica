<template>
  <footer class="landing-footer">
    <v-container>
      <!-- Main Footer Content -->
      <div class="footer-main">
        <div class="footer-column footer-brand">
          <div class="brand-logo">
            <v-icon icon="mdi-chart-timeline-variant-shimmer" size="36" color="primary" />
            <span class="brand-name">Simplifica</span>
          </div>
          <p class="brand-description">
            Plataforma completa de gestão de processos, riscos e maturidade institucional para organizações públicas e privadas.
          </p>
          <div class="social-links">
            <v-btn
              icon
              variant="text"
              size="small"
              class="social-button"
              href="https://linkedin.com"
              target="_blank"
              aria-label="LinkedIn"
            >
              <v-icon icon="mdi-linkedin" />
            </v-btn>
            <v-btn
              icon
              variant="text"
              size="small"
              class="social-button"
              href="https://facebook.com"
              target="_blank"
              aria-label="Facebook"
            >
              <v-icon icon="mdi-facebook" />
            </v-btn>
            <v-btn
              icon
              variant="text"
              size="small"
              class="social-button"
              href="https://instagram.com"
              target="_blank"
              aria-label="Instagram"
            >
              <v-icon icon="mdi-instagram" />
            </v-btn>
            <v-btn
              icon
              variant="text"
              size="small"
              class="social-button"
              href="https://youtube.com"
              target="_blank"
              aria-label="YouTube"
            >
              <v-icon icon="mdi-youtube" />
            </v-btn>
          </div>
        </div>

        <div class="footer-column">
          <h3 class="footer-title">Soluções</h3>
          <ul class="footer-links">
            <li>
              <a href="#solucoes" @click.prevent="scrollToSection('#solucoes')">Modelagem BPM</a>
            </li>
            <li>
              <a href="#solucoes" @click.prevent="scrollToSection('#solucoes')">Gestão de Riscos</a>
            </li>
            <li>
              <a href="#solucoes" @click.prevent="scrollToSection('#solucoes')">Maturidade Institucional</a>
            </li>
            <li>
              <a href="#solucoes" @click.prevent="scrollToSection('#solucoes')">Governança</a>
            </li>
          </ul>
        </div>

        <div class="footer-column">
          <h3 class="footer-title">Empresa</h3>
          <ul class="footer-links">
            <li><a href="#sobre">Sobre nós</a></li>
            <li><a href="#casos-de-uso" @click.prevent="scrollToSection('#casos-de-uso')">Casos de uso</a></li>
            <li><a href="#blog">Blog</a></li>
            <li><a href="#carreiras">Carreiras</a></li>
            <li><a href="#imprensa">Imprensa</a></li>
          </ul>
        </div>

        <div class="footer-column">
          <h3 class="footer-title">Suporte</h3>
          <ul class="footer-links">
            <li><a href="#ajuda">Central de ajuda</a></li>
            <li><a href="#documentacao">Documentação</a></li>
            <li><a href="#contato" @click.prevent="scrollToSection('#contato')">Contato</a></li>
            <li><a href="#status">Status do sistema</a></li>
            <li><router-link to="/login">Acessar sistema</router-link></li>
          </ul>
        </div>

        <div class="footer-column">
          <h3 class="footer-title">Contato</h3>
          <ul class="footer-contact">
            <li>
              <v-icon icon="mdi-email" size="18" />
              <a href="mailto:contato@simplifica.com.br">contato@simplifica.com.br</a>
            </li>
            <li>
              <v-icon icon="mdi-phone" size="18" />
              <a href="tel:+5511999999999">(11) 9 9999-9999</a>
            </li>
            <li>
              <v-icon icon="mdi-map-marker" size="18" />
              <span>São Paulo, SP - Brasil</span>
            </li>
            <li>
              <v-icon icon="mdi-clock" size="18" />
              <span>Seg-Sex: 8h às 18h</span>
            </li>
          </ul>
        </div>
      </div>

      <v-divider class="footer-divider" />

      <!-- Bottom Footer -->
      <div class="footer-bottom">
        <div class="footer-legal">
          <p class="copyright">
            &copy; {{ currentYear }} Simplifica. Todos os direitos reservados.
          </p>
          <div class="legal-links">
            <a href="#privacidade">Política de Privacidade</a>
            <span class="legal-divider">•</span>
            <a href="#termos">Termos de Uso</a>
            <span class="legal-divider">•</span>
            <a href="#cookies">Cookies</a>
            <span class="legal-divider">•</span>
            <a href="#lgpd">LGPD</a>
          </div>
        </div>

        <div class="footer-badges">
          <div class="badge-item">
            <v-icon icon="mdi-shield-check" size="20" color="success" />
            <span>ISO 9001</span>
          </div>
          <div class="badge-item">
            <v-icon icon="mdi-lock" size="20" color="primary" />
            <span>LGPD</span>
          </div>
          <div class="badge-item">
            <v-icon icon="mdi-security" size="20" color="info" />
            <span>SSL</span>
          </div>
        </div>
      </div>
    </v-container>
  </footer>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const currentYear = computed(() => new Date().getFullYear())

const scrollToSection = (href: string) => {
  const id = href.replace('#', '')
  const element = document.getElementById(id)

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
</script>

<style scoped lang="scss">
.landing-footer {
  background: #0f172a;
  padding: 80px 0 32px;
  color: rgba(255, 255, 255, 0.8);
}

.footer-main {
  display: grid;
  grid-template-columns: 2fr repeat(4, 1fr);
  gap: 48px;
  margin-bottom: 48px;
}

.footer-column {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.footer-brand {
  max-width: 320px;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.brand-name {
  font-size: 24px;
  font-weight: 700;
  color: white;
  letter-spacing: -0.5px;
}

.brand-description {
  font-size: 14px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 8px;
}

.social-links {
  display: flex;
  gap: 8px;
}

.social-button {
  color: rgba(255, 255, 255, 0.7);
  transition: all 0.2s ease;

  &:hover {
    color: rgb(var(--v-theme-primary));
    background: rgba(var(--v-theme-primary), 0.1);
  }
}

.footer-title {
  font-size: 16px;
  font-weight: 700;
  color: white;
  margin-bottom: 4px;
  letter-spacing: 0.5px;
}

.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;

  li {
    font-size: 14px;
    line-height: 1.5;
  }

  a {
    color: rgba(255, 255, 255, 0.7);
    text-decoration: none;
    transition: color 0.2s ease;

    &:hover {
      color: rgb(var(--v-theme-primary));
    }
  }
}

.footer-contact {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;

  li {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 14px;
    line-height: 1.5;
  }

  a {
    color: rgba(255, 255, 255, 0.7);
    text-decoration: none;
    transition: color 0.2s ease;

    &:hover {
      color: rgb(var(--v-theme-primary));
    }
  }

  span {
    color: rgba(255, 255, 255, 0.7);
  }
}

.footer-divider {
  border-color: rgba(255, 255, 255, 0.1);
  margin: 48px 0 32px;
}

.footer-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

.footer-legal {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

.copyright {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.legal-links {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;

  a {
    color: rgba(255, 255, 255, 0.6);
    text-decoration: none;
    transition: color 0.2s ease;

    &:hover {
      color: rgba(255, 255, 255, 0.9);
    }
  }

  .legal-divider {
    color: rgba(255, 255, 255, 0.3);
  }
}

.footer-badges {
  display: flex;
  gap: 16px;
  align-items: center;
}

.badge-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.1);

  span {
    font-size: 12px;
    font-weight: 600;
    color: rgba(255, 255, 255, 0.8);
  }
}

@media (max-width: 1280px) {
  .footer-main {
    grid-template-columns: 1fr 1fr 1fr;
    gap: 32px;
  }

  .footer-brand {
    grid-column: 1 / -1;
    max-width: 100%;
  }
}

@media (max-width: 960px) {
  .landing-footer {
    padding: 60px 0 24px;
  }

  .footer-main {
    grid-template-columns: 1fr 1fr;
    gap: 32px;
    margin-bottom: 32px;
  }

  .footer-bottom {
    flex-direction: column;
    text-align: center;
  }

  .footer-legal {
    flex-direction: column;
    width: 100%;
  }
}

@media (max-width: 600px) {
  .footer-main {
    grid-template-columns: 1fr;
    gap: 32px;
  }

  .brand-logo {
    justify-content: center;
  }

  .brand-description {
    text-align: center;
  }

  .social-links {
    justify-content: center;
  }

  .footer-title {
    text-align: center;
  }

  .footer-links,
  .footer-contact {
    align-items: center;
    text-align: center;
  }

  .legal-links {
    flex-direction: column;
    gap: 8px;

    .legal-divider {
      display: none;
    }
  }

  .footer-badges {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
