# Validação Manual do Sistema de Upload

## Pré-requisitos
- Backend rodando na porta 8080
- PostgreSQL configurado e rodando
- Autenticação configurada (usuário ADMIN)

## Cenários de Teste

### 1. Upload de PNG 2MB - Sucesso

#### Criar arquivo de teste
```bash
# Criar uma imagem PNG de teste com ~2MB
convert -size 2000x2000 xc:blue /tmp/test-2mb.png
```

#### Testar upload via curl
```bash
curl -X POST http://localhost:8080/api/institutions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "name=Test Institution" \
  -F "acronym=TEST" \
  -F "type=UNIVERSITY" \
  -F "logo=@/tmp/test-2mb.png" \
  -v
```

#### Resultado esperado
- Status: 201 Created
- Resposta contém:
  - `logoUrl`: URL completa do arquivo original
  - `logoThumbnailUrl`: URL completa do thumbnail
- Arquivos criados em `/tmp/simplifica/uploads/institutions/`:
  - `{uuid}.png` (arquivo original)
  - `thumbnails/{uuid}.png` (thumbnail 150px)

#### Verificar thumbnail
```bash
# Listar arquivos criados
ls -lh /tmp/simplifica/uploads/institutions/
ls -lh /tmp/simplifica/uploads/institutions/thumbnails/

# Verificar dimensões do thumbnail (deve ser 150px na maior dimensão)
identify /tmp/simplifica/uploads/institutions/thumbnails/*.png
```

---

### 2. Upload de arquivo 6MB - Erro "exceeds maximum"

#### Criar arquivo de teste
```bash
# Criar uma imagem PNG com ~6MB
convert -size 3000x3000 xc:red /tmp/test-6mb.png
```

#### Testar upload via curl
```bash
curl -X POST http://localhost:8080/api/institutions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "name=Test Institution 2" \
  -F "acronym=TEST2" \
  -F "type=GOVERNMENT" \
  -F "logo=@/tmp/test-6mb.png" \
  -v
```

#### Resultado esperado
- Status: 400 Bad Request
- Mensagem de erro: "File size exceeds maximum allowed size of 5 MB"
- Nenhum arquivo criado no sistema

---

### 3. Upload de arquivo TXT - Erro "type not allowed"

#### Criar arquivo de teste
```bash
echo "This is a text file" > /tmp/test.txt
```

#### Testar upload via curl
```bash
curl -X POST http://localhost:8080/api/institutions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "name=Test Institution 3" \
  -F "acronym=TEST3" \
  -F "type=PRIVATE" \
  -F "logo=@/tmp/test.txt" \
  -v
```

#### Resultado esperado
- Status: 400 Bad Request
- Mensagem de erro: "File must be an image"
- Nenhum arquivo criado no sistema

---

### 4. Testar atualização de logo (substituição)

#### Upload inicial
```bash
# Criar primeira imagem
convert -size 500x500 xc:green /tmp/logo-v1.png

# Upload
curl -X POST http://localhost:8080/api/institutions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "name=Test Update" \
  -F "acronym=TUPD" \
  -F "type=UNIVERSITY" \
  -F "logo=@/tmp/logo-v1.png" \
  -v
```

Anotar o `id` da instituição criada.

#### Atualizar com nova imagem
```bash
# Criar segunda imagem
convert -size 500x500 xc:yellow /tmp/logo-v2.png

# Atualizar
curl -X PUT http://localhost:8080/api/institutions/{INSTITUTION_ID} \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "logo=@/tmp/logo-v2.png" \
  -v
```

#### Resultado esperado
- Status: 200 OK
- Logo antiga deletada (arquivo e thumbnail removidos)
- Nova logo criada com novo UUID
- Resposta contém novos URLs

---

### 5. Testar acesso público aos arquivos

#### Obter URL do logo
Usando o response do teste 1, copiar a `logoUrl`, exemplo:
```
http://localhost:8080/api/public/uploads/institutions/abc123.png
```

#### Acessar no navegador
```bash
# Arquivo original
curl http://localhost:8080/api/public/uploads/institutions/abc123.png --output test-original.png

# Thumbnail
curl http://localhost:8080/api/public/uploads/institutions/thumbnails/abc123.png --output test-thumb.png
```

#### Resultado esperado
- Status: 200 OK
- Content-Type: image/png
- Arquivo baixado corretamente
- Thumbnail é menor que o original

---

### 6. Testar arquivo inexistente

```bash
curl http://localhost:8080/api/public/uploads/institutions/nonexistent.png -v
```

#### Resultado esperado
- Status: 404 Not Found

---

## Validação de Extensões

### Extensões permitidas (devem funcionar)
```bash
# JPG
curl -X POST http://localhost:8080/api/institutions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "name=Test JPG" \
  -F "acronym=TJPG" \
  -F "type=UNIVERSITY" \
  -F "logo=@test.jpg"

# JPEG
curl ... -F "logo=@test.jpeg"

# PNG
curl ... -F "logo=@test.png"

# GIF
curl ... -F "logo=@test.gif"

# WEBP
curl ... -F "logo=@test.webp"
```

### Extensões não permitidas (devem falhar)
```bash
# BMP (não permitido)
curl -X POST http://localhost:8080/api/institutions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "name=Test BMP" \
  -F "acronym=TBMP" \
  -F "type=UNIVERSITY" \
  -F "logo=@test.bmp"

# Resultado esperado: 400 Bad Request
# Mensagem: "File type not allowed. Allowed types: jpg, jpeg, png, gif, webp"
```

---

## Limpeza

```bash
# Limpar arquivos de teste
rm -rf /tmp/simplifica/uploads/institutions/
rm /tmp/test-*.png
rm /tmp/logo-*.png
rm /tmp/test.txt
```

---

## Checklist de Validação

- [ ] Upload PNG 2MB: ✓ Sucesso, thumbnail criado
- [ ] Upload 6MB: ✓ Exceção "exceeds maximum"
- [ ] Upload TXT: ✓ Exceção "type not allowed"
- [ ] Substituição de logo: ✓ Logo antiga deletada
- [ ] Acesso público original: ✓ Arquivo servido corretamente
- [ ] Acesso público thumbnail: ✓ Thumbnail servido corretamente
- [ ] Arquivo inexistente: ✓ 404 Not Found
- [ ] Extensões permitidas: ✓ JPG, JPEG, PNG, GIF, WEBP
- [ ] Extensões proibidas: ✓ BMP e outras bloqueadas
- [ ] Thumbnail gerado: ✓ 150px máx dimensão, proporção mantida

---

## Notas Importantes

1. **Validação MIME Real**: O sistema valida o tipo MIME real do arquivo, não apenas a extensão.

2. **Limpeza Automática**: Ao atualizar um logo, o anterior é automaticamente deletado.

3. **Diretórios Criados Automaticamente**: O sistema cria os diretórios necessários se não existirem.

4. **UUID Único**: Cada arquivo é salvo com um UUID único para evitar conflitos.

5. **Thumbnail Proporcional**: O thumbnail mantém a proporção original da imagem.

6. **Logs Detalhados**: Todos os logs são registrados com INFO para operações e DEBUG para detalhes.
