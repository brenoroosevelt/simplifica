# CORREÇÕES SUGERIDAS - TRILHA 3 ValueChain

## Overview

Este documento fornece as correções específicas para as 3 ressalvas encontradas na revisão de código da TRILHA 3. As correções são **opcionais mas recomendadas** para manter o código alinhado com best practices.

---

## CORREÇÃO #1: Tratamento de TenantContext Nulo

### Arquivo
`backend/src/main/java/com/simplifica/application/service/ValueChainService.java`

### Problema
Linha 63: Lança `BadRequestException` quando deveria ser `UnauthorizedAccessException` (ou melhor ainda, isso nunca deveria acontecer)

### Código Atual
```java
private UUID getCurrentInstitutionId() {
    UUID institutionId = TenantContext.getCurrentInstitution();
    if (institutionId == null) {
        throw new BadRequestException("No institution context found. Please select an institution.");
    }
    return institutionId;
}
```

### Código Corrigido
```java
/**
 * Gets the current institution ID from TenantContext.
 * CRITICAL: This should always be set by the request interceptor.
 *
 * @return the current institution ID
 * @throws UnauthorizedAccessException if no institution context is set
 */
private UUID getCurrentInstitutionId() {
    UUID institutionId = TenantContext.getCurrentInstitution();
    if (institutionId == null) {
        // Log como erro crítico - isso indica falha no interceptor
        LOGGER.error("SECURITY ALERT: TenantContext is null. Interceptor may have failed to set institution context.");
        throw new UnauthorizedAccessException("Access denied: No institution context found");
    }
    return institutionId;
}
```

### Explicação
1. **Muda a exceção** de `BadRequestException` (400) para `UnauthorizedAccessException` (403)
   - 400: Cliente enviou dados ruins
   - 403: Cliente não tem permissão
   - **Semanticamente correto**: TenantContext nulo = acesso não autorizado

2. **Adiciona logging de erro crítico**
   - `LOGGER.error()` em vez de silencioso
   - Alerta operacional que algo deu errado no interceptor

3. **Clarifica com comentário CRITICAL**
   - Indica que isso deve ser mantido por interceptor

---

## CORREÇÃO #2: Validação em UpdateValueChainDTO

### Arquivo
`backend/src/main/java/com/simplifica/application/dto/UpdateValueChainDTO.java`

### Problema
Linha 22: Validação `@Size(min = 1)` é confusa em campo opcional

```java
@Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
private String name;
```

Quando `name` é null (não fornecido), `@Size` é ignorado. Mas quando é vazio, falha. Confuso.

### Código Atual
```java
package com.simplifica.application.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating an existing value chain.
 *
 * All fields are optional to support partial updates.
 * Only non-null fields will be applied to the value chain.
 * Used in PUT/PATCH requests to update value chains.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateValueChainDTO {

    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private Boolean active;
}
```

### Código Corrigido - OPÇÃO 1: Clarificar a mensagem

```java
package com.simplifica.application.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating an existing value chain.
 *
 * All fields are optional to support partial updates.
 * Only non-null fields will be applied to the value chain.
 * Used in PUT/PATCH requests to update value chains.
 *
 * Validation Rules:
 * - name: If provided, must be 1-255 characters (cannot be blank)
 * - description: If provided, must not exceed 5000 characters
 * - active: If provided, must be boolean
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateValueChainDTO {

    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters (cannot be blank)")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private Boolean active;
}
```

### Código Corrigido - OPÇÃO 2: Mais rigoroso (Recomendado)

```java
package com.simplifica.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating an existing value chain.
 *
 * All fields are optional to support partial updates.
 * Only non-null fields will be applied to the value chain.
 * Used in PUT/PATCH requests to update value chains.
 *
 * Validation Rules:
 * - name: If provided, must be non-blank and 1-255 characters
 * - description: If provided, must not exceed 5000 characters
 * - active: If provided, must be boolean
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateValueChainDTO {

    @NotBlank(message = "Name cannot be blank if provided")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private Boolean active;
}
```

### Explicação

**OPÇÃO 1**: Apenas melhora a mensagem
- Menos invasivo
- Mantém a lógica existente
- Clarifica para desenvolvedores

**OPÇÃO 2** (Recomendado): Adiciona `@NotBlank`
- Mais rigoroso
- Impede que alguém envie `name: ""` (string vazio)
- Força nome válido quando fornecido
- Align com validações de criação

**Recomendação**: Usar **OPÇÃO 2** para consistência com `CreateValueChainDTO` que tem `@NotBlank`

---

## CORREÇÃO #3: Padronização de Paginação

### Arquivo
`backend/src/main/java/com/simplifica/presentation/controller/ValueChainController.java`

### Problema
Linhas 54-71: Paginação usa `@RequestParam` manual em vez de `@PageableDefault` como em Institution

### Padrão Institution (correto)
```java
@GetMapping
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Page<InstitutionDTO>> listInstitutions(
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) InstitutionType type,
        @RequestParam(required = false) String search,
        @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
    Page<Institution> institutions = institutionService.findAll(active, type, search, pageable);
    Page<InstitutionDTO> dtos = institutions.map(InstitutionDTO::fromEntity);
    return ResponseEntity.ok(dtos);
}
```

### Código Atual (ValueChain)
```java
@GetMapping
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public ResponseEntity<Page<ValueChainDTO>> list(
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "name") String sort,
        @RequestParam(defaultValue = "asc") String direction) {

    Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

    Page<ValueChainDTO> valueChains = valueChainService.findAll(active, search, pageable);
    return ResponseEntity.ok(valueChains);
}
```

### Código Corrigido

```java
/**
 * Lists all value chains for the current institution with optional filters and pagination.
 *
 * @param active filter by active status (optional)
 * @param search search term for name (optional)
 * @param pageable pagination and sorting parameters
 * @return paginated list of value chains
 */
@GetMapping
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public ResponseEntity<Page<ValueChainDTO>> list(
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) String search,
        @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

    Page<ValueChainDTO> valueChains = valueChainService.findAll(active, search, pageable);
    return ResponseEntity.ok(valueChains);
}
```

### Mudanças

1. **Remove `@RequestParam` manuais**
   - Remove: `@RequestParam(defaultValue = "0") int page`
   - Remove: `@RequestParam(defaultValue = "10") int size`
   - Remove: `@RequestParam(defaultValue = "name") String sort`
   - Remove: `@RequestParam(defaultValue = "asc") String direction`

2. **Usa `@PageableDefault` do Spring**
   ```java
   @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
   ```

3. **Remove lógica manual de parsing**
   ```java
   // Remove estas linhas:
   Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
           ? Sort.Direction.DESC
           : Sort.Direction.ASC;
   Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
   ```

4. **Simplifica o método**
   - Passa `Pageable` diretamente ao service
   - Spring data binding cuida dos detalhes

### Vantagens

1. **Consistência com Institution**
2. **Menos código duplicado**
3. **Spring cuida da validação**
4. **URLs idênticas**

### Exemplo de Uso

Antes:
```bash
GET /value-chains?page=0&size=10&sort=name&direction=asc&active=true
```

Depois (ambos funcionam):
```bash
GET /value-chains?page=0&size=10&sort=name,asc&active=true
# ou
GET /value-chains?active=true
# (usa defaults: page=0, size=10, sort=name, asc)
```

---

## RESUMO DAS CORREÇÕES

| Correção | Arquivo | Tipo | Esforço | Impacto |
|----------|---------|------|---------|---------|
| #1: TenantContext | ValueChainService.java | Semântica | 5 min | Médio (melhor error handling) |
| #2: UpdateValueChainDTO | UpdateValueChainDTO.java | Validação | 10 min | Baixo (clareza) |
| #3: Paginação | ValueChainController.java | Padronização | 15 min | Muito Baixo (consistency) |

**Tempo Total de Correção**: ~30 minutos

**Nível de Risco**: Muito Baixo - todas as mudanças são retrocompatíveis

---

## COMO APLICAR AS CORREÇÕES

### Passo 1: Correção #1
```bash
# Editar ValueChainService.java
# Linha 60-66: Trocar exceção e adicionar logging

# Teste rápido:
# Remover TenantContext manualmente e testar se UnauthorizedAccessException é lançada
```

### Passo 2: Correção #2
```bash
# Editar UpdateValueChainDTO.java
# Linha 22: Adicionar @NotBlank

# Teste rápido:
# PUT /value-chains/{id} com name=""
# Deve falhar com mensagem clara
```

### Passo 3: Correção #3
```bash
# Editar ValueChainController.java
# Linhas 54-71: Simplificar com @PageableDefault

# Teste rápido:
# GET /value-chains
# GET /value-chains?page=0&size=5&sort=name,desc
# Ambos devem funcionar igual a Institution
```

---

## PRÓXIMAS FASES

Após aplicar estas correções:

1. **Testes Unitários**: Validar exceptions e validações
2. **Testes de Integração**: Validar endpoints REST
3. **Testes E2E**: Validar fluxo completo multi-tenant
4. **Code Review Final**: Aprovar para merge
5. **Merge para main**: Deploy
6. **Trilha 4**: Implementar Frontend ValueChain

---

**Preparado por**: Engenheiro Senior
**Data**: 2026-01-27
**Versão**: 1.0
