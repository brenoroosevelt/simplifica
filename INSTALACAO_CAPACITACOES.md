# Instalação do Sistema de Capacitações

## Dependências Frontend

O sistema de capacitações utiliza duas bibliotecas adicionais que precisam ser instaladas:

```bash
cd frontend
npm install vuedraggable@next @vueuse/core
```

### Descrição das dependências:

- **vuedraggable@next**: Biblioteca para drag-and-drop de elementos Vue 3, usada para reordenar vídeos na playlist
- **@vueuse/core**: Collection de composables Vue 3, usada para o debounce da busca

## Estrutura Implementada

### Backend (Java/Spring Boot)

#### Migrations
- `V12__create_trainings_table.sql` - Tabela de capacitações
- `V13__create_training_videos_table.sql` - Tabela de vídeos

#### Entities
- `Training.java` - Entidade principal de capacitação
- `TrainingVideo.java` - Entidade de vídeo da playlist

#### DTOs
- `TrainingDTO.java` - DTO de resposta completo
- `TrainingVideoDTO.java` - DTO de vídeo
- `CreateTrainingDTO.java` - DTO de criação
- `UpdateTrainingDTO.java` - DTO de atualização
- `CreateTrainingVideoDTO.java` - DTO de criação de vídeo
- `UpdateTrainingVideoDTO.java` - DTO de atualização de vídeo
- `ReorderVideosDTO.java` - DTO para reordenação

#### Repositories
- `TrainingRepository.java` - Repository com queries tenant-aware
- `TrainingVideoRepository.java` - Repository de vídeos

#### Specifications
- `TrainingSpecifications.java` - Queries dinâmicas para filtros

#### Services
- `TrainingService.java` - Lógica de negócio completa
  - CRUD de capacitações
  - Upload/delete de cover image
  - Gerenciamento de vídeos (add, update, delete, reorder)
  - Validações de tenant isolation

#### Controllers
- `TrainingController.java` - Endpoints REST
  - GET /trainings (list com filtros e paginação)
  - GET /trainings/{id} (buscar por ID)
  - POST /trainings (criar)
  - PUT /trainings/{id} (atualizar)
  - DELETE /trainings/{id} (soft delete)
  - POST /trainings/{id}/cover-image (upload capa)
  - DELETE /trainings/{id}/cover-image (remover capa)
  - POST /trainings/{trainingId}/videos (adicionar vídeo)
  - PUT /trainings/{trainingId}/videos/{videoId} (atualizar vídeo)
  - DELETE /trainings/{trainingId}/videos/{videoId} (remover vídeo)
  - PUT /trainings/{trainingId}/videos/reorder (reordenar vídeos)

#### Atualizações
- `FileStorageService.java` - Adicionado "trainings" ao whitelist

### Frontend (Vue 3 + TypeScript + Vuetify)

#### Types
- `training.types.ts` - Interfaces TypeScript completas

#### Services
- `training.service.ts` - Client API com todos os métodos

#### Components
- `YouTubePlayer.vue` - Player de vídeo YouTube com iframe
- `TrainingVideoForm.vue` - Formulário de vídeo com validação
- `TrainingVideoManager.vue` - Gerenciador de playlist com drag-and-drop
- `TrainingForm.vue` - Formulário principal de capacitação
- `TrainingList.vue` - Listagem em cards com filtros

#### Composables
- `useTrainingList.ts` - Lógica de listagem e filtros
- `useTrainingForm.ts` - Lógica de formulário e CRUD

#### Views
- `TrainingsPage.vue` - Página principal completa
  - Listagem com cards visuais
  - Modal de criação com primeiro vídeo
  - Modal de edição com gerenciamento de vídeos
  - Modal de visualização com players
  - Upload de capa
  - Reordenação de vídeos por drag-and-drop

#### Router
- `/trainings` - Rota adicionada com proteção de autenticação

#### Navigation
- `AppSidebar.vue` - Item "Capacitações" adicionado ao menu

## Funcionalidades Implementadas

### Backend
✅ Multi-tenant isolation (institution_id)
✅ CRUD completo de capacitações
✅ Upload de imagem de capa
✅ Gerenciamento de playlist de vídeos
✅ Reordenação de vídeos
✅ Validações (mínimo 1 vídeo, order_index único, YouTube URL válida)
✅ Filtros e paginação
✅ Soft delete
✅ Cálculo automático de videoCount e totalDurationMinutes

### Frontend
✅ Listagem em cards visuais com cover image
✅ Busca e filtros (ativas/inativas)
✅ Paginação
✅ Formulário de criação com primeiro vídeo
✅ Formulário de edição
✅ Upload de cover image
✅ Gerenciamento de playlist:
  - Adicionar vídeos
  - Editar vídeos
  - Remover vídeos
  - Reordenar por drag-and-drop
✅ Visualização com YouTube players
✅ Validação de YouTube URL
✅ Feedback visual (snackbar)
✅ Estados de loading
✅ Confirmação de exclusão

## Segurança

✅ Isolamento por tenant (institution_id)
✅ @PreAuthorize no controller
✅ validateTenantAccess no service
✅ Queries sempre filtradas por institution_id
✅ Validação de YouTube URL (regex pattern)
✅ Upload de imagem com validação de tipo e tamanho

## Validações Implementadas

### Backend
- Título obrigatório (max 255 chars)
- Mínimo 1 vídeo por capacitação
- Order index único por training
- YouTube URL válida (regex)
- Tenant ownership em todas operações

### Frontend
- Campos obrigatórios marcados
- Contadores de caracteres
- Validação de YouTube URL em tempo real
- Não permitir excluir último vídeo
- Drag-and-drop visual

## Como Testar

1. Instalar dependências do frontend:
```bash
cd frontend
npm install vuedraggable@next @vueuse/core
```

2. Iniciar o backend (Flyway rodará as migrations automaticamente)

3. Iniciar o frontend

4. Acessar a rota /trainings

5. Criar uma capacitação:
   - Preencher título e descrição
   - Adicionar pelo menos um vídeo com URL do YouTube
   - Salvar

6. Editar a capacitação:
   - Upload de capa
   - Adicionar mais vídeos
   - Reordenar vídeos por drag-and-drop
   - Editar/remover vídeos

7. Visualizar a capacitação:
   - Ver vídeos em players do YouTube
   - Verificar informações (duração, quantidade)

## Observações

- As migrations serão executadas automaticamente pelo Flyway
- O sistema usa soft delete (active flag)
- Todas as operações respeitam o tenant atual
- YouTube video IDs são extraídos automaticamente das URLs
- Cover images são armazenadas em `uploads/trainings/`
- O drag-and-drop mantém a ordem consistente no banco
