package com.renatizzi.photovideomanager.data.hub

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupCifsBundleNameTest {
    @Test
    fun acceptsCanonicalAndLegacyNames() {
        assertTrue(SetupCifsBundle.isBundleFileName("DATI_CIFS.json"))
        assertTrue(SetupCifsBundle.isBundleFileName("dati_cifs.json"))
        assertTrue(SetupCifsBundle.isBundleFileName("MediaManager_cifs_bundle.json"))
        assertTrue(SetupCifsBundle.isBundleFileName("MediaManager_cifs_bundle_abc.json"))
    }

    @Test
    fun acceptsNumberedDatiCifsCopies_forMtpOverwrite() {
        // MediaStore: DATI_CIFS (n).json — Esplora/MTP Windows: DATI_CIFS.json (n).
        assertTrue(SetupCifsBundle.isBundleFileName("DATI_CIFS (1).json"))
        assertTrue(SetupCifsBundle.isBundleFileName("DATI_CIFS (2).json"))
        assertTrue(SetupCifsBundle.isBundleFileName("DATI_CIFS.json (1)"))
        assertTrue(SetupCifsBundle.isBundleFileName("DATI_CIFS.json (2)"))
    }

    @Test
    fun rejectsOtherNumberedDuplicates() {
        assertFalse(SetupCifsBundle.isBundleFileName("MediaManager_cifs_bundle (1).json"))
        assertFalse(SetupCifsBundle.isBundleFileName("no_pw (1).json"))
    }

    @Test
    fun scrubMatchesOddExtensionsAndBareNames() {
        // Samsung / MTP: a volte senza .json o con tipo BIN.
        assertTrue(SetupCifsBundle.isScrubbableDatiCifsName("DATI_CIFS"))
        assertTrue(SetupCifsBundle.isScrubbableDatiCifsName("DATI_CIFS.json"))
        assertTrue(SetupCifsBundle.isScrubbableDatiCifsName("DATI_CIFS.json (1)"))
        assertTrue(SetupCifsBundle.isScrubbableDatiCifsName("DATI_CIFS (1).json"))
        assertTrue(SetupCifsBundle.isScrubbableDatiCifsName("DATI_CIFS.bin"))
        assertTrue(SetupCifsBundle.isScrubbableDatiCifsName("MediaManager_cifs_bundle_x.json"))
        assertFalse(SetupCifsBundle.isScrubbableDatiCifsName("no_pw.json"))
        assertFalse(SetupCifsBundle.isScrubbableDatiCifsName("CONFIGURA.cmd"))
    }
}
