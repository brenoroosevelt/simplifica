# Guia de Contribuição

Obrigado por considerar contribuir com o Simplifica! Este documento fornece diretrizes e boas práticas para contribuições ao projeto.

## Índice

- [Código de Conduta](#código-de-conduta)
- [Como Posso Contribuir?](#como-posso-contribuir)
- [Configuração do Ambiente](#configuração-do-ambiente)
- [Fluxo de Trabalho Git](#fluxo-de-trabalho-git)
- [Padrões de Código](#padrões-de-código)
- [Testes](#testes)
- [Commits](#commits)
- [Pull Requests](#pull-requests)
- [Reportando Bugs](#reportando-bugs)
- [Sugerindo Features](#sugerindo-features)

## Código de Conduta

Este projeto adere a padrões profissionais de comportamento. Esperamos que todos os contribuidores:

- Sejam respeitosos e inclusivos
- Aceitem críticas construtivas
- Foquem no que é melhor para o projeto
- Demonstrem empatia com outros membros da comunidade

## Como Posso Contribuir?

### Tipos de Contribuições

- **Correção de Bugs**: Identificar e corrigir problemas existentes
- **Novas Features**: Implementar funcionalidades planejadas ou propostas
- **Melhorias de Performance**: Otimizar código existente
- **Documentação**: Melhorar ou expandir a documentação
- **Testes**: Adicionar ou melhorar cobertura de testes
- **Refatoração**: Melhorar estrutura do código sem alterar funcionalidade

### Antes de Começar

1. Verifique se já não existe uma issue ou PR relacionado
2. Para features grandes, crie uma issue para discussão antes de implementar
3. Para bugs, verifique se consegue reproduzir no ambiente de desenvolvimento

## Configuração do Ambiente

### 1. Fork e Clone

```bash
# Fork o repositório via interface GitHub
# Clone seu fork
git clone https://github.com/seu-usuario/claude-agents.git
cd claude-agents

# Adicione o upstream
git remote add upstream https://github.com/original-owner/claude-agents.git
```

### 2. Setup Local

```bash
# Execute o script de setup
./scripts/setup-dev.sh

# Configure os arquivos .env conforme README.md

# Inicie o ambiente
./scripts/start-dev.sh
```

### 3. Verifique que está funcionando

- Acesse http://localhost:5173 (Frontend)
- Acesse http://localhost:8080/api/public/health (Backend)
- Execute os testes: `mvn test` e `npm test`

## Fluxo de Trabalho Git

### Branches

Usamos o modelo de Git Flow simplificado:

- `main` - Código em produção (protegida)
- `develop` - Integração de features (protegida)
- `feature/*` - Novas funcionalidades
- `bugfix/*` - Correções de bugs
- `hotfix/*` - Correções urgentes para produção

### Criando uma Branch

```bash
# Atualize seu repositório local
git checkout develop
git pull upstream develop

# Crie sua branch a partir da develop
git checkout -b feature/nome-da-feature
# ou
git checkout -b bugfix/nome-do-bug
```

### Nomenclatura de Branches

Use nomes descritivos e em kebab-case:

✅ **Bom:**
- `feature/oauth-github-integration`
- `bugfix/jwt-token-expiration`
- `hotfix/security-vulnerability`

❌ **Ruim:**
- `feature/issue123`
- `fix`
- `minha-branch`

## Padrões de Código

### Backend (Java)

#### Convenções Gerais

- **Nomenclatura**:
  - Classes: `PascalCase`
  - Métodos e variáveis: `camelCase`
  - Constantes: `UPPER_SNAKE_CASE`
  - Packages: `lowercase.separated.by.dots`

- **Estrutura**:
  ```java
  // 1. Package declaration
  package com.claudeagents.application.service;

  // 2. Imports (organizados)
  import com.claudeagents.domain.entity.User;
  import org.springframework.stereotype.Service;

  // 3. Class documentation
  /**
   * Service responsible for user management operations.
   */
  @Service
  public class UserService {
      // 4. Constants
      private static final int MAX_LOGIN_ATTEMPTS = 5;

      // 5. Dependencies
      private final UserRepository userRepository;

      // 6. Constructor
      public UserService(UserRepository userRepository) {
          this.userRepository = userRepository;
      }

      // 7. Public methods
      public User findById(UUID id) {
          return userRepository.findById(id)
              .orElseThrow(() -> new UserNotFoundException(id));
      }

      // 8. Private methods
      private void validateUser(User user) {
          // validation logic
      }
  }
  ```

#### Princípios SOLID

- **Single Responsibility**: Uma classe = uma responsabilidade
- **Open/Closed**: Aberto para extensão, fechado para modificação
- **Liskov Substitution**: Subtipos substituíveis por seus tipos base
- **Interface Segregation**: Interfaces específicas > interfaces gerais
- **Dependency Inversion**: Dependa de abstrações, não implementações

#### Boas Práticas

- Use Lombok para reduzir boilerplate (`@Getter`, `@Setter`, `@Builder`)
- Prefira `Optional` a retornar `null`
- Use streams do Java 8+ quando apropriado
- Documente métodos públicos com Javadoc
- Evite magic numbers - use constantes nomeadas
- Máximo 120 caracteres por linha
- Use `final` em variáveis que não mudam

#### Exemplo de Bom Código

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private static final int MAX_LOGIN_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Finds a user by email address.
     *
     * @param email the user's email
     * @return the user if found
     * @throws UserNotFoundException if user doesn't exist
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
    }

    /**
     * Creates a new user account.
     *
     * @param userDto the user data
     * @return the created user
     */
    @Transactional
    public User createUser(CreateUserDTO userDto) {
        validateEmailNotInUse(userDto.getEmail());

        User user = User.builder()
            .email(userDto.getEmail())
            .name(userDto.getName())
            .status(UserStatus.PENDING)
            .build();

        return userRepository.save(user);
    }

    private void validateEmailNotInUse(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyInUseException(email);
        }
    }
}
```

### Frontend (Vue 3 + TypeScript)

#### Convenções Gerais

- **Nomenclatura**:
  - Componentes: `PascalCase.vue`
  - Composables: `useCamelCase.ts`
  - Stores: `camelCase.store.ts`
  - Constantes: `UPPER_SNAKE_CASE`

- **Estrutura de Componente**:
  ```vue
  <script setup lang="ts">
  // 1. Imports
  import { ref, computed, onMounted } from 'vue'
  import { useAuthStore } from '@/stores/auth.store'

  // 2. Props
  interface Props {
    userId: string
    showAvatar?: boolean
  }
  const props = withDefaults(defineProps<Props>(), {
    showAvatar: true
  })

  // 3. Emits
  const emit = defineEmits<{
    update: [userId: string]
    delete: [userId: string]
  }>()

  // 4. Composables e Stores
  const authStore = useAuthStore()

  // 5. Reactive State
  const loading = ref(false)
  const error = ref<string | null>(null)

  // 6. Computed
  const isCurrentUser = computed(() => props.userId === authStore.user?.id)

  // 7. Methods
  const handleUpdate = async () => {
    loading.value = true
    try {
      // logic
      emit('update', props.userId)
    } catch (e) {
      error.value = 'Failed to update'
    } finally {
      loading.value = false
    }
  }

  // 8. Lifecycle
  onMounted(() => {
    // initialization
  })
  </script>

  <template>
    <v-card>
      <!-- template content -->
    </v-card>
  </template>

  <style scoped>
  /* Component-specific styles */
  </style>
  ```

#### Boas Práticas

- Use Composition API (`<script setup>`)
- Tipagem forte com TypeScript
- Componentes pequenos e focados (< 300 linhas idealmente)
- Props com valores default quando apropriado
- Emits tipados para comunicação pai-filho
- Use composables para lógica reutilizável
- Evite lógica de negócio no template
- Use `v-if` para renderização condicional, `v-show` para toggles frequentes
- Prefira computed properties a methods para valores derivados

#### Exemplo de Bom Código

```typescript
// useAuth.ts
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import type { OAuthProvider } from '@/types/auth.types'

export function useAuth() {
  const authStore = useAuthStore()

  const isAuthenticated = computed(() => authStore.isAuthenticated)
  const user = computed(() => authStore.user)
  const isPending = computed(() => user.value?.status === 'PENDING')

  const login = async (provider: OAuthProvider): Promise<void> => {
    await authStore.loginWithProvider(provider)
  }

  const logout = async (): Promise<void> => {
    await authStore.logout()
  }

  return {
    isAuthenticated,
    user,
    isPending,
    login,
    logout
  }
}
```

```vue
<!-- UserProfile.vue -->
<script setup lang="ts">
import { computed } from 'vue'
import { useAuth } from '@/composables/useAuth'

const { user, logout } = useAuth()

const initials = computed(() => {
  if (!user.value) return ''
  return user.value.name
    .split(' ')
    .map(n => n[0])
    .join('')
    .toUpperCase()
})

const handleLogout = async () => {
  if (confirm('Tem certeza que deseja sair?')) {
    await logout()
  }
}
</script>

<template>
  <v-menu>
    <template #activator="{ props }">
      <v-btn icon v-bind="props">
        <v-avatar v-if="user?.pictureUrl" :image="user.pictureUrl" />
        <v-avatar v-else color="primary">
          {{ initials }}
        </v-avatar>
      </v-btn>
    </template>

    <v-list>
      <v-list-item :title="user?.name" :subtitle="user?.email" />
      <v-divider />
      <v-list-item @click="handleLogout">
        <template #prepend>
          <v-icon icon="mdi-logout" />
        </template>
        Sair
      </v-list-item>
    </v-list>
  </v-menu>
</template>
```

### Linting

Antes de fazer commit, sempre execute:

```bash
# Backend
mvn checkstyle:check

# Frontend
npm run lint
npm run format
```

## Testes

### Cobertura Mínima

- **Backend**: 80% de cobertura em services e repositories
- **Frontend**: 70% de cobertura em composables e stores

### Backend - Testes Unitários

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        // Given
        UUID userId = UUID.randomUUID();
        User expectedUser = User.builder()
            .id(userId)
            .email("test@example.com")
            .build();
        when(userRepository.findById(userId))
            .thenReturn(Optional.of(expectedUser));

        // When
        User result = userService.findById(userId);

        // Then
        assertThat(result).isEqualTo(expectedUser);
        verify(userRepository).findById(userId);
    }

    @Test
    void findById_WhenUserNotExists_ShouldThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId))
            .thenReturn(Optional.empty());

        // When / Then
        assertThrows(UserNotFoundException.class,
            () -> userService.findById(userId));
    }
}
```

### Frontend - Testes Unitários

```typescript
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth.store'

describe('Auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should initialize with unauthenticated state', () => {
    const store = useAuthStore()

    expect(store.isAuthenticated).toBe(false)
    expect(store.user).toBeNull()
    expect(store.token).toBeNull()
  })

  it('should authenticate user on successful login', async () => {
    const store = useAuthStore()
    const mockToken = 'mock-jwt-token'

    await store.handleCallback(mockToken)

    expect(store.isAuthenticated).toBe(true)
    expect(store.token).toBe(mockToken)
  })

  it('should clear state on logout', async () => {
    const store = useAuthStore()
    store.token = 'mock-token'
    store.user = { id: '1', email: 'test@example.com' }

    await store.logout()

    expect(store.isAuthenticated).toBe(false)
    expect(store.user).toBeNull()
    expect(store.token).toBeNull()
  })
})
```

### Executando Testes

```bash
# Backend - todos os testes
mvn test

# Backend - testes de integração
mvn verify

# Frontend - todos os testes
npm test

# Frontend - modo watch
npm test -- --watch

# Frontend - com coverage
npm test -- --coverage
```

## Commits

### Formato de Mensagem

Usamos o padrão [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### Types

- `feat`: Nova funcionalidade
- `fix`: Correção de bug
- `docs`: Mudanças na documentação
- `style`: Formatação, ponto e vírgula, etc (sem mudança de código)
- `refactor`: Refatoração (sem mudança de funcionalidade)
- `test`: Adição ou correção de testes
- `chore`: Atualizações de build, configs, etc

#### Scope (opcional)

- `auth`: Autenticação
- `user`: Gestão de usuários
- `oauth`: OAuth providers
- `api`: API REST
- `ui`: Interface do usuário
- `db`: Database/Migrations

#### Exemplos

```bash
feat(oauth): add GitHub OAuth provider support

Implement GitHub as a new OAuth provider following the existing
architecture pattern. Includes backend OAuth2UserInfo implementation
and frontend provider service.

Closes #123
```

```bash
fix(auth): correct JWT token expiration validation

The token expiration was not being validated correctly, allowing
expired tokens to be accepted. Fixed the comparison logic in
JwtTokenProvider.validateToken().

Fixes #456
```

```bash
docs(readme): update OAuth setup instructions

Add detailed steps for configuring redirect URIs in Google Cloud
Console and Azure Portal.
```

### Boas Práticas de Commit

- Use imperativo presente ("add feature" não "added feature")
- Primeira linha com máximo 72 caracteres
- Corpo do commit com máximo 80 caracteres por linha
- Separe subject do body com linha em branco
- Explique o "porquê" no body, não o "o quê"
- Referencie issues relacionadas

## Pull Requests

### Antes de Criar um PR

1. ✅ Todos os testes passam
2. ✅ Código segue os padrões (lint passa)
3. ✅ Novos testes foram adicionados
4. ✅ Documentação foi atualizada (se necessário)
5. ✅ Branch está atualizada com `develop`

```bash
# Atualize sua branch
git checkout develop
git pull upstream develop
git checkout feature/sua-feature
git rebase develop

# Verifique testes e lint
mvn test && mvn checkstyle:check
npm test && npm run lint
```

### Criando o PR

1. Push sua branch:
   ```bash
   git push origin feature/sua-feature
   ```

2. Crie o PR via GitHub interface

3. Use o template de PR (se disponível)

4. Preencha:
   - **Título**: Descrição clara e concisa
   - **Descrição**: O que mudou e por quê
   - **Como testar**: Passos para validar as mudanças
   - **Screenshots**: Se mudanças visuais
   - **Issues relacionadas**: `Closes #123`, `Fixes #456`

### Template de PR

```markdown
## Descrição

Breve descrição das mudanças realizadas.

## Tipo de Mudança

- [ ] Bug fix (mudança que corrige um problema)
- [ ] Nova feature (mudança que adiciona funcionalidade)
- [ ] Breaking change (mudança que quebra compatibilidade)
- [ ] Documentação

## Como Testar

1. Execute `./scripts/start-dev.sh`
2. Acesse http://localhost:5173
3. Clique em "Login with Google"
4. Verifique que...

## Checklist

- [ ] Código segue os padrões do projeto
- [ ] Comentei código em áreas complexas
- [ ] Documentação atualizada
- [ ] Testes adicionados/atualizados
- [ ] Todos os testes passam
- [ ] Lint passa sem erros

## Screenshots (se aplicável)

![Screenshot](url)

## Issues Relacionadas

Closes #123
Fixes #456
```

### Review Process

- Pelo menos 1 aprovação necessária
- CI/CD deve passar (quando configurado)
- Responda aos comentários de forma construtiva
- Faça as alterações solicitadas
- Marque conversas como resolvidas após ajustes

## Reportando Bugs

### Antes de Reportar

1. Verifique se o bug já foi reportado
2. Tente reproduzir no ambiente de desenvolvimento limpo
3. Colete informações relevantes (logs, screenshots, versões)

### Template de Bug Report

```markdown
**Descrição do Bug**
Descrição clara e concisa do problema.

**Como Reproduzir**
1. Vá para '...'
2. Clique em '...'
3. Observe erro '...'

**Comportamento Esperado**
O que deveria acontecer.

**Screenshots**
Se aplicável, adicione screenshots.

**Ambiente:**
- SO: [ex: Ubuntu 22.04]
- Docker version: [ex: 24.0.5]
- Browser: [ex: Chrome 120]
- Node version: [ex: 20.10.0]
- Java version: [ex: OpenJDK 21]

**Logs**
```
Cole logs relevantes aqui
```

**Contexto Adicional**
Qualquer outra informação relevante.
```

## Sugerindo Features

### Template de Feature Request

```markdown
**Problema a Resolver**
Descrição clara do problema ou necessidade.

**Solução Proposta**
Como você imagina que a feature deveria funcionar.

**Alternativas Consideradas**
Outras abordagens que você considerou.

**Contexto Adicional**
Screenshots, mockups, referências, etc.
```

---

## Perguntas?

Se você tiver dúvidas sobre como contribuir, sinta-se à vontade para:

- Abrir uma issue com a tag `question`
- Entrar em contato com a equipe de desenvolvimento

## Agradecimentos

Obrigado por contribuir com o Simplifica! Suas contribuições tornam este projeto melhor para todos.

---

**Happy Coding!** 🚀
