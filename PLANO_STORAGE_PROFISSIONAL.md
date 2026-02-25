# 📋 PLANO DETALHADO: SISTEMA DE GESTÃO DE ARQUIVOS PROFISSIONAL

## 🎯 DECISÃO ARQUITETURAL

**Solução Escolhida: Spring Content + Filesystem (com preparação para Cloud)**

**Justificativa:**
- ✅ Começar local (infra institucional)
- ✅ Path claro de migração para cloud (S3/Azure/GCS)
- ✅ Suporte nativo a streaming (essencial para vídeos)
- ✅ Integração perfeita com Spring Boot
- ✅ Zero custom code de abstração
- ✅ Cache HTTP já built-in no Spring

---

## 🏗️ ARQUITETURA PROPOSTA

```
┌─────────────────────────────────────────────────────────────┐
│                     CAMADA DE APLICAÇÃO                      │
│  InstitutionService | ProcessService | TrainingService      │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                  CAMADA DE STORAGE (Nova)                    │
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ LogoStore    │  │ MappingStore │  │ MediaStore   │      │
│  │ (interface)  │  │ (interface)  │  │ (interface)  │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
│         │                  │                  │               │
│         └──────────────────┴──────────────────┘              │
│                            │                                  │
│                   ┌────────▼────────┐                        │
│                   │ Spring Content  │                        │
│                   │  ContentStore   │                        │
│                   └────────┬────────┘                        │
└────────────────────────────┼────────────────────────────────┘
                             │
            ┌────────────────┴────────────────┐
            │                                  │
    ┌───────▼────────┐              ┌─────────▼──────────┐
    │   Filesystem   │              │   Cloud Storage    │
    │   (Atual)      │  ──Future──> │  (S3/Azure/GCS)   │
    └────────────────┘              └────────────────────┘
```

---

## 📁 ESTRUTURA DE DIRETÓRIOS

```
storage/
├── institutions/
│   ├── {institutionId}/
│   │   ├── logo/
│   │   │   └── original.{ext}
│   │   └── metadata.json
│
├── processes/
│   ├── {processId}/
│   │   ├── mappings/
│   │   │   ├── {mappingId}/
│   │   │   │   ├── index.html
│   │   │   │   ├── assets/
│   │   │   │   └── metadata.json
│   │   └── documents/
│   │       └── {documentId}.{ext}
│
├── trainings/
│   ├── {trainingId}/
│   │   ├── cover/
│   │   │   └── image.{ext}
│   │   ├── videos/
│   │   │   ├── {videoId}/
│   │   │   │   ├── original.mp4
│   │   │   │   └── metadata.json
│   │   └── attachments/
│   │       └── {attachmentId}.{ext}
│
└── temp/
    └── uploads/
        └── {sessionId}/
            └── {tempfile}
```

**Convenções:**
- IDs sempre em UUID format
- Metadata em JSON (tamanho, mimetype, upload date, hash)
- Separação clara por entidade de domínio
- Temp para uploads parciais/processamento

---

## 🔧 IMPLEMENTAÇÃO TÉCNICA

### 1. Dependências (pom.xml)

```xml
<!-- Spring Content Filesystem -->
<dependency>
    <groupId>com.github.paulcwarren</groupId>
    <artifactId>spring-content-fs-boot-starter</artifactId>
    <version>3.0.12</version>
</dependency>

<!-- Spring Content REST (opcional - expor via REST) -->
<dependency>
    <groupId>com.github.paulcwarren</groupId>
    <artifactId>spring-content-rest-boot-starter</artifactId>
    <version>3.0.12</version>
</dependency>

<!-- Para futuro: Spring Content S3 -->
<!--
<dependency>
    <groupId>com.github.paulcwarren</groupId>
    <artifactId>spring-content-s3-boot-starter</artifactId>
    <version>3.0.12</version>
</dependency>
-->

<!-- Tika para detecção de MIME types -->
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-core</artifactId>
    <version>2.9.1</version>
</dependency>

<!-- Thumbnailator para resize de imagens (opcional) -->
<dependency>
    <groupId>net.coobird</groupId>
    <artifactId>thumbnailator</artifactId>
    <version>0.4.20</version>
</dependency>
```

### 2. Configuração (application.yml)

```yaml
spring:
  content:
    storage:
      type: filesystem  # Trocar para 's3' quando migrar
      filesystem:
        root: ${STORAGE_ROOT:/var/simplifica/storage}
      # Para futuro cloud:
      # s3:
      #   bucket: simplifica-files
      #   region: us-east-1

app:
  storage:
    max-file-size: 100MB
    max-video-size: 500MB
    allowed-image-types: image/jpeg,image/png,image/webp,image/gif
    allowed-document-types: application/pdf,application/zip
    allowed-video-types: video/mp4,video/webm
    cache:
      enabled: true
      max-age: 86400  # 24h para arquivos estáticos
```

### 3. Estrutura de Classes

```
com.simplifica.storage/
├── config/
│   ├── StorageConfig.java
│   └── StorageProperties.java
│
├── domain/
│   ├── StorageType.java (enum)
│   ├── StoredFile.java (metadata entity)
│   └── FileCategory.java (enum)
│
├── store/
│   ├── InstitutionLogoStore.java (interface)
│   ├── ProcessMappingStore.java (interface)
│   ├── TrainingMediaStore.java (interface)
│   └── GenericFileStore.java (interface)
│
├── service/
│   ├── StorageService.java (interface - alta abstração)
│   ├── FileStorageService.java (implementação Spring Content)
│   ├── FileValidationService.java
│   ├── FileMetadataService.java
│   └── StreamingService.java (para vídeos)
│
├── repository/
│   └── StoredFileRepository.java (JPA)
│
└── controller/
    ├── FileServeController.java (substituir FileController)
    └── FileUploadController.java
```

---

## 🎨 PADRÕES DE DESIGN APLICADOS

### Pattern 1: Store per Entity (Spring Content)

```java
// Interface por tipo de conteúdo
@StoreRestResource
public interface InstitutionLogoStore extends ContentStore<Institution, UUID> {
    // Spring Content gera implementação automaticamente
}

@StoreRestResource
public interface ProcessMappingStore extends ContentStore<ProcessMapping, UUID> {
    // Métodos customizados se necessário
    InputStream getContent(ProcessMapping mapping);
}

@StoreRestResource
public interface TrainingMediaStore extends ContentStore<TrainingMedia, UUID> {
    // Suporte a range requests para streaming
}
```

### Pattern 2: Service Layer (Facade)

```java
public interface StorageService {
    // Upload
    StoredFile storeInstitutionLogo(UUID institutionId, MultipartFile file);
    StoredFile storeProcessMapping(UUID processId, MultipartFile zipFile);
    StoredFile storeTrainingCover(UUID trainingId, MultipartFile file);
    StoredFile storeTrainingVideo(UUID trainingId, MultipartFile file);

    // Download
    Resource loadAsResource(UUID fileId);
    InputStream loadAsStream(UUID fileId);

    // Metadata
    FileMetadata getMetadata(UUID fileId);

    // Cleanup
    void delete(UUID fileId);
    void deleteByEntity(String entityType, UUID entityId);
}
```

### Pattern 3: Strategy para Cloud Migration

```java
@Configuration
public class StorageConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.content.storage.type", havingValue = "filesystem")
    public FileSystemResourceLoader fileSystemResourceLoader() {
        return new FileSystemResourceLoader(storageProperties.getRoot());
    }

    @Bean
    @ConditionalOnProperty(name = "spring.content.storage.type", havingValue = "s3")
    public S3ResourceLoader s3ResourceLoader() {
        return new S3ResourceLoader(s3Client, bucketName);
    }
}
```

---

## 🔒 SEGURANÇA

### Validações Implementadas:

1. **Path Traversal Prevention**
   ```java
   // Spring Content já previne, mas adicionar camada extra
   private void validatePath(String path) {
       Path normalized = Paths.get(path).normalize();
       if (!normalized.startsWith(rootPath)) {
           throw new SecurityException("Path traversal detected");
       }
   }
   ```

2. **File Type Validation**
   ```java
   // Validar por MIME type real (não apenas extensão)
   @Component
   public class FileValidationService {
       private final Tika tika = new Tika();

       public void validateImageFile(MultipartFile file) {
           String detectedType = tika.detect(file.getInputStream());
           if (!allowedImageTypes.contains(detectedType)) {
               throw new InvalidFileTypeException();
           }
       }
   }
   ```

3. **Size Limits**
   ```java
   @Configuration
   public class MultipartConfig {
       @Bean
       public MultipartConfigElement multipartConfigElement() {
           MultipartConfigFactory factory = new MultipartConfigFactory();
           factory.setMaxFileSize(DataSize.ofMegabytes(100));
           factory.setMaxRequestSize(DataSize.ofMegabytes(100));
           return factory.createMultipartConfig();
       }
   }
   ```

4. **Access Control**
   ```java
   // Validar se usuário tem permissão para acessar arquivo
   @PreAuthorize("@fileSecurityService.canAccess(#fileId)")
   public Resource loadAsResource(UUID fileId) {
       // ...
   }
   ```

---

## ⚡ OTIMIZAÇÕES DE PERFORMANCE

### 1. Cache HTTP (ETag + Last-Modified)

```java
@GetMapping("/files/{fileId}")
public ResponseEntity<Resource> serveFile(
        @PathVariable UUID fileId,
        @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
        @RequestHeader(value = "If-Modified-Since", required = false) String ifModifiedSince) {

    FileMetadata metadata = metadataService.getMetadata(fileId);
    String etag = metadata.getEtag(); // MD5 hash

    // Check cache
    if (etag.equals(ifNoneMatch)) {
        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
    }

    Resource resource = storageService.loadAsResource(fileId);

    return ResponseEntity.ok()
            .eTag(etag)
            .lastModified(metadata.getLastModified())
            .cacheControl(CacheControl.maxAge(24, TimeUnit.HOURS))
            .contentType(MediaType.parseMediaType(metadata.getContentType()))
            .body(resource);
}
```

### 2. Streaming para Vídeos (Range Requests)

```java
@GetMapping("/videos/{videoId}")
public ResponseEntity<InputStreamResource> streamVideo(
        @PathVariable UUID videoId,
        @RequestHeader(value = "Range", required = false) String range) {

    TrainingMedia video = trainingMediaRepository.findById(videoId)
            .orElseThrow(() -> new NotFoundException());

    if (range == null) {
        // Full video
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp4"))
                .body(new InputStreamResource(videoStore.getContent(video)));
    }

    // Partial content (range request)
    return streamingService.servePartialContent(video, range);
}
```

### 3. Thumbnail Generation (On-Demand)

```java
@GetMapping("/images/{imageId}/thumbnail")
public ResponseEntity<Resource> getThumbnail(
        @PathVariable UUID imageId,
        @RequestParam(defaultValue = "200") int width) {

    // Check se thumbnail já existe em cache
    Resource cached = thumbnailCache.get(imageId, width);
    if (cached != null) {
        return ResponseEntity.ok(cached);
    }

    // Gerar thumbnail
    Resource original = storageService.loadAsResource(imageId);
    Resource thumbnail = thumbnailService.resize(original, width);

    // Cachear
    thumbnailCache.put(imageId, width, thumbnail);

    return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
            .body(thumbnail);
}
```

---

## 🔄 MIGRAÇÃO DO CÓDIGO ATUAL

### Fase 1: Preparação (Sem Breaking Changes)

1. Criar nova estrutura de packages `storage/`
2. Implementar novos services com Spring Content
3. Manter `FileStorageService` atual como legacy
4. Criar adapter entre novo e antigo sistema

### Fase 2: Migração Gradual

1. **Institutions (mais simples)**
   - Migrar upload de logos primeiro
   - Testar thoroughly

2. **Trainings (novo recurso)**
   - Implementar já no novo sistema
   - Zero migração necessária

3. **Processes (mais complexo)**
   - Migrar estrutura de pastas do Bizagi
   - Script de migração de arquivos existentes
   - Manter compatibilidade de URLs

### Fase 3: Cleanup

1. Deprecar `FileStorageService` antigo
2. Remover código legacy
3. Atualizar documentação

---

## 🚀 PREPARAÇÃO PARA CLOUD

### Design Decisions Cloud-Ready:

1. **URLs Absolutas (CDN-Ready)**
   ```java
   // Sempre retornar URL absoluta
   public String getFileUrl(UUID fileId) {
       if (storageType == StorageType.S3) {
           return s3Client.getUrl(bucketName, key).toString();
       } else {
           return baseUrl + "/files/" + fileId;
       }
   }
   ```

2. **Presigned URLs (para S3 futuro)**
   ```java
   public String getPresignedUrl(UUID fileId, Duration expiration) {
       if (storageType == StorageType.S3) {
           return s3Client.generatePresignedUrl(request);
       } else {
           // Token temporário para filesystem
           return tokenService.generateTemporaryUrl(fileId, expiration);
       }
   }
   ```

3. **Metadata Separada do Storage**
   ```java
   @Entity
   @Table(name = "stored_files")
   public class StoredFile {
       @Id private UUID id;
       private String entityType;  // "institution", "process", etc
       private UUID entityId;
       private String filename;
       private String contentType;
       private Long size;
       private String storageKey;  // Path or S3 key
       private String etag;
       private LocalDateTime uploadedAt;
   }
   ```

4. **Feature Toggle**
   ```yaml
   app:
     storage:
       mode: filesystem  # ou 's3', 'azure', 'gcs'
   ```

---

## 📊 MONITORING & OBSERVABILITY

```java
@Aspect
@Component
public class StorageMetricsAspect {

    private final MeterRegistry meterRegistry;

    @Around("@annotation(Timed)")
    public Object measureStorageOperation(ProceedingJoinPoint pjp) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Object result = pjp.proceed();
            sample.stop(Timer.builder("storage.operation")
                    .tag("operation", pjp.getSignature().getName())
                    .tag("status", "success")
                    .register(meterRegistry));
            return result;
        } catch (Throwable e) {
            sample.stop(Timer.builder("storage.operation")
                    .tag("operation", pjp.getSignature().getName())
                    .tag("status", "error")
                    .register(meterRegistry));
            throw new RuntimeException(e);
        }
    }
}
```

**Métricas a monitorar:**
- Upload time por tipo de arquivo
- Storage usage por entidade
- Cache hit rate
- Erros de validação
- Bandwidth usage

---

## 📝 CHECKLIST DE IMPLEMENTAÇÃO

### Sprint 1: Fundação (1-2 semanas)
- [ ] Adicionar dependências Spring Content
- [ ] Criar estrutura de packages
- [ ] Implementar `StorageService` interface
- [ ] Implementar `FileValidationService`
- [ ] Configurar storage filesystem
- [ ] Entity `StoredFile` + Repository
- [ ] Testes unitários

### Sprint 2: Migração Institutions (1 semana)
- [ ] Implementar `InstitutionLogoStore`
- [ ] Migrar `InstitutionService` para usar novo storage
- [ ] Script de migração de logos existentes
- [ ] Atualizar controller de institutions
- [ ] Testes de integração
- [ ] Deploy e validação

### Sprint 3: Processes Mapping (2 semanas)
- [ ] Implementar `ProcessMappingStore`
- [ ] Adaptar extração de ZIP para novo storage
- [ ] Migrar `ProcessService`
- [ ] Atualizar `FileController` para usar Spring Content
- [ ] Script de migração de mappings existentes
- [ ] Testes de streaming/serving de HTML
- [ ] Deploy e validação

### Sprint 4: Trainings (novo - 1 semana)
- [ ] Implementar `TrainingMediaStore`
- [ ] Upload de covers
- [ ] Upload de vídeos com validação
- [ ] Streaming service com range requests
- [ ] Controllers
- [ ] Frontend integration
- [ ] Testes

### Sprint 5: Otimizações (1 semana)
- [ ] Implementar cache HTTP (ETag)
- [ ] Thumbnail generation service
- [ ] Compression para imagens
- [ ] Métricas e monitoring
- [ ] Load testing
- [ ] Documentação API

### Sprint 6: Cloud Preparation (1 semana)
- [ ] Feature toggle para storage mode
- [ ] Presigned URL logic
- [ ] Metadata external to storage
- [ ] Configuration templates para S3/Azure/GCS
- [ ] Documentação de migração cloud
- [ ] Testes com MinIO (S3-compatible local)

---

## 🎯 RESULTADO ESPERADO

Ao final da implementação você terá:

✅ **Sistema profissional de storage**
- Abstração limpa e testável
- Fácil manutenção e evolução
- Zero vendor lock-in

✅ **Performance otimizada**
- Cache HTTP eficiente
- Streaming de vídeos
- Thumbnails on-demand

✅ **Pronto para escalar**
- Migração cloud transparente
- CDN-ready
- Arquivos separados por domínio

✅ **Seguro**
- Validações robustas
- Access control
- Path traversal prevention

✅ **Monitorável**
- Métricas de uso
- Alertas de erros
- Auditoria de acessos

---

## 💰 ESTIMATIVA DE ESFORÇO

| Fase | Esforço | Complexidade | Risco |
|------|---------|--------------|-------|
| Sprint 1 - Fundação | 8-12h | Média | Baixo |
| Sprint 2 - Institutions | 4-6h | Baixa | Baixo |
| Sprint 3 - Processes | 10-16h | Alta | Médio |
| Sprint 4 - Trainings | 6-8h | Média | Baixo |
| Sprint 5 - Otimizações | 6-8h | Média | Baixo |
| Sprint 6 - Cloud Prep | 4-6h | Média | Baixo |
| **TOTAL** | **38-56h** | - | - |

---

## ❓ DÚVIDAS/VALIDAÇÕES

1. **Storage root path**: `/var/simplifica/storage` está ok ou prefere outro local?

2. **Backup strategy**: Você já tem backup da pasta de storage configurado?

3. **Trainings**: Além de cover e vídeos, vai ter PDFs/documentos anexos?

4. **Versionamento**: Quer manter histórico quando substitui um arquivo (ex: trocar logo)?

5. **Auditoria**: Precisa registrar quem fez upload/download de cada arquivo?

---

## 📚 REFERÊNCIAS

### Documentação Oficial
- [Spring Content GitHub](https://github.com/paulcwarren/spring-content)
- [Spring Content Filesystem Reference](https://paulcwarren.github.io/spring-content/refs/release/1.2.4/fs-index.html)
- [Apache Commons VFS](https://commons.apache.org/vfs/)
- [JClouds BlobStore Guide](https://jclouds.apache.org/start/blobstore/)

### Artigos e Tutoriais
- [Exploring File Storage Solutions in Spring Boot](https://foojay.io/today/exploring-file-storage-solutions-in-spring-boot-database-local-systems-cloud-services-and-beyond/)
- [File Storage in Spring Boot Explained](https://medium.com/@sunil17bbmp/file-storage-in-spring-boot-database-local-and-cloud-explained-96cbec7fa9d8)
- [Java High-Performance Local Cache](https://www.alibabacloud.com/blog/java-high-performance-local-cache-practices_599804)
- [Caching Best Practices](https://vladmihalcea.com/caching-best-practices/)
- [Spring Cache Tutorial](https://www.baeldung.com/spring-cache-tutorial)

### Cloud Storage
- [Spring Cloud GCP Storage](https://googlecloudplatform.github.io/spring-cloud-gcp/reference/html/storage.html)
- [Access files in Cloud Storage with Spring Resource](https://codelabs.developers.google.com/codelabs/spring-cloud-gcp-gcs)
- [Spring Cloud Azure Resource Handling](https://learn.microsoft.com/en-us/azure/developer/java/spring-framework/resource-handling)

---

**Data de Criação**: 2026-02-23
**Versão**: 1.0
**Status**: Aguardando Validação
