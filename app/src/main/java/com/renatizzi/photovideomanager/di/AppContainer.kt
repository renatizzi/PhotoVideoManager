package com.renatizzi.photovideomanager.di

import android.content.Context
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.application.CensusService
import com.renatizzi.photovideomanager.application.SourceRegistry
import com.renatizzi.photovideomanager.data.catalog.CatalogDatabase
import com.renatizzi.photovideomanager.data.catalog.RoomCatalogStore
import com.renatizzi.photovideomanager.data.storage.LocalFilesystemStorageAdapter
import com.renatizzi.photovideomanager.data.storage.StorageAdapterFactory
import com.renatizzi.photovideomanager.domain.policy.DisabledSyncPort
import com.renatizzi.photovideomanager.domain.policy.LocalOwnerPermissionGate
import com.renatizzi.photovideomanager.domain.policy.LocalTrustAuthPort
import com.renatizzi.photovideomanager.domain.port.AuthPort
import com.renatizzi.photovideomanager.domain.port.CatalogStore
import com.renatizzi.photovideomanager.domain.port.PermissionGate
import com.renatizzi.photovideomanager.domain.port.StorageAdapter
import com.renatizzi.photovideomanager.domain.port.SyncPort
import java.io.File

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val personalRoot = File(appContext.filesDir, "personal_archive")

    val database: CatalogDatabase = CatalogDatabase.create(appContext)
    val catalogStore: CatalogStore = RoomCatalogStore(database)
    val adapterFactory: StorageAdapterFactory = StorageAdapterFactory(
        context = appContext,
        personalRoot = personalRoot,
    )
    val localStorage: StorageAdapter = LocalFilesystemStorageAdapter(personalRoot)
    val permissionGate: PermissionGate = LocalOwnerPermissionGate()
    val authPort: AuthPort = LocalTrustAuthPort()
    val syncPort: SyncPort = DisabledSyncPort()

    val sourceRegistry: SourceRegistry = SourceRegistry(
        appContext = appContext,
        catalogStore = catalogStore,
        adapterFactory = adapterFactory,
        permissionGate = permissionGate,
    )

    val censusService: CensusService = CensusService(
        catalogStore = catalogStore,
        adapterFactory = adapterFactory,
        permissionGate = permissionGate,
    )

    val catalogFacade: CatalogFacade = CatalogFacade(
        sourceRegistry = sourceRegistry,
        censusService = censusService,
    )
}
