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
    fun rejectsNumberedDuplicates() {
        assertFalse(SetupCifsBundle.isBundleFileName("DATI_CIFS (1).json"))
        assertFalse(SetupCifsBundle.isBundleFileName("DATI_CIFS (2).json"))
        assertFalse(SetupCifsBundle.isBundleFileName("MediaManager_cifs_bundle (1).json"))
    }
}
