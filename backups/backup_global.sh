#!/bin/bash
# Script de Backup Global Diario - PostgreSQL

# Configurar variables
DB_NAME="erp_db"
DB_USER="erp"
export PGPASSWORD="Abril2026+++"
BACKUP_DIR="/backups"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/global_${DATE}.dump"

echo "=== Iniciando Backup Global Diario ==="
echo "Fecha y hora de inicio: $(date)"
echo "Base de datos: ${DB_NAME}"
echo "Destino: ${BACKUP_FILE}"

# Crear directorio si no existe (por si acaso)
mkdir -p "${BACKUP_DIR}"

# Ejecutar pg_dump con formato Custom (-F c) que es comprimido nativamente y estructurado
pg_dump -h localhost -U "${DB_USER}" -d "${DB_NAME}" -F c -b -v -f "${BACKUP_FILE}"

# Verificar el resultado de la operación
if [ $? -eq 0 ]; then
    SIZE_BYTES=$(stat -c%s "${BACKUP_FILE}")
    echo "=== Backup Completado Exitosamente ==="
    echo "Archivo: ${BACKUP_FILE}"
    echo "Tamaño del archivo: ${SIZE_BYTES} bytes"
else
    echo "=== ERROR: El backup ha fallado ===" >&2
    exit 1
fi

# Limpieza de archivos antiguos (más de 7 días)
echo "Ejecutando limpieza de archivos antiguos (más de 7 días) en ${BACKUP_DIR}..."
find "${BACKUP_DIR}" -name "global_*.dump" -type f -mtime +7 -print -delete

echo "=== Fin del proceso ==="
