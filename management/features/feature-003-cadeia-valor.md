# Feature 003 - CRUD Cadeia de Valor

## Contexto
O sistema precisa gerenciar Cadeias de Valor (Value Chains) de cada instituição, permitindo que gestores e administradores criem, visualizem, editem e organizem as cadeias de valor específicas de sua instituição. Cada cadeia de valor representa uma sequência de processos/atividades que agregam valor e podem incluir imagem ilustrativa, nome, descrição e status.

## Objetivo
Implementar um CRUD completo de Cadeia de Valor com:
- Controle de permissão por tenant (instituição)
- Upload de imagens (PNG, JPG, JPEG) com validação e geração de thumbnails
- Paginação, ordenação e filtros no backend
- Interface seguindo o padrão existente (ex: Instituições)
- Usuário não escolhe a instituição - usa a atualmente selecionada
- Soft delete com flag `active`

## Usuários impactados
- Gestores de instituição (MANAGER role)
- Administradores de sistema (ADMIN role)
- Usuários com acesso à instituição ativa

## Escopo

### Inclui:

#### Backend:
- **Pré-requisito - Sistema de Upload:**
  - FileStorageProperties para configurações
  - FileStorageService completo (upload, validação, thumbnail, delete)
  - FileController para servir arquivos públicos
  - Integração com InstitutionController existente

- **Entidade ValueChain:**
  - Relacionamento ManyToOne com Institution
  - Campos: nome, descrição, imageUrl, imageThumbnailUrl, active, timestamps
  - Soft delete via flag `active`
  - Migration Flyway V7

- **Repository & Specifications:**
  - ValueChainRepository com JpaSpecificationExecutor
  - ValueChainSpecifications (belongsToInstitution, hasActive, searchByName)
  - Queries otimizadas com índices

- **Service:**
  - ValueChainService com validação multi-tenant
  - Usa TenantContext para obter instituição atual
  - Validação de unicidade de nome por instituição
  - Métodos: findAll, findById, create, update, uploadImage, deleteImage, delete

- **Controller REST:**
  - ValueChainController com endpoints protegidos
  - Multipart/form-data para upload
  - @PreAuthorize para MANAGER e ADMIN
  - Paginação server-side

- **DTOs:**
  - CreateValueChainDTO, UpdateValueChainDTO, ValueChainDTO
  - Validações Jakarta (Bean Validation)

#### Frontend:
- **Types TypeScript:**
  - ValueChain, ValueChainCreateRequest, ValueChainUpdateRequest
  - ValueChainListParams, PageResponse<T>

- **Service:**
  - ValueChainService com métodos async
  - buildFormData() para multipart
  - Integração com apiClient (header X-Institution-Id automático)

- **Componentes Vue:**
  - ValueChainList.vue (tabela server-side com filtros)
  - ValueChainForm.vue (formulário create/edit com upload)
  - Upload com preview, validação (5MB, tipos permitidos)

- **Página:**
  - ValueChainsPage.vue (orchestration)
  - Dialogs (create/edit/delete)
  - Snackbars para feedback

- **Rota e Menu:**
  - Rota /value-chains com meta requiresUserManagement
  - Item de menu "Cadeias de Valor" no sidebar

#### Configuração:
- Variáveis .env para storage (base path, max size, extensions)
- application.yml com configurações de storage

### Não Inclui (Futuro):
- Versionamento de cadeias de valor
- Relacionamento entre cadeias (hierarquia)
- Export/import de cadeias
- Análise de métricas por cadeia
- Integração com processos/atividades

---

## Fluxo do usuário

### Fluxo Gestor/Admin - Criar Cadeia de Valor
1. Usuário acessa menu "Cadeias de Valor"
2. Visualiza lista de cadeias da instituição ativa
3. Clica em "Nova Cadeia de Valor"
4. Preenche formulário:
   - Upload de imagem (opcional, max 5MB, jpg/png/jpeg)
   - Nome (obrigatório, max 255 chars)
   - Descrição (opcional, text area)
   - Status (Ativa/Inativa)
   - Instituição (exibida, read-only, não editável)
5. Clica "Criar"
6. Sistema valida, faz upload da imagem, gera thumbnail
7. Cadeia criada e vinculada à instituição ativa
8. Retorna à lista com mensagem de sucesso

### Fluxo Gestor/Admin - Editar Cadeia de Valor
1. Na lista, clica no ícone de editar
2. Dialog abre com formulário preenchido
3. Altera campos desejados (nome, descrição, imagem, status)
4. Clica "Salvar"
5. Sistema valida, se nova imagem: remove antiga, faz upload da nova
6. Cadeia atualizada
7. Lista atualiza com mudanças

### Fluxo Gestor/Admin - Filtrar e Paginar
1. Na lista, digita termo de busca (nome)
2. Sistema aplica debounce (500ms) e busca
3. Seleciona filtro de status (Ativa/Inativa)
4. Resultados filtrados em tempo real
5. Pode alterar ordenação clicando nos headers da tabela
6. Pode mudar itens por página (10, 25, 50, 100)
7. Navega entre páginas

### Fluxo Gestor/Admin - Excluir Cadeia de Valor
1. Clica no ícone de deletar
2. Dialog de confirmação aparece
3. Clica "Excluir"
4. Sistema faz soft delete (active=false)
5. Cadeia removida da lista (se filtro = Ativas)
6. Mensagem de sucesso

---

## Regras de negócio

### Cadeia de Valor
- Uma cadeia de valor pertence a uma única instituição
- Nome deve ser único dentro da instituição
- Descrição é opcional
- Imagem é opcional
- Status active controla soft delete
- Timestamps (createdAt, updatedAt) são automáticos
- Apenas MANAGER e ADMIN podem gerenciar cadeias

### Upload de Imagem
- Tamanho máximo: 5MB (configurável via .env)
- Formatos permitidos: JPG, JPEG, PNG (configurável via .env)
- Thumbnail gerado automaticamente (150px mantendo proporção)
- Validação de tipo MIME real (não apenas extensão)
- Arquivos salvos em: `/var/simplifica/uploads/value-chains/` (configurável)
- URLs públicas servidas via: `/api/public/uploads/value-chains/{filename}`

### Isolamento Multi-tenant
- GESTOR só acessa cadeias de sua instituição
- ADMIN pode acessar cadeias de qualquer instituição (via contexto)
- Todas as queries filtram por institution_id automaticamente
- TenantContext obtém instituição do header X-Institution-Id
- Tentativa de acesso cross-tenant resulta em 403 Forbidden
- Validação em múltiplas camadas (interceptor, service, repository)

### Contexto de Instituição
- Instituição não é escolhida pelo usuário no formulário
- Sistema usa a instituição atualmente ativa (InstitutionStore)
- Frontend exibe instituição mas como campo read-only
- Backend valida que usuário pertence à instituição
- Usuário não pode trocar instituição de cadeia existente

---

## Critérios de aceite

### Pré-requisito - Sistema de Upload
- [ ] FileStorageProperties criado e carrega .env
- [ ] FileStorageService implementado com:
  - [ ] storeImage() valida e salva arquivo
  - [ ] deleteFile() remove arquivo e thumbnail
  - [ ] Geração automática de thumbnail (150px)
  - [ ] Validação de tamanho (5MB)
  - [ ] Validação de tipo MIME
- [ ] FileController serve arquivos via GET /public/uploads/{folder}/{filename}
- [ ] InstitutionController modificado para processar upload de logo
- [ ] Teste manual: upload PNG/JPG funciona, thumbnail gerado, URL acessível

### Backend - Cadeia de Valor
- [ ] Migration V7__create_value_chains_table.sql criada
- [ ] Tabela value_chains criada com:
  - [ ] FK institution_id com ON DELETE CASCADE
  - [ ] Índices: institution_id, active, institution_id+active
  - [ ] Trigger para updated_at
- [ ] Entidade ValueChain.java criada com:
  - [ ] ManyToOne com Institution
  - [ ] @PrePersist e @PreUpdate para timestamps
  - [ ] Métodos: setImageUrls(), clearImage(), isActive()
- [ ] ValueChainRepository criado com:
  - [ ] Extends JpaRepository e JpaSpecificationExecutor
  - [ ] Queries: findByIdAndInstitutionId, existsByNameAndInstitutionId
- [ ] ValueChainSpecifications criado com:
  - [ ] belongsToInstitution(UUID) - CRÍTICO para multi-tenant
  - [ ] hasActive(Boolean)
  - [ ] searchByName(String)
- [ ] DTOs criados (CreateValueChainDTO, UpdateValueChainDTO, ValueChainDTO)
- [ ] ValueChainService implementado com:
  - [ ] getCurrentInstitutionId() usa TenantContext
  - [ ] validateTenantAccess(ValueChain)
  - [ ] findAll() filtra por institutionId + filtros opcionais
  - [ ] create() valida nome único por instituição
  - [ ] update() valida tenant antes de modificar
  - [ ] uploadImage() deleta antiga e faz upload da nova
  - [ ] delete() soft delete (active=false)
- [ ] ValueChainController criado com:
  - [ ] GET /value-chains (lista com filtros e paginação)
  - [ ] GET /value-chains/{id}
  - [ ] POST /value-chains (multipart/form-data)
  - [ ] PUT /value-chains/{id} (multipart/form-data)
  - [ ] DELETE /value-chains/{id}/image
  - [ ] DELETE /value-chains/{id} (soft delete)
  - [ ] @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
- [ ] Teste manual backend:
  - [ ] GESTOR lista cadeias → só retorna da instituição dele
  - [ ] GESTOR cria cadeia → institution_id = sua instituição
  - [ ] GESTOR tenta acessar cadeia de outra instituição → 403
  - [ ] ADMIN lista cadeias → retorna da instituição no contexto
  - [ ] Upload de 6MB → falha com "exceeds maximum"
  - [ ] Upload de TXT → falha com "type not allowed"

### Frontend - Cadeia de Valor
- [ ] Types criados em valueChain.types.ts
- [ ] ValueChainService criado com:
  - [ ] list(), getById(), create(), update(), deleteImage(), delete()
  - [ ] buildFormData() para multipart
- [ ] ValueChainList.vue criado com:
  - [ ] v-data-table-server (paginação server-side)
  - [ ] Filtros: busca (debounce 500ms), status (Ativa/Inativa)
  - [ ] Colunas: Avatar imagem, Nome+Descrição, Instituição (se showInstitution), Status chip, Data criação, Ações
  - [ ] Emits: update:filters, update:pagination, edit, delete
- [ ] ValueChainForm.vue criado com:
  - [ ] Upload imagem: preview, validação 5MB, tipos permitidos
  - [ ] Nome: v-text-field obrigatório, counter 255
  - [ ] Descrição: v-textarea opcional, counter 5000
  - [ ] Instituição: v-text-field readonly (exibe instituição ativa)
  - [ ] Status: v-select (Ativa/Inativa)
  - [ ] Validação: hasChanges computed
  - [ ] Emits: submit, cancel
- [ ] ValueChainsPage.vue criado com:
  - [ ] PageHeader + ValueChainList
  - [ ] Dialogs: create/edit, delete confirmation
  - [ ] Snackbar para feedback
  - [ ] Methods: loadValueChains(), handleFormSubmit(), handleDelete()
- [ ] Rota /value-chains adicionada com meta:
  - [ ] requiresAuth: true
  - [ ] requiresInstitution: true
  - [ ] requiresUserManagement: true
- [ ] Menu "Cadeias de Valor" adicionado ao sidebar
- [ ] Teste manual frontend:
  - [ ] Login GESTOR → acessa Cadeias de Valor
  - [ ] Cria nova cadeia com imagem → sucesso
  - [ ] Preview da imagem aparece
  - [ ] Instituição exibida mas não editável
  - [ ] Lista mostra a cadeia criada
  - [ ] Filtro por nome funciona com debounce
  - [ ] Paginação funciona (mudar tamanho, trocar página)
  - [ ] Editar: altera nome, troca imagem → mudanças salvas
  - [ ] Excluir: soft delete, cadeia some da lista Ativas
  - [ ] Filtrar Inativas: cadeia excluída aparece

### Segurança Multi-tenant
- [ ] TenantContext.getCurrentInstitution() usado em todos métodos de service
- [ ] belongsToInstitution(institutionId) incluído em TODAS queries
- [ ] validateTenantAccess() chamado antes de modificações
- [ ] @PreAuthorize aplicado em todos endpoints
- [ ] Header X-Institution-Id enviado automaticamente (apiClient)
- [ ] Teste: GESTOR não acessa cadeia de instituição diferente
- [ ] Teste: ADMIN com contexto Inst A não acessa cadeia de Inst B
- [ ] Teste: Query SQL sempre tem WHERE institution_id = ?

### Qualidade
- [ ] Código segue padrões (Checkstyle backend, ESLint frontend)
- [ ] Logs apropriados (INFO para operações, DEBUG para detalhes, WARN para acessos negados)
- [ ] Exceções customizadas (ResourceNotFoundException, UnauthorizedAccessException)
- [ ] Transações corretas (@Transactional)
- [ ] Documentação clara em javadoc/jsdoc

---

## Tarefas

### FASE 1: Sistema de Upload (Pré-requisito)

#### 1.1 - Criar FileStorageProperties
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: Mapear configurações de storage do application.yml

**Arquivo**: `/backend/src/main/java/com/simplifica/config/FileStorageProperties.java`

**Conteúdo**:
```java
@Configuration
@ConfigurationProperties(prefix = "app.storage")
@Getter @Setter
public class FileStorageProperties {
    private String provider; // local or s3
    private Local local = new Local();
    private Integer maxFileSizeMb = 5;
    private List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "gif", "webp");

    @Getter @Setter
    public static class Local {
        private String basePath = "/tmp/simplifica/uploads";
        private String publicUrl = "http://localhost:8080/api/public/uploads";
    }
}
```

**Validação**: Classe compila, propriedades carregadas de application.yml

---

#### 1.2 - Criar FileStorageService
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 3h

**Objetivo**: Implementar service completo de upload

**Arquivo**: `/backend/src/main/java/com/simplifica/application/service/FileStorageService.java`

**Funcionalidades**:
- `storeImage(MultipartFile file, String folder)`: valida, salva, gera thumbnail, retorna FileUploadResult
- `deleteFile(String fileUrl)`: remove arquivo e thumbnail
- `validateImage()`: valida tamanho, extensão, MIME type real
- `generateThumbnail()`: redimensiona para 150px mantendo proporção

**Validação**:
- Upload PNG 2MB → sucesso, thumbnail criado em /thumbnails/
- Upload 6MB → exceção "exceeds maximum"
- Upload TXT → exceção "type not allowed"
- URLs corretas geradas (fileUrl, thumbnailUrl)

---

#### 1.3 - Criar FileController
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Servir arquivos públicos

**Arquivo**: `/backend/src/main/java/com/simplifica/presentation/controller/FileController.java`

**Endpoints**:
- GET `/public/uploads/{folder}/{filename}` - serve arquivo original
- GET `/public/uploads/{folder}/thumbnails/{filename}` - serve thumbnail

**Validação**:
- GET /public/uploads/value-chains/abc123.jpg → retorna imagem
- GET /public/uploads/value-chains/thumbnails/thumb_abc123.jpg → retorna thumbnail
- Arquivo inexistente → 404

---

#### 1.4 - Modificar InstitutionController
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Processar upload de logo em Institution

**Arquivo**: `/backend/src/main/java/com/simplifica/presentation/controller/InstitutionController.java`

**Modificações**:
- Injetar FileStorageService
- No método `createInstitution()`: se logo != null, chamar fileStorageService.storeImage(logo, "institutions"), salvar URLs
- No método `updateInstitution()`: deletar logo antiga se nova for enviada, fazer upload

**Validação**: Upload de logo em Institution funciona, URL salva no banco

---

### FASE 2: Backend - Entidade e Repositories

#### 2.1 - Criar Migration Flyway
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 45min

**Objetivo**: Schema de banco para value_chains

**Arquivo**: `/backend/src/main/resources/db/migration/V7__create_value_chains_table.sql`

**Conteúdo**:
```sql
CREATE TABLE value_chains (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    institution_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(1024),
    image_thumbnail_url VARCHAR(1024),
    image_uploaded_at TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_value_chains_institution
        FOREIGN KEY (institution_id) REFERENCES institutions(id) ON DELETE CASCADE
);

CREATE INDEX idx_value_chains_institution_id ON value_chains(institution_id);
CREATE INDEX idx_value_chains_active ON value_chains(active);
CREATE INDEX idx_value_chains_institution_active ON value_chains(institution_id, active);

CREATE TRIGGER trg_value_chains_updated_at
    BEFORE UPDATE ON value_chains
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

**Validação**: mvn flyway:migrate executa sem erros, tabela criada

---

#### 2.2 - Criar Entidade ValueChain
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Entidade JPA com relacionamento

**Arquivo**: `/backend/src/main/java/com/simplifica/domain/entity/ValueChain.java`

**Características**:
- @Entity, @Table, @Getter, @Setter, @Builder
- ManyToOne com Institution (fetch LAZY, nullable=false)
- @PrePersist onCreate(), @PreUpdate onUpdate()
- Métodos: setImageUrls(), clearImage(), isActive()

**Validação**: Aplicação inicia, entidade reconhecida pelo JPA

---

#### 2.3 - Criar Repository e Specifications
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Acesso a dados com queries dinâmicas

**Arquivos**:
- `/backend/src/main/java/com/simplifica/infrastructure/repository/ValueChainRepository.java`
- `/backend/src/main/java/com/simplifica/infrastructure/repository/ValueChainSpecifications.java`

**ValueChainRepository**:
- Extends JpaRepository<ValueChain, UUID> e JpaSpecificationExecutor
- Queries: findByIdAndInstitutionId, existsByNameAndInstitutionId, existsByNameAndInstitutionIdAndIdNot

**ValueChainSpecifications**:
- belongsToInstitution(UUID) - CRÍTICO
- hasActive(Boolean)
- searchByName(String)

**Validação**: Queries funcionam, Specifications retornam Predicate correto

---

### FASE 3: Backend - Service e Controller

#### 3.1 - Criar DTOs
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 45min

**Objetivo**: Objetos de transferência

**Arquivos** (em `com.simplifica.application.dto`):
- CreateValueChainDTO.java - @NotBlank name, @Size description, Boolean active
- UpdateValueChainDTO.java - todos opcionais
- ValueChainDTO.java - response com todos campos + fromEntity()

**Validação**: DTOs compilam, validações Bean Validation funcionam

---

#### 3.2 - Criar ValueChainService
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 3h

**Objetivo**: Lógica de negócio com multi-tenant

**Arquivo**: `/backend/src/main/java/com/simplifica/application/service/ValueChainService.java`

**Métodos principais**:
- `getCurrentInstitutionId()`: TenantContext.getCurrentInstitution()
- `validateTenantAccess(ValueChain)`: valida que pertence à instituição do contexto
- `findById(UUID)`: busca + validateTenantAccess
- `findAll()`: Specification com belongsToInstitution + filtros
- `create()`: valida nome único, cria com institution do contexto
- `update()`: findById (valida tenant) + atualiza
- `uploadImage()`: deleta antiga + storeImage
- `delete()`: soft delete (active=false)

**Validação**: Métodos funcionam, transações corretas, logs apropriados

---

#### 3.3 - Criar ValueChainController
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 2h

**Objetivo**: Endpoints REST

**Arquivo**: `/backend/src/main/java/com/simplifica/presentation/controller/ValueChainController.java`

**Endpoints**:
- GET /value-chains - @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
- GET /value-chains/{id}
- POST /value-chains (multipart/form-data)
- PUT /value-chains/{id} (multipart/form-data)
- DELETE /value-chains/{id}/image
- DELETE /value-chains/{id}

**Validação**: Postman/curl testam endpoints, status HTTP corretos, validações funcionam

---

### FASE 4: Frontend - Types e Service

#### 4.1 - Criar Types TypeScript
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: Interfaces TypeScript

**Arquivo**: `/frontend/src/types/valueChain.types.ts`

**Interfaces**: ValueChain, ValueChainCreateRequest, ValueChainUpdateRequest, ValueChainListParams, PageResponse<T>

**Validação**: Types compilam, autocomplete funciona

---

#### 4.2 - Criar ValueChainService
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: API client

**Arquivo**: `/frontend/src/services/valueChain.service.ts`

**Métodos**: list(), getById(), create(), update(), deleteImage(), delete(), buildFormData()

**Validação**: Service funciona, FormData construído corretamente

---

### FASE 5: Frontend - Componentes e Página

#### 5.1 - Criar ValueChainList.vue
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 3h

**Objetivo**: Tabela com filtros e paginação

**Arquivo**: `/frontend/src/components/valueChain/ValueChainList.vue`

**Características**:
- v-data-table-server do Vuetify
- Filtros: busca (debounce 500ms), status
- Colunas: Avatar, Nome+Descrição, Status chip, Data, Ações
- Emits: update:filters, update:pagination, edit, delete

**Validação**: Componente renderiza, filtros funcionam, eventos emitidos

---

#### 5.2 - Criar ValueChainForm.vue
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 2h30min

**Objetivo**: Formulário create/edit

**Arquivo**: `/frontend/src/components/valueChain/ValueChainForm.vue`

**Campos**:
- Upload imagem: preview 120px, validação 5MB/tipos
- Nome: obrigatório, counter 255
- Descrição: opcional, counter 5000
- Instituição: readonly
- Status: select Ativa/Inativa

**Validação**: Form valida, hasChanges funciona, emite submit corretamente

---

#### 5.3 - Criar ValueChainsPage.vue
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 2h

**Objetivo**: Página principal

**Arquivo**: `/frontend/src/views/private/ValueChainsPage.vue`

**Estrutura**: PageHeader + ValueChainList + Dialogs + Snackbar

**Validação**: Página funciona, CRUD completo operacional

---

#### 5.4 - Adicionar Rota e Menu
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 30min

**Objetivos**:
- Adicionar rota /value-chains em router/index.ts
- Adicionar item "Cadeias de Valor" no menu sidebar

**Validação**: Rota funciona, menu aparece, navegação OK

---

### FASE 6: Testes e Validação

#### 6.1 - Teste Multi-tenant Backend
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 2h

**Cenários**:
1. GESTOR cria cadeia → institution_id = sua instituição
2. GESTOR lista cadeias → só retorna da instituição dele
3. GESTOR tenta GET /value-chains/{id} de outra instituição → 403
4. ADMIN troca instituição → lista muda conforme contexto
5. Query SQL sempre tem WHERE institution_id = ?

**Validação**: Isolamento total de dados por instituição

---

#### 6.2 - Teste Upload
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 1h

**Cenários**:
1. Upload PNG 2MB → sucesso, thumbnail gerado
2. Upload 6MB → falha "exceeds maximum"
3. Upload TXT → falha "type not allowed"
4. GET /public/uploads/value-chains/{file} → retorna imagem
5. Update com nova imagem → antiga deletada
6. DELETE /value-chains/{id}/image → arquivo removido

**Validação**: Upload seguro e funcional

---

#### 6.3 - Teste End-to-End
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 1h30min

**Fluxo completo**:
1. Login GESTOR → acessa Cadeias de Valor
2. Cria nova cadeia com imagem
3. Valida que aparece na lista
4. Edita nome e descrição
5. Troca imagem
6. Testa filtros (busca, status)
7. Testa paginação e ordenação
8. Exclui cadeia (soft delete)
9. Filtra Inativas → cadeia aparece

**Validação**: Fluxo completo funciona sem erros

---

## Dependências Técnicas

### Backend
```xml
<!-- Já existentes -->
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-security
spring-boot-starter-validation
postgresql
flyway
lombok

<!-- Nenhuma nova dependência necessária -->
```

### Frontend
```json
// Já existentes
"vue": "^3.4"
"vuetify": "^3.5"
"pinia": "^2.1"
"vue-router": "^4.2"
"axios": "^1.6"
"typescript": "^5.2"

// Nenhuma nova dependência necessária
```

---

## Configuração .env

Adicionar ao backend `.env`:
```bash
# Storage configuration
STORAGE_PROVIDER=local
STORAGE_LOCAL_BASE_PATH=/var/simplifica/uploads
STORAGE_LOCAL_PUBLIC_URL=http://localhost:8080/api/public/uploads
STORAGE_MAX_FILE_SIZE_MB=5
STORAGE_ALLOWED_EXTENSIONS=jpg,jpeg,png,webp
```

---

## Riscos e Mitigações

### Risco 1: Upload de arquivos maliciosos
**Mitigação**: Validação rigorosa de MIME type real, tamanho, extensão; processar apenas formatos conhecidos

### Risco 2: Vazamento cross-tenant
**Mitigação**: Validação em múltiplas camadas (interceptor, service, repository); testes exaustivos de isolamento

### Risco 3: Performance com muitas imagens
**Mitigação**: Thumbnails para preview, lazy loading, paginação server-side, índices otimizados

### Risco 4: Disco cheio com uploads
**Mitigação**: Limite de tamanho configurável, limpeza de arquivos órfãos (task futura), monitoramento de espaço

---

## Validação Final

**Checklist de Aceite**:
- [ ] Sistema de upload funciona (validação, thumbnail, delete)
- [ ] Entidade ValueChain criada com relacionamento Institution
- [ ] Repository e Specifications funcionam
- [ ] Service valida multi-tenant em todas operações
- [ ] Controller REST com endpoints protegidos
- [ ] Frontend: lista, formulário, página completa
- [ ] Rota e menu adicionados
- [ ] GESTOR só acessa cadeias de sua instituição
- [ ] ADMIN acessa conforme instituição ativa
- [ ] Upload funciona: validação, thumbnail, URLs públicas
- [ ] Soft delete preserva dados
- [ ] Logs apropriados (INFO, DEBUG, WARN)
- [ ] Configurações .env documentadas

**Estimativa Total**: 24-28 horas de desenvolvimento

---

## Próximos Passos

1. **Implementação**: Seguir fases 1-5 sequencialmente
2. **Teste**: Executar fase 6 (testes multi-tenant, upload, e2e)
3. **Code Review**: Revisar código, padrões, segurança
4. **Deploy**: Subir para ambiente de staging
5. **Melhorias Futuras**:
   - Versionamento de cadeias
   - Relacionamentos hierárquicos
   - Export/import
   - Análise de métricas
