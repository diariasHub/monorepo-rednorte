## ⚠️ IMPORTANTE - CONFIGURACIÓN CENTRALIZADA

Este microservicio **NO debe ejecutarse de forma independiente** con su propio `docker-compose.yml`.

### ✅ Forma correcta de ejecutar:

1. Navega a: `SPS-RedNorte-Infraestructura/`
2. Ejecuta: `docker-compose up -d`
3. El ms-ficha-clinica se levantará automáticamente en puerto **8083** (externo) → **8001** (interno)

### 📋 Configuración Centralizada

- **Base de datos**: PostgreSQL compartida (`rednorte-postgres`)
- **Servidor FHIR**: HAPI FHIR (`rednorte-hapi-fhir`)
- **Variables de entorno**: Definidas en `SPS-RedNorte-Infraestructura/.env`

### ❌ NO HACER:

```bash
# ❌ INCORRECTO - No uses el docker-compose local
cd SPS-ms-ficha-clinica
docker-compose up -d
```

### ✅ HACER:

```bash
# ✅ CORRECTO - Usa el docker-compose centralizado
cd SPS-RedNorte-Infraestructura
docker-compose up -d
```

### 📁 Archivos Importantes

- `docker-compose.yml` - **NO USAR** (mantener solo para referencia local de desarrollo)
- `application.properties` - Configurado para usar variables de entorno
- `db/seed_data.sql` - Datos de ejemplo (migrados a `SPS-RedNorte-Infraestructura/db/seed_data.sql`)

---

Más información en: `SPS-RedNorte-Infraestructura/README.md`
