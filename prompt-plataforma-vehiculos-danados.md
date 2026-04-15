# PROMPT INICIAL - PLATAFORMA DE VENTA DE VEHÍCULOS DAÑADOS

## VISIÓN GENERAL
Crear una plataforma web B2B/B2C para la venta de:
- Vehículos dañados (damaged cars)
- Vehículos para desguace/desmantelamiento (salvage cars)
- Partes usadas de vehículos (used car parts) - estructura solo, sin implementar
- Vehículos de ocasión (occasion vehicles) - estructura solo, sin implementar

**Fase inicial:** Solo turismo, vehículos comerciales y motos. Modelo de negocio: múltiples vendedores/concesionarios usando la plataforma.

---

## 1. ARQUITECTURA GENERAL

### Stack recomendado:
- **Frontend:** React/Vue.js con TypeScript
- **Backend:** Node.js/Express, Python/Django, o similar
- **Base de datos:** PostgreSQL para datos relacionales, Redis para caché
- **Búsqueda:** Elasticsearch o similar para búsqueda avanzada
- **Almacenamiento:** Cloud storage (AWS S3, Google Cloud Storage) para imágenes
- **Hosting:** Docker + Kubernetes para escalabilidad

---

## 2. ESTRUCTURA DE NAVEGACIÓN PRINCIPAL

### Header/Navbar:
- Logo con link a home
- Menú principal:
  - Home
  - Damaged cars (Vehículos dañados)
  - Salvage cars (Desguace)
  - Used car parts (Partes usadas) - menú visible pero no funcional
  - Occasion passenger cars (Vehículos ocasión) - menú visible pero no funcional
  - Dealers (Link a sección de concesionarios)
- Selector de idiomas (mínimo: EN, ES, NL, DE, FR) - con banderas
- Iconos secundarios:
  - Carrito de compras (para partes)
  - Parking (gestión de vehículos guardados)

### Footer:
- Sección "Find a dealer" con buscador
- Links legales (términos, privacidad, disclaimer)
- Enlaces a servicios principales
- Info de contacto

---

## 3. PÁGINA DE LISTADO DE VEHÍCULOS

### Ruta: `/en/search/damaged/[tipo-vehiculo]+[marca]/[pagina]`

### Componentes principales:

#### A) Filtros laterales:
- **Marca:** Dropdown con 150+ marcas (Hyundai, BMW, Mercedes, etc.)
- **Modelo:** Dropdown dinámico que se actualiza según marca seleccionada
- **Año de matriculación (ERD):** 
  - Rango desde 1996 hasta 2025
  - Dos selects: año desde/hasta
- **Tipo de combustible:** 
  - Todos los tipos
  - Gasolina, Diésel, Híbrido, Eléctrico, Gas, GLP, Hidrógeno, CNG, Híbrido bencina-GNC
- **Transmisión:**
  - Todos
  - Manual
  - Automática
- **Rango de precio:** Slider dual (€450 - €33.000)
- **Botón "Search"** para aplicar filtros
- **Link "Advanced search"** para búsqueda avanzada

#### B) Opciones de ordenamiento (arriba del listado):
- Ordenar por fecha (descendente)
- Ordenar por marca + modelo
- Ordenar por precio

#### C) Breadcrumb:
- Home > Damaged cars > Marca > [Filtro aplicado]

#### D) Grid de vehículos (respuesta):
- Cada tarjeta contiene:
  - Imagen principal del vehículo
  - Título: "Marca Model"
  - Descripción: especificaciones (ej: "73kWh 160kW Connect+")
  - Precio en EUR (€)
  - Indicador "export price" si aplica
  - Icono + año de matriculación (ERD)
  - Icono + tipo de combustible
  - Icono + kilometraje
  - Click en tarjeta = ir a detalle

#### E) Sección lateral "Last viewed":
- Últimos 5 vehículos visitados
- Con miniaturas y precios

#### F) Link a partes:
- "Parts stock" - enlaza a búsqueda de partes por modelo

---

## 4. PÁGINA DE DETALLE DE VEHÍCULO

### Ruta: `/en/damaged/[tipo-vehiculo]/[marca]-[modelo]/o/[id]`

### Secciones:

#### A) Galería de imágenes:
- Mínimo 20-25 imágenes por vehículo
- Visor con navegación (anterior/siguiente)
- Thumbnails abajo para saltar entre imágenes
- Soporte para zoom

#### B) Información principal (tabla de datos):
- Número interno de vehículo
- Marca
- Modelo
- Tipo de combustible
- Potencia (en kW)
- Transmisión
- Origen (país)
- Año de matriculación (ERD)
- Kilometraje
- Precio neto de exportación (sin IVA)
- IVA/Margen
- Número de identificación del vehículo (VIN)
- Color
- Número de puertas

#### C) Opciones del vehículo (agrupadas por categoría):
- Entertainment (Ej: Navigation system)
- Sales (Ej: Complete instruction manual)
- Safety (Ej: ABS, ESP)
- Otras características

#### D) Información del concesionario (dealer):
- Nombre del concesionario con link a perfil
- Dirección completa
- Teléfono (con link WhatsApp pre-rellenado)
- Email
- Sitio web
- Número VAT

#### E) Acciones:
- Botón "Direct contact" (teléfono)
- Link a desguace si existe
- "Parts stock" para este modelo
- Botón "Parking" (guardar vehículo)

#### F) Opciones de compartir:
- Facebook
- Twitter
- Email
- Link directo
- Descarga de ficha técnica (PDF)

#### G) Vehículos relacionados:
- Otros vehículos del mismo modelo
- 4-5 items mostrados
- Con precios y características

---

## 5. PÁGINA DE DESGUACE (SALVAGE)

### Ruta: `/en/salvage-car`

**Estructura:**
- Similar a listado de dañados
- Muestra vehículos para desmantelar
- Filtros idénticos
- Últimos modelos añadidos destacados
- Link "Go to disassembly" en ficha de vehículos dañados

---

## 6. MÓDULO DE DEALERS

### Funcionalidades principales:
- **Perfil de dealer:**
  - Información completa
  - Logo/bandera
  - Vehículos en venta
  - Datos de contacto
  - Ubicación en mapa
  - Enlace a sitio web

- **Gestión de inventario (área privada):**
  - Login para dealers
  - Dashboard con vehículos activos
  - Formulario para añadir nuevos vehículos:
    - Datos básicos (marca, modelo, año, etc.)
    - Especificaciones técnicas
    - Precio (con y sin IVA)
    - Carga de galería (múltiples imágenes)
    - Estado del vehículo (descripción de daños)
    - Opciones disponibles
  - Estadísticas de vistas y contactos
  - Herramienta de modificación/eliminación de anuncios

---

## 7. BÚSQUEDA AVANZADA

- Búsqueda por criterios múltiples
- Filtros adicionales (condición, localización, etc.)
- Guardado de búsquedas favoritas
- Alertas de nuevos vehículos según criterios

---

## 8. GESTIÓN DE VISTAS Y CONTACTO

### "Last viewed" section:
- Guardado en localStorage/cookie
- Máximo 5-10 vehículos
- Actualización automática al visitar detalle

### Sistema de contacto:
- Formulario de consulta (nombre, email, teléfono, mensaje)
- Links de WhatsApp pre-rellenados
- Teléfono directo
- Email del dealer
- Integración con sistema de tickets (para seguimiento)

---

## 9. MULTIIDIOMA

**Idiomas soportados (mínimo):**
- Inglés (en)
- Español (es)
- Neerlandés (nl)
- Alemán (de)
- Francés (fr)

**Implementación:**
- URL con prefijo de idioma: `/en/`, `/es/`, `/nl/`, etc.
- Selector visual con banderas
- Traducción de:
  - Interfaz completa
  - Nombres de marcas/modelos
  - Filtros y etiquetas
  - URLs amigables por idioma

---

## 10. FUNCIONALIDADES TÉCNICAS

### SEO:
- URLs amigables y canónicas
- Meta tags dinámicos
- Sitemap XML
- Schema.org para vehículos

### Rendimiento:
- Caché de búsquedas
- Lazy loading de imágenes
- CDN para imágenes
- Compresión de datos

### Seguridad:
- HTTPS obligatorio
- Validación de entrada
- Rate limiting en búsquedas
- Protección CSRF
- Sanitización de datos

### Responsividad:
- Diseño mobile-first
- Breakpoints: 320px, 768px, 1024px, 1440px
- Touch-friendly en móvil

---

## 11. BASE DE DATOS - ESTRUCTURAS PRINCIPALES
```sql
VEHICLES:
- id (PK)
- dealer_id (FK)
- brand_id (FK)
- model_id (FK)
- year
- fuel_type
- transmission
- power_kw
- mileage_km
- price_net_eur
- price_with_vat_eur
- vin
- color
- doors
- description
- damage_description
- status (active/sold/removed)
- created_at
- updated_at
- origin_country

DEALERS:
- id (PK)
- company_name
- address
- city
- country
- phone
- email
- website
- vat_number
- logo_url
- created_at

IMAGES:
- id (PK)
- vehicle_id (FK)
- image_url
- order
- created_at

VEHICLE_OPTIONS:
- id (PK)
- vehicle_id (FK)
- option_name
- category (entertainment/safety/sales/other)

SEARCHES:
- id (PK)
- user_id
- filters (JSON)
- created_at
- updated_at
```

---

## 12. FUNCIONALIDADES NO IMPLEMENTADAS (FASE INICIAL)

Menú visible pero sin función:
- **Partes usadas (Used car parts)** - estructura de URL lista, pero búsqueda no funcional
- **Vehículos de ocasión (Occasion passenger cars)** - estructura lista, búsqueda no funcional

---

## 13. INTEGRACIONES FUTURAS

- Pasarela de pago
- Sistema de subastas
- Integración con transportistas
- API para terceros
- App móvil
- Sistema de garantías

---

## RESUMEN DE PÁGINAS CLAVE

| Página | Ruta | Descripción |
|--------|------|-------------|
| Home | `/en` | Página principal |
| Damaged Cars | `/en/damaged-car` | Listado de vehículos dañados |
| Search Results | `/en/search/damaged/[tipo]+[marca]/[pag]` | Búsqueda filtrada |
| Vehicle Detail | `/en/damaged/[tipo]/[marca]-[modelo]/o/[id]` | Detalle de vehículo |
| Salvage Cars | `/en/salvage-car` | Listado de vehículos para desguace |
| Dealers | `/en/dealer/[id]` | Perfil del concesionario |
| Dealer Admin | `/im/admin_klt/` | Panel privado de dealer |
| Parts | `/en/used-car-part` | Partes usadas (no implementado) |
| Occasion | `/en/occasion-passenger-cars` | Vehículos ocasión (no implementado) |

---
