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
        // LOCK #2: MTP crea spesso DATI_CIFS (1).json; il token JSON decide la validità.
        assertTrue(SetupCifsBundle.isBundleFileName("DATI_CIFS (1).json"))
        assertTrue(SetupCifsBundle.isBundleFileName("DATI_CIFS (2).json"))
    }

    @Test
    fun rejectsOtherNumberedDuplicates() {
        assertFalse(SetupCifsBundle.isBundleFileName("MediaManager_cifs_bundle (1).json"))
        assertFalse(SetupCifsBundle.isBundleFileName("no_pw (1).json"))
    }
}
