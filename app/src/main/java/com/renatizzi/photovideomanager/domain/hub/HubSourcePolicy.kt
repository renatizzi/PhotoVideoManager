package com.renatizzi.photovideomanager.domain.hub

import com.renatizzi.photovideomanager.domain.model.MediaCopyState

/**
 * Cosa di una sorgente può viaggiare nel fascicolo e cosa resta su questo apparecchio.
 * Un documentId uguale non collega la cartella: serve un nuovo permesso e una conferma.
 */
object HubSourcePolicy {
    fun shareable(provider: String): Boolean =
        provider == "GOOGLE_DRIVE" || provider == "NETWORK_NAS" || provider == "OTHER"

    fun linksWithoutExplicitConfirm(): Boolean = false
}

/**
 * Cestino della scheda locale. Senza copie si usa il timbro dell'ultimo allineamento,
 * così una scheda recepita come cestinata non risulta di nuovo da pubblicare.
 */
object HubLocalCard {
    fun trashed(copyStates: List<MediaCopyState>, stamp: HubDiffEngine.SyncStamp?): Boolean =
        if (copyStates.isEmpty()) {
            stamp?.trashed == true
        } else {
            copyStates.all { it == MediaCopyState.TRASHED }
        }
}

/**
 * La scelta sul cestino cambia solo lo stato della copia. Non cancella il file.
 */
object HubCardApply {
    fun copyStateAfterRemote(remoteTrashed: Boolean, current: MediaCopyState): MediaCopyState = when {
        remoteTrashed -> MediaCopyState.TRASHED
        current == MediaCopyState.TRASHED -> MediaCopyState.ACTIVE
        else -> current
    }
}
