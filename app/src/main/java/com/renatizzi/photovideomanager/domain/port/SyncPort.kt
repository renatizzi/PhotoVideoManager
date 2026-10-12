package com.renatizzi.photovideomanager.domain.port

/**
 * Estensione architetturale futura (BL-16 — evoluzione Archivio Condiviso / Catalogo multi-installazione).
 *
 * v1: sempre disabilitata ([DisabledSyncPort]). Nessuna sync, coda o retry automatici.
 * Quando consolidata la specifica: metadati Catalogo (opt-in) su area personale cloud;
 * file fisici restano sulle fonti salvo Importa/Backup; AuthPort separato.
 */
interface SyncPort {
    val isEnabled: Boolean
        get() = false
}
