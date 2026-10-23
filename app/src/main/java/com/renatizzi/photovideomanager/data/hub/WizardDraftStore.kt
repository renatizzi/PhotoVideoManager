package com.renatizzi.photovideomanager.data.hub

import android.content.Context

/**
 * Persistenza bozza Setup Catalogo. Nessuna password.
 */
class WizardDraftStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    data class Draft(
        val stepName: String,
        val selectedKindName: String,
        val folderPath: String,
        val shareName: String,
        val userName: String,
        val scriptPrepared: Boolean,
        val usbCopiedDeclared: Boolean,
        val scriptRanDeclared: Boolean,
        val setupMode: String,
        val setupToken: String = "",
        val pcHostIp: String = "",
        val cifsUserQualified: String = "",
    )

    fun load(): Draft? {
        if (!prefs.getBoolean(KEY_ACTIVE, false)) return null
        val stepName = prefs.getString(KEY_STEP, null)?.takeIf { it.isNotBlank() } ?: return null
        return Draft(
            stepName = stepName,
            selectedKindName = prefs.getString(KEY_KIND, "PC_NOTEBOOK") ?: "PC_NOTEBOOK",
            folderPath = prefs.getString(KEY_FOLDER, WindowsCatalogSetupScript.DEFAULT_FOLDER)
                ?: WindowsCatalogSetupScript.DEFAULT_FOLDER,
            shareName = prefs.getString(KEY_SHARE, WindowsCatalogSetupScript.DEFAULT_SHARE)
                ?: WindowsCatalogSetupScript.DEFAULT_SHARE,
            userName = prefs.getString(KEY_USER, WindowsCatalogSetupScript.DEFAULT_USER)
                ?: WindowsCatalogSetupScript.DEFAULT_USER,
            scriptPrepared = prefs.getBoolean(KEY_SCRIPT_PREPARED, false),
            usbCopiedDeclared = prefs.getBoolean(KEY_USB_COPIED, false),
            scriptRanDeclared = prefs.getBoolean(KEY_SCRIPT_RAN, false),
            setupMode = prefs.getString(KEY_MODE, MODE_CREATE) ?: MODE_CREATE,
            setupToken = prefs.getString(KEY_SETUP_TOKEN, "").orEmpty(),
            pcHostIp = prefs.getString(KEY_PC_HOST_IP, "").orEmpty(),
            cifsUserQualified = prefs.getString(KEY_CIFS_USER, "").orEmpty(),
        )
    }

    fun save(draft: Draft) {
        prefs.edit()
            .putBoolean(KEY_ACTIVE, true)
            .putString(KEY_STEP, draft.stepName)
            .putString(KEY_KIND, draft.selectedKindName)
            .putString(KEY_FOLDER, draft.folderPath)
            .putString(KEY_SHARE, draft.shareName)
            .putString(KEY_USER, draft.userName)
            .putBoolean(KEY_SCRIPT_PREPARED, draft.scriptPrepared)
            .putBoolean(KEY_USB_COPIED, draft.usbCopiedDeclared)
            .putBoolean(KEY_SCRIPT_RAN, draft.scriptRanDeclared)
            .putString(KEY_MODE, draft.setupMode)
            .putString(KEY_SETUP_TOKEN, draft.setupToken)
            .putString(KEY_PC_HOST_IP, draft.pcHostIp)
            .putString(KEY_CIFS_USER, draft.cifsUserQualified)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        const val MODE_CREATE = "CREATE"
        const val MODE_MODIFY = "MODIFY"

        private const val PREFS = "pvm_wizard_draft"
        private const val KEY_ACTIVE = "active"
        private const val KEY_STEP = "step"
        private const val KEY_KIND = "kind"
        private const val KEY_FOLDER = "folder"
        private const val KEY_SHARE = "share"
        private const val KEY_USER = "user"
        private const val KEY_SCRIPT_PREPARED = "script_prepared"
        private const val KEY_USB_COPIED = "usb_copied"
        private const val KEY_SCRIPT_RAN = "script_ran"
        private const val KEY_MODE = "mode"
        private const val KEY_SETUP_TOKEN = "setup_token"
        private const val KEY_PC_HOST_IP = "pc_host_ip"
        private const val KEY_CIFS_USER = "cifs_user_qualified"
    }
}
