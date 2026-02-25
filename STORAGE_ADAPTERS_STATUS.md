# 📦 STATUS: Implementação de Storage Adapters (Flysystem-style)

## ✅ COMPLETADO (Tasks 10-15)

### 1. **Dependências Adicionadas** (Task #10)
- ✅ Dropbox Java SDK
- ✅ Google Drive API
- ✅ Google API Client
- ⚠️ Precisa ajustar versões (erros de compilação)

### 2. **Interface StorageAdapter Criada** (Task #11)
- ✅ `StorageAdapter.java` - Interface unificada
- ✅ `StorageException.java` - Exception customizada
- ✅ Métodos: store(), retrieve(), delete(), exists(), getPublicUrl()

### 3. **Adapters Implementados** (Tasks #12-14)
- ✅ `FilesystemStorageAdapter.java` - Armazenamento local (default)
- ✅ `DropboxStorageAdapter.java` - Dropbox API
- ✅ `GoogleDriveStorageAdapter.java` - Google Drive API

### 4. **Factory Criado** (Task #15)
- ✅ `StorageAdapterFactory.java` - Cria adapter baseado em config
- ✅ `StorageProperties.java` - Atualizado com config dos 3 adapters

---

## ⏳ PENDENTE (Tasks 16-19)

### 5. **Simplificar StorageService** (Task #16)
**O que fazer:**
- Refatorar `FileStorageServiceV2` para usar `StorageAdapter`
- Remover lógica de filesystem específica
- Delegar tudo para o adapter

**Código esperado:**
```java
@Service
public class StorageService {
    private final StorageAdapter adapter;

    public StorageService(StorageAdapterFactory factory, StorageProperties properties) {
        this.adapter = factory.createAdapter(properties, baseUrl);
    }

    public FileUploadResult storeFile(...) {
        // Validações
        String storedPath = adapter.store(path, inputStream, contentType);
        // Salvar metadata no banco
        // Retornar resultado
    }
}
```

### 6. **Criar/Atualizar .env** (Task #17)
**Criar arquivos:**

**`.env`:**
```bash
# Storage Configuration (Flysystem-style)
# Options: local, dropbox, googledrive
STORAGE_PROVIDER=local

# Local/Filesystem Storage (default)
STORAGE_FILESYSTEM_ROOT=/tmp/simplifica/storage

# Dropbox Storage (se STORAGE_PROVIDER=dropbox)
# STORAGE_DROPBOX_ACCESS_TOKEN=seu-token-aqui
# STORAGE_DROPBOX_BASE_PATH=/simplifica

# Google Drive Storage (se STORAGE_PROVIDER=googledrive)
# STORAGE_GOOGLEDRIVE_CREDENTIALS_PATH=/path/to/credentials.json
# STORAGE_GOOGLEDRIVE_BASE_FOLDER_NAME=Simplifica
```

**`.env.example`:**
```bash
# Storage Configuration
STORAGE_PROVIDER=local

# Filesystem
STORAGE_FILESYSTEM_ROOT=/tmp/simplifica/storage

# Dropbox (obter token em: https://www.dropbox.com/developers/apps)
# STORAGE_DROPBOX_ACCESS_TOKEN=
# STORAGE_DROPBOX_BASE_PATH=/simplifica

# Google Drive (criar Service Account em: https://console.cloud.google.com)
# STORAGE_GOOGLEDRIVE_CREDENTIALS_PATH=
# STORAGE_GOOGLEDRIVE_BASE_FOLDER_NAME=Simplifica
```

### 7. **Atualizar application.yml** (Task #18)
```yaml
app:
  storage:
    provider: ${STORAGE_PROVIDER:local}

    # Filesystem
    filesystem:
      root-path: ${STORAGE_FILESYSTEM_ROOT:/tmp/simplifica/storage}

    # Dropbox
    dropbox:
      access-token: ${STORAGE_DROPBOX_ACCESS_TOKEN:}
      base-path: ${STORAGE_DROPBOX_BASE_PATH:/simplifica}

    # Google Drive
    google-drive:
      credentials-path: ${STORAGE_GOOGLEDRIVE_CREDENTIALS_PATH:}
      base-folder-name: ${STORAGE_GOOGLEDRIVE_BASE_FOLDER_NAME:Simplifica}

    # File validation
    max-file-size: 104857600
    max-video-size: 524288000
    allowed-image-types: image/jpeg,image/png,image/webp,image/gif
```

### 8. **Limpar Código Obsoleto** (Task #19)
**Remover:**
- ❌ `FileStorageServiceV2.java` (será substituído)
- ❌ `ValueChainImageStore.java` (Spring Content não usado mais)
- ❌ `StorageConfig.java` (simplificar)
- ❌ Dependência `spring-content-fs-boot-starter`

---

## 🐛 PROBLEMAS ATUAIS

### **Erro de Compilação:**
```
[ERROR] package com.google.auth.http does not exist
[ERROR] package com.google.auth.oauth2 does not exist
```

**Causa:** Versões de dependências incompatíveis

**Solução:** Atualizar `pom.xml`:
```xml
<!-- Usar versões compatíveis -->
<dependency>
    <groupId>com.google.apis</groupId>
    <artifactId>google-api-services-drive</artifactId>
    <version>v3-rev20220815-2.0.0</version>
</dependency>

<dependency>
    <groupId>com.google.auth</groupId>
    <artifactId>google-auth-library-oauth2-http</artifactId>
    <version>1.19.0</version>
</dependency>

<dependency>
    <groupId>com.google.api-client</groupId>
    <artifactId>google-api-client</artifactId>
    <version>2.2.0</version>
</dependency>
```

---

## 🎯 PRÓXIMOS PASSOS

### **Opção 1: Continuar Implementação Completa**
1. Corrigir dependências (pom.xml)
2. Compilar e testar
3. Completar tasks 16-19
4. Testar os 3 adapters

### **Opção 2: Simplificar e Usar Apenas Local**
1. Remover adapters Dropbox e Google Drive
2. Manter apenas FilesystemStorageAdapter
3. Simplificar configuração
4. Implementar outros adapters quando necessário

### **Opção 3: Usar Spring Content Puro**
1. Voltar para abordagem original
2. Usar `@EnableFilesystemStores`
3. Trocar apenas a dependência (fs → s3 → gcs)
4. Muito mais simples

---

## 📚 COMO USAR (quando completar)

### **1. Local (Development)**
```bash
# .env
STORAGE_PROVIDER=local
STORAGE_FILESYSTEM_ROOT=/tmp/simplifica/storage
```

### **2. Dropbox**
```bash
# 1. Criar app: https://www.dropbox.com/developers/apps
# 2. Gerar Access Token
# 3. Configurar:
STORAGE_PROVIDER=dropbox
STORAGE_DROPBOX_ACCESS_TOKEN=seu-token
STORAGE_DROPBOX_BASE_PATH=/simplifica
```

### **3. Google Drive**
```bash
# 1. Criar projeto: https://console.cloud.google.com
# 2. Ativar Google Drive API
# 3. Criar Service Account e baixar JSON
# 4. Configurar:
STORAGE_PROVIDER=googledrive
STORAGE_GOOGLEDRIVE_CREDENTIALS_PATH=/path/to/credentials.json
STORAGE_GOOGLEDRIVE_BASE_FOLDER_NAME=Simplifica
```

---

## 💡 RECOMENDAÇÃO

**Sugiro Opção 2 ou 3:**
- Implementar adapters complexos (Dropbox/Drive) só quando REALMENTE precisar
- Por enquanto, manter local é suficiente
- Quando escalar, migrar para GCS (Google Cloud Storage) é mais profissional
- GCS tem suporte nativo do Spring Content (muito mais simples)

---

## ❓ DECISÃO NECESSÁRIA

**Você prefere:**
1. ✅ Continuar e completar os 3 adapters (mais complexo)
2. ✅ Simplificar e usar apenas local por enquanto (recomendado)
3. ✅ Voltar para Spring Content puro e usar GCS quando precisar cloud

**Me avise qual opção prefere e continuo a implementação!**
