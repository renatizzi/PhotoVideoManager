package com.renatizzi.photovideomanager.domain.policy

import com.renatizzi.photovideomanager.domain.port.AuthPort
import com.renatizzi.photovideomanager.domain.port.AuthResult

/** Policy local-first: nessuna autenticazione obbligatoria. */
class LocalTrustAuthPort : AuthPort {
    override suspend fun ensureAuthenticated(resourceId: String): AuthResult = AuthResult.Trusted
}
