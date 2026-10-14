package com.renatizzi.photovideomanager.domain.port

/**
 * Porta astratta per autenticazione verso risorse (es. SMB).
 * Nella v1 local-first è no-op / LocalTrust; sostituibile senza rifattorizzare il Domain.
 */
interface AuthPort {
    suspend fun ensureAuthenticated(resourceId: String): AuthResult
}

sealed interface AuthResult {
    data object Trusted : AuthResult
    data class ChallengeRequired(val reason: String) : AuthResult
    data class Denied(val reason: String) : AuthResult
}
