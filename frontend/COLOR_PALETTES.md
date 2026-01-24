# Paletas de Cores - Guia Visual

## Como Alternar Entre Paletas

Edite `/frontend/src/plugins/vuetify.ts` na linha:
```typescript
defaultTheme: 'modernTheme', // Altere aqui
```

---

## 🎨 Tema Moderno (Recomendado) - `modernTheme`

### Paleta de Cores

```
┌─────────────────────────────────────────────────────────┐
│ Primary (#64748B)    ████████████ Slate Blue            │
│ Secondary (#71717A)  ████████████ Zinc Gray             │
│ Accent (#A78BFA)     ████████████ Soft Violet           │
│ Error (#F87171)      ████████████ Coral Red             │
│ Info (#38BDF8)       ████████████ Sky Blue              │
│ Success (#4ADE80)    ████████████ Mint Green            │
│ Warning (#FBBF24)    ████████████ Soft Amber            │
│ Background (#F8FAFC) ████████████ Pale Slate            │
│ Surface (#FFFFFF)    ████████████ Pure White            │
└─────────────────────────────────────────────────────────┘
```

### Quando Usar
- ✅ Aplicações corporativas e profissionais
- ✅ Dashboards administrativos
- ✅ Sistemas de gestão (ERP, CRM, etc)
- ✅ Plataformas B2B
- ✅ Interfaces longas (muito tempo de uso)

### Vantagens
- Reduz fadiga visual
- Aparência mais sofisticada e moderna
- Melhor para ambientes profissionais
- Segue tendências de design 2024-2026
- Cores harmônicas e equilibradas

---

## 🌈 Tema Original - `originalTheme`

### Paleta de Cores

```
┌─────────────────────────────────────────────────────────┐
│ Primary (#1976D2)    ████████████ Material Blue         │
│ Secondary (#424242)  ████████████ Dark Gray             │
│ Accent (#82B1FF)     ████████████ Light Blue            │
│ Error (#FF5252)      ████████████ Bright Red            │
│ Info (#2196F3)       ████████████ Info Blue             │
│ Success (#4CAF50)    ████████████ Bright Green          │
│ Warning (#FFC107)    ████████████ Amber Yellow          │
│ Background (#F5F5F5) ████████████ Light Gray            │
│ Surface (#FFFFFF)    ████████████ Pure White            │
└─────────────────────────────────────────────────────────┘
```

### Quando Usar
- ✅ Aplicações que precisam chamar atenção
- ✅ Produtos B2C (consumidor final)
- ✅ Marketing e vendas
- ✅ Aplicações voltadas para público jovem
- ✅ Dashboards com foco em alertas

### Vantagens
- Cores mais vivas e energéticas
- Maior contraste e visibilidade
- Padrão Material Design clássico
- Familiares para usuários Android

---

## 📊 Comparação Lado a Lado

| Aspecto | Tema Moderno | Tema Original |
|---------|--------------|---------------|
| **Saturação** | Baixa (~40%) | Alta (~80%) |
| **Vibração** | Suave e pastel | Vibrante e forte |
| **Contraste** | Médio | Alto |
| **Fadiga Visual** | Baixa | Média-Alta |
| **Profissionalismo** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| **Impacto Visual** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Modernidade** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |

---

## 🎯 Recomendação

Para o **Simplifica** (sistema de gestão multi-tenant corporativo):

👉 **Use `modernTheme`**

**Motivo**: O sistema será usado por longas horas por administradores e gestores. Cores suaves reduzem fadiga, transmitem profissionalismo e seguem as tendências modernas de UI/UX (2024-2026).

---

## 🔧 Customização Avançada

Se quiser criar sua própria paleta, edite `vuetify.ts`:

```typescript
const customTheme = {
  dark: false,
  colors: {
    primary: '#SUA_COR_AQUI',
    secondary: '#SUA_COR_AQUI',
    accent: '#SUA_COR_AQUI',
    error: '#SUA_COR_AQUI',
    info: '#SUA_COR_AQUI',
    success: '#SUA_COR_AQUI',
    warning: '#SUA_COR_AQUI',
    background: '#SUA_COR_AQUI',
    surface: '#FFFFFF',
  },
}
```

### Ferramentas Recomendadas
- [Coolors.co](https://coolors.co/) - Gerador de paletas
- [Tailwind Colors](https://tailwindcss.com/docs/customizing-colors) - Referência
- [Material Design Color Tool](https://material.io/resources/color/) - Validador

---

## 📝 Notas

- O tema **não afeta** cores definidas manualmente nos componentes
- Para aplicar globalmente, use as cores do tema: `color="primary"` ao invés de `color="#1976D2"`
- As cores são aplicadas automaticamente em chips, botões, alertas, etc.
