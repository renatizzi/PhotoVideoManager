package com.renatizzi.photovideomanager.data.catalog

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Contratto anti-regressione: i DAO genitori di FK CASCADE non devono usare
 * `@Insert(OnConflictStrategy.REPLACE)`, perché SQLite REPLACE elimina la riga
 * (sparando CASCADE sulle MediaCopy) prima di reinserirla.
 *
 * Sintomo utente: dopo IMPORTA, Home → bootstrap upsert location personale →
 * acquiredPhotoCount/acquiredVideoCount tornano a 0 pur restando i file su disco
 * e i conteggi Catalogo (copie SAF) invariati.
 */
class RoomUpsertCascadeContractTest {
    @Test
    fun catalogDaos_useUpsertNotReplaceInsert() {
        val daoFile = File("src/main/java/com/renatizzi/photovideomanager/data/catalog/CatalogDaos.kt")
        assertTrue("CatalogDaos.kt deve esistere", daoFile.exists())
        val text = daoFile.readText()
        assertTrue("DAO devono usare @Upsert", text.contains("@Upsert"))
        assertFalse(
            "DAO non devono usare @Insert(onConflict = REPLACE) (CASCADE wipe)",
            Regex("""@Insert\s*\(\s*onConflict\s*=\s*OnConflictStrategy\.REPLACE\s*\)""")
                .containsMatchIn(text),
        )
        assertFalse(
            "import OnConflictStrategy non deve restare nei DAO",
            text.contains("import androidx.room.OnConflictStrategy"),
        )
        assertFalse(
            "import Insert non deve restare nei DAO",
            text.contains("import androidx.room.Insert"),
        )
    }

    @Test
    fun mediaCopy_hasCascadeFromStorageLocation() {
        val entityFile = File("src/main/java/com/renatizzi/photovideomanager/data/catalog/CatalogEntities.kt")
        assertTrue(entityFile.exists())
        val text = entityFile.readText()
        assertTrue(text.contains("entity = StorageLocationEntity::class"))
        assertTrue(text.contains("onDelete = ForeignKey.CASCADE"))
    }
}
