# Tech Stack

## Frontend
- Vue 3
- Vite
- Vuetify 3
- TypeScript
- Pinia
- Axios
- Landing page pública (estilo SaaS)
- Layout privado (autenticação Oauth Google e Microsoft)


## Backend
- Java 21
- Spring Boot 3
- Spring Security
- OAuth2 (Google, Microsoft)
- JPA / Hibernate
- Flyway (migrations)
- Serviço de e-mail para notificação de usuários (no-reply)

## Database
- PostgreSQL 15

## Infra
- Docker
- Docker Compose
- Monorepo

## Qualidade
- Front: ESLint + Prettier + Vitest
- Back: JUnit + Mockito + Checkstyle

## Configuração
- Variáveis centralizadas em .env tanto no frontend quanto no backend 
- Manter .env.example sempre atualizado, mas sem expor senhas, chaves e secrets