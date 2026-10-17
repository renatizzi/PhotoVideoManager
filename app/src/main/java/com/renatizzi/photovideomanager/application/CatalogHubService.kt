package com.renatizzi.photovideomanager.application

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import com.renatizzi.photovideomanager.data.hub.CatalogSeatStore
import com.renatizzi.photovideomanager.data.hub.HubPackageIo
import com.renatizzi.photovideomanager.data.hub.HubReadOutcome
import com.renatizzi.photovideomanager.data.storage.SafPathLabels
import com.renatizzi.photovideomanager.domain.hub.CatalogIdentityRules
import com.renatizzi.photovideomanager.domain.hub.CatalogSeat
import com.renatizzi.photovideomanager.domain.hub.ConflictResolution
import com.renatizzi.photovideomanager.domain.hub.HubApplyPlan
import com.renatizzi.photovideomanager.domain.hub.HubCard
import com.renatizzi.photovideomanager.domain.hub.HubCardApply
import com.renatizzi.photovideomanager.domain.hub.HubDiffEngine
import com.renatizzi.photovideomanager.domain.hub.HubDiffPreview
import com.renatizzi.photovideomanager.domain.hub.HubLocalCard
import com.renatizzi.photovideomanager.domain.hub.HubPackage
import com.renatizzi.photovideomanager.domain.hub.HubSourceDescriptor
import com.renatizzi.photovideomanager.domain.hub.HubSourcePolicy
import com.renatizzi.photovideomanager.domain.hub.IdentityAction
import com.renatizzi.photovideomanager.domain.hub.IdentityAssessment
import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.model.MediaItem
import com.renatizzi.photovideomanager.domain.model.MediaKind
import com.renatizzi.photovideomanager.domain.model.StorageAdapterKind
import com.renatizzi.photovideomanager.domain.model.StorageLocation
import com.renatizzi.photovideomanager.domain.port.CatalogStore
import com.renatizzi.photovideomanager.domain.port.PermissionGate
import java.util.UUID

class CatalogHubService(
    private val appContext: Context,
    private val catalogStore: CatalogStore,
    private val seatStore: CatalogSeatStore,
    private val packageIo: HubPackageIo,
    private val permissionGate: PermissionGate,
) {
    fun currentSeat(): CatalogSeat = seatStore.seat()

    fun deviceId(): String = seatStore.deviceId()

    fun adoptedCatalogId(): String? = seatStore.adoptedCatalogId()

    fun hubUnverified(): Boolean = seatStore.isHubUnverified()

    suspend fun inspectSeat(seat: CatalogSeat): Result<IdentityAssessment> = runCatching {
        require(permissionGate.canView(DomainScope.PERSONAL))
        val file = when (val outcome = packageIo.readOutcome(seat)) {
            is HubReadOutcome.Present -> outcome.pkg
            is HubReadOutcome.Missing -> null
            is HubReadOutcome.Invalid -> error(outcome.message)
            is HubReadOutcome.Unreachable -> error(outcome.message)
        }
        CatalogIdentityRules.assess(file, seatStore.adoptedCatalogId(), localCardIds())
    }

    /**
     * Nuovo Catalogo solo se la cartella non contiene già schede o un'identità.
     * Se questo apparecchio ha già un id, lo riusa: non ne nasce un secondo.
     */
    suspend fun configureSeat(seat: CatalogSeat): Result<CatalogSeat> = runCatching {
        require(permissionGate.canMutateCatalog(DomainScope.PERSONAL))
        ensureWritable()
        val toSave = seat.copy(configuredAtEpochMs = System.currentTimeMillis())
        persistTreePermission(toSave)
        val existing = readablePackage(toSave, markUnverified = false)
        if (existing != null && (existing.catalogId != null || existing.cards.isNotEmpty())) {
            error("Nella sede c'è già un Catalogo. Utilizza quello esistente: il fascicolo non è stato modificato.")
        }
        val id = seatStore.adoptedCatalogId() ?: UUID.randomUUID().toString()
        val pkg = HubPackage(
            seatId = toSave.treeUri ?: "local",
            updatedAtEpochMs = System.currentTimeMillis(),
            updatedByDeviceId = deviceId(),
            cards = buildLocalCards(),
            catalogId = id,
            displayLabel = toSave.displayLabel.ifBlank { null },
            sources = buildSourceDescriptors(),
        )
        commitPackage(toSave, pkg)
        seatStore.setAdoptedCatalogId(id)
        seatStore.saveSeat(toSave)
        toSave
    }

    /** Adotta un fascicolo che ha già un catalogId. Non scrive il file. */
    suspend fun adoptDefinedSeat(seat: CatalogSeat): Result<CatalogSeat> = runCatching {
        require(permissionGate.canMutateCatalog(DomainScope.PERSONAL))
        val assessment = inspectSeat(seat).getOrThrow()
        when (assessment.action) {
            IdentityAction.ADOPT_FILE_ID, IdentityAction.SAME_ID -> Unit
            IdentityAction.BLOCK_DIFFERENT_IDS ->
                error("Questo fascicolo ha un'identità diversa da quella già in uso. Non è stato modificato.")
            IdentityAction.NOT_A_CATALOG ->
                error("Nessun Catalogo in questo fascicolo.")
            IdentityAction.NEEDS_CONFIRM_NEW_ID, IdentityAction.NEEDS_CONFIRM_STAMP ->
                error("Il fascicolo non ha ancora un'identità. Serve una conferma esplicita.")
        }
        val fileId = assessment.fileCatalogId ?: error("Identità assente")
        persistTreePermission(seat)
        val saved = seat.copy(configuredAtEpochMs = System.currentTimeMillis())
        seatStore.setAdoptedCatalogId(fileId)
        seatStore.saveSeat(saved)
        saved
    }

    /**
     * Scrive un catalogId su un fascicolo storico che non ne ha uno.
     * [stampExisting] riusa l'id già adottato; altrimenti ne genera uno solo se non ce n'è già uno.
     * Le schede del file restano quelle lette.
     */
    suspend fun assignCatalogIdentity(seat: CatalogSeat, stampExisting: Boolean): Result<String> = runCatching {
        require(permissionGate.canMutateCatalog(DomainScope.PERSONAL))
        ensureWritable()
        val existing = readablePackage(seat, markUnverified = false)
            ?: error("Nessun fascicolo da identificare")
        if (existing.catalogId != null) {
            error("Il fascicolo ha già un'identità. Non viene sostituita.")
        }
        if (existing.cards.isEmpty()) error("Nessun Catalogo in questo fascicolo.")
        val adopted = seatStore.adoptedCatalogId()
        val id = if (stampExisting) {
            adopted ?: error("Nessuna identità già in uso da riportare sul fascicolo.")
        } else {
            if (adopted != null) {
                error("C'è già un'identità su questo apparecchio. Non ne viene creata un'altra.")
            }
            UUID.randomUUID().toString()
        }
        val pkg = existing.copy(
            catalogId = id,
            displayLabel = existing.displayLabel ?: seat.displayLabel.ifBlank { null },
        )
        persistTreePermission(seat)
        commitPackage(seat, pkg)
        val saved = seat.copy(configuredAtEpochMs = System.currentTimeMillis())
        seatStore.setAdoptedCatalogId(id)
        seatStore.saveSeat(saved)
        id
    }

    suspend fun seatReachable(): Boolean {
        val seat = seatStore.seat()
        if (!seat.isConfigured) return false
        return when (val outcome = packageIo.readOutcome(seat)) {
            is HubReadOutcome.Present -> true
            is HubReadOutcome.Invalid -> {
                seatStore.setHubUnverified(true)
                false
            }
            is HubReadOutcome.Missing, is HubReadOutcome.Unreachable -> false
        }
    }

    suspend fun portableSources(): List<HubSourceDescriptor> {
        val seat = seatStore.seat()
        if (!seat.isConfigured) return emptyList()
        return when (val outcome = packageIo.readOutcome(seat)) {
            is HubReadOutcome.Present -> outcome.pkg.sources
            else -> emptyList()
        }
    }

    fun linkedSourceKeys(): Set<String> = seatStore.linkedSourceKeys()

    fun rememberSourceLink(sourceKey: String, locationId: String) {
        seatStore.rememberSourceLink(sourceKey, locationId)
    }

    suspend fun buildPreview(): Result<HubDiffPreview> = runCatching {
        require(permissionGate.canView(DomainScope.PERSONAL))
        ensureNotUnverified()
        val seat = seatStore.seat()
        require(seat.isConfigured) { "Sede non configurata" }
        val remote = readablePackage(seat, markUnverified = true)?.cards.orEmpty()
        HubDiffEngine.preview(buildLocalCards(), remote, seatStore.lastSynced())
    }

    suspend fun applyUpdate(
        preview: HubDiffPreview,
        resolutions: Map<String, ConflictResolution>,
        receiveOnly: Boolean,
    ): Result<HubDiffPreview> = runCatching {
        require(permissionGate.canMutateCatalog(DomainScope.PERSONAL))
        ensureWritable()
        val seat = seatStore.seat()
        require(seat.isConfigured) { "Sede non configurata" }
        val original = readablePackage(seat, markUnverified = true)
            ?: error("Fascicolo assente. L'aggiornamento non ne crea uno nuovo.")
        val effective = if (receiveOnly) {
            resolutions.mapValues { (_, value) ->
                if (value == ConflictResolution.KEEP_LOCAL) ConflictResolution.DEFER else value
            }
        } else {
            resolutions
        }
        val resolved = HubDiffEngine.applyResolutions(preview, effective)
        val now = System.currentTimeMillis()
        val next = HubApplyPlan.nextPackage(
            original = original,
            resolved = resolved,
            now = now,
            deviceId = deviceId(),
            sources = buildSourceDescriptors(),
            displayLabel = seat.displayLabel.ifBlank { null },
            receiveOnly = receiveOnly,
        )
        commitPackage(seat, next)
        for (change in resolved.toReceive) {
            if (resolved.conflicts.any { it.cardId == change.cardId }) continue
            val remote = change.remote ?: continue
            upsertFromRemote(remote)
        }
        seatStore.putLastSynced(
            HubApplyPlan.stampsFor(resolved, seatStore.lastSynced(), receiveOnly),
        )
        resolved
    }

    suspend fun restoreVerifiedBackup(): Result<Unit> = runCatching {
        require(permissionGate.canMutateCatalog(DomainScope.PERSONAL))
        val seat = seatStore.seat()
        require(seat.isConfigured) { "Sede non configurata" }
        val backup = packageIo.verifiedBackupText() ?: error("Nessuna copia verificata da ripristinare")
        val pkg = runCatching { com.renatizzi.photovideomanager.domain.hub.HubPackageCodec.parse(backup) }
            .getOrElse { error("La copia verificata non è leggibile") }
        commitPackage(seat, pkg)
    }

    private suspend fun commitPackage(seat: CatalogSeat, pkg: HubPackage) {
        val report = packageIo.writeVerified(seat, pkg)
        if (!report.verified) {
            if (report.unverified) seatStore.setHubUnverified(true)
            error(report.message)
        }
        seatStore.setHubUnverified(false)
    }

    private fun ensureWritable() {
        if (seatStore.isHubUnverified()) {
            error("Fascicolo non verificato. Ripristina l'ultima copia verificata prima di scrivere di nuovo.")
        }
    }

    private fun ensureNotUnverified() {
        if (seatStore.isHubUnverified()) {
            error("Fascicolo non verificato. Nessun confronto finché non è stato ripristinato o riletto con successo.")
        }
    }

    private suspend fun readablePackage(seat: CatalogSeat, markUnverified: Boolean): HubPackage? =
        when (val outcome = packageIo.readOutcome(seat)) {
            is HubReadOutcome.Present -> outcome.pkg
            is HubReadOutcome.Missing -> null
            is HubReadOutcome.Invalid -> {
                if (markUnverified) seatStore.setHubUnverified(true)
                error(
                    "Fascicolo non leggibile. Non è stato modificato. " +
                        "Se esiste una copia verificata, ripristinala da Aggiorna Catalogo.",
                )
            }
            is HubReadOutcome.Unreachable -> error(outcome.message)
        }

    private fun persistTreePermission(seat: CatalogSeat) {
        if (seat.treeUri.isNullOrBlank()) return
        val uri = Uri.parse(seat.treeUri)
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        runCatching { appContext.contentResolver.takePersistableUriPermission(uri, flags) }
    }

    private suspend fun localCardIds(): Set<String> =
        catalogStore.listAllMediaItems()
            .filter { it.domainScope == DomainScope.PERSONAL }
            .map { it.id }
            .toSet()

    private suspend fun buildLocalCards(): List<HubCard> {
        val items = catalogStore.listAllMediaItems()
            .filter { it.domainScope == DomainScope.PERSONAL }
        val copies = catalogStore.listAllMediaCopies().groupBy { it.mediaItemId }
        val device = deviceId()
        val synced = seatStore.lastSynced()
        return items.map { item ->
            val itemCopies = copies[item.id].orEmpty()
            val stamp = synced[item.id]
            val trashed = HubLocalCard.trashed(itemCopies.map { it.state }, stamp)
            val sha = itemCopies.firstOrNull()?.let { copy ->
                catalogStore.listFingerprintsForCopy(copy.id)
                    .firstOrNull { it.algorithm.equals("SHA-256", ignoreCase = true) }
                    ?.value
            }
            val by = if (stamp != null && stamp.updatedAtEpochMs == item.updatedAtEpochMs) {
                stamp.updatedByDeviceId
            } else {
                device
            }
            HubCard(
                id = item.id,
                kind = item.kind.name,
                displayTitle = item.displayTitle,
                updatedAtEpochMs = item.updatedAtEpochMs,
                updatedByDeviceId = by,
                trashed = trashed,
                sha256 = sha,
            )
        }
    }

    private suspend fun buildSourceDescriptors(): List<HubSourceDescriptor> {
        return catalogStore.listStorageLocations().map { location ->
            val key = seatStore.sourceKeyForLocation(location.id)
                ?: UUID.randomUUID().toString().also { seatStore.rememberSourceKey(location.id, it) }
            val described = describeSource(location)
            HubSourceDescriptor(
                sourceKey = key,
                displayName = location.displayName,
                provider = described.provider,
                humanPath = described.humanPath,
                documentId = described.documentId,
                shareable = described.shareable,
            )
        }
    }

    private fun describeSource(location: StorageLocation): HubSourceDescriptor {
        if (location.adapterKind != StorageAdapterKind.SAF_TREE || location.opaqueLocator.isBlank()) {
            return HubSourceDescriptor(
                sourceKey = "",
                displayName = location.displayName,
                provider = "APP_PRIVATE",
                shareable = false,
            )
        }
        val uri = Uri.parse(location.opaqueLocator)
        val provider = SafPathLabels.providerOf(uri)
        val shareable = HubSourcePolicy.shareable(provider.name)
        val documentId = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull()
        val path = runCatching { SafPathLabels.humanPathOrCloud(appContext, uri) }.getOrDefault("")
        return HubSourceDescriptor(
            sourceKey = "",
            displayName = location.displayName,
            provider = provider.name,
            humanPath = path,
            documentId = documentId,
            shareable = shareable,
        )
    }

    private suspend fun upsertFromRemote(remote: HubCard) {
        val existing = catalogStore.getMediaItem(remote.id)
        val kind = runCatching { MediaKind.valueOf(remote.kind) }.getOrDefault(MediaKind.PHOTO)
        val now = System.currentTimeMillis()
        val item = MediaItem(
            id = remote.id,
            kind = kind,
            domainScope = DomainScope.PERSONAL,
            capturedAtEpochMs = existing?.capturedAtEpochMs,
            displayTitle = remote.displayTitle ?: existing?.displayTitle,
            createdAtEpochMs = existing?.createdAtEpochMs ?: now,
            updatedAtEpochMs = remote.updatedAtEpochMs,
        )
        catalogStore.upsertMediaItem(item)
        val copies = catalogStore.listMediaCopiesForItem(remote.id)
        copies.forEach { copy ->
            val next = HubCardApply.copyStateAfterRemote(remote.trashed, copy.state)
            if (next != copy.state) {
                catalogStore.upsertMediaCopy(copy.copy(state = next))
            }
        }
    }
}
