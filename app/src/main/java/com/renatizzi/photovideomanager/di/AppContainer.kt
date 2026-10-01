package com.renatizzi.photovideomanager.di

import android.content.Context
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.data.catalog.CatalogDatabase
import com.renatizzi.photovideomanager.data.catalog.RoomCatalogStore
import com.renatizzi.photovideomanager.data.storage.LocalFilesystemStorageAdapter
import com.renatizzi.photovideomanager.domain.policy.DisabledSyncPort
import com.renatizzi.photovideomanager.domain.policy.LocalOwnerPermissionGate
import com.renatizzi.photovideomanager.domain.policy.LocalTrustAuthPort
import com.renatizzi.photovideomanager.domain.port.AuthPort
import com.renatizzi.photovideomanager.domain.port.CatalogStore
import com.renatizzi.photovideomanager.domain.port.PermissionGate
import com.renatizzi.photovideomanager.domain.port.StorageAdapter
import com.renatizzi.photovideomanager.domain.port.SyncPort
import java.io.File

/**
 * Composition root manuale (senza DI framework obbligatorio).
 * Mantiene Domain libero da Android framework salvo i confini Adapter.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: CatalogDatabase = CatalogDatabase.create(appContext)

    val catalogStore: CatalogStore = RoomCatalogStore(database)

    val localStorage: StorageAdapter = LocalFilesystemStorageAdapter(
        rootDirectory = File(appContext.filesDir, "personal_archive"),
    )

    val permissionGate: PermissionGate = LocalOwnerPermissionGate()
    val authPort: AuthPort = LocalTrustAuthPort()
    val syncPort: SyncPort = DisabledSyncPort()

    val catalogFacade: CatalogFacade = CatalogFacade(
        catalogStore = catalogStore,
        localStorage = localStorage,
        permissionGate = permissionGate,
    )
}
