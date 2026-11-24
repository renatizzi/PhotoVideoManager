package com.renatizzi.photovideomanager.data.storage

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.Settings
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Etichette leggibili per alberi SAF (evita doc=encoded=… in UI).
 * Distingue storage locale da provider cloud (es. Google Drive).
 */
object SafPathLabels {
    const val ROOT_FOLDER_TITLE = "Tutta la memoria"
    const val DEVICE_GOOGLE_DRIVE = "Google Drive"
    const val DEVICE_DOWNLOADS = "Download"
    /** Share Catalogo / PC Windows tipico (CIFS Documents Provider). */
    const val DEVICE_PC_NOTEBOOK = "PC Windows"
    const val DEVICE_NETWORK_NAS = "Disco di rete / NAS"
    const val DEVICE_CLOUD_OTHER = "Cloud / altro archivio"

    private const val AUTH_EXTERNAL = "com.android.externalstorage.documents"
    private const val AUTH_DOWNLOADS = "com.android.providers.downloads.documents"
    private const val AUTH_DRIVE_PREFIX = "com.google.android.apps.docs"
    private const val AUTH_CIFS_PROVIDER = "com.wa2c.android.cifsdocumentsprovider"

    /**
     * Share Catalogo su PC (nome digitato in Avvio + legacy MyMedia/MediaManagerCatalog).
     * Usato per etichettare «PC Windows» invece di «Disco di rete / NAS».
     * Persistito: altrimenti dopo cold-start MyCatalog torna NAS (LOCK #6/#8).
     */
    private val pcCatalogShareNames: MutableSet<String> =
        mutableSetOf("mymedia", "mediamanagercatalog")
    private var sharePrefs: android.content.SharedPreferences? = null

    /** Collega persistenza + ripristina share noti (chiamare all’avvio app). */
    fun bindPcCatalogShareStore(context: Context) {
        val prefs = context.applicationContext
            .getSharedPreferences(PC_SHARE_PREFS, Context.MODE_PRIVATE)
        sharePrefs = prefs
        val raw = prefs.getString(KEY_PC_SHARES, null).orEmpty()
        if (raw.isBlank()) return
        synchronized(pcCatalogShareNames) {
            raw.split('\u001f').map { it.trim().lowercase() }.filter { it.isNotEmpty() }
                .forEach { pcCatalogShareNames.add(it) }
        }
    }

    /** Registra lo share della sede Catalogo PC (LOCK test telefono #5/#6/#8). */
    fun rememberPcCatalogShare(shareName: String) {
        val n = shareName.trim().lowercase()
        if (n.isEmpty()) return
        val changed = synchronized(pcCatalogShareNames) { pcCatalogShareNames.add(n) }
        if (changed) persistPcCatalogShares()
    }

    /** Dalla tree SAF CIFS: memorizza lo share SMB (es. MyCatalog da smb://host:445/MyCatalog/). */
    fun rememberPcCatalogShareFromTreeUri(treeUri: String?) {
        if (treeUri.isNullOrBlank()) return
        extractSmbShareSegment(treeUri)?.let { rememberPcCatalogShare(it) }
    }

    fun knownPcCatalogShares(): Set<String> =
        synchronized(pcCatalogShareNames) { pcCatalogShareNames.toSet() }

    private fun persistPcCatalogShares() {
        val prefs = sharePrefs ?: return
        val joined = synchronized(pcCatalogShareNames) {
            pcCatalogShareNames.joinToString("\u001f")
        }
        prefs.edit().putString(KEY_PC_SHARES, joined).apply()
    }

    private const val PC_SHARE_PREFS = "pvm_pc_catalog_shares"
    private const val KEY_PC_SHARES = "shares"

    fun deviceLabel(context: Context): String {
        val fromSettings = runCatching {
            Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
        }.getOrNull()?.trim().orEmpty()
        if (fromSettings.isNotBlank()) return fromSettings
        return Build.MODEL.ifBlank { "Questo dispositivo" }
    }

    /** Dispositivo / provider per un albero SAF (telefono vs Drive vs PC/NAS vs altro). */
    fun deviceLabelForTree(context: Context, treeUri: Uri, localDeviceAlias: String): String =
        when (providerOf(treeUri)) {
            SafProvider.LOCAL_STORAGE -> localDeviceAlias.ifBlank { "Questo telefono o tablet" }
            SafProvider.GOOGLE_DRIVE -> DEVICE_GOOGLE_DRIVE
            SafProvider.DOWNLOADS -> DEVICE_DOWNLOADS
            SafProvider.NETWORK_NAS -> networkDeviceLabel(treeUri, displayHint = null)
            SafProvider.OTHER -> DEVICE_CLOUD_OTHER
        }

    /**
     * Distingue PC/notebook (es. share MediaManager) da NAS generico sulla stessa app CIFS.
     */
    fun networkDeviceLabel(treeUri: Uri, displayHint: String?): String =
        networkDeviceLabelFromHints(
            displayHint = displayHint,
            path = humanPath(treeUri).orEmpty(),
            uriString = treeUri.toString(),
        )

    /**
     * Etichetta dispositivo rete per UI (LOCK path): solo il tipo
     * (`Disco di rete / NAS` / `PC Windows`), **senza** `· host`
     * (l’host non va accanto al nome; la cartella sta in `\path`).
     */
    fun networkDeviceLabelFromHints(
        displayHint: String?,
        path: String,
        uriString: String,
    ): String = networkDeviceKindFromHints(displayHint, path, uriString)

    fun networkDeviceKindFromHints(
        displayHint: String?,
        path: String,
        uriString: String,
    ): String {
        val hint = displayHint?.trim().orEmpty()
        val pathTrim = path.trim()
        val blob = listOfNotNull(displayHint, path, uriString)
            .joinToString(" ")
            .lowercase()
        val shares = knownPcCatalogShares()
        val smbShare = extractSmbShareSegment(uriString)?.lowercase()
        // CIFS Documents Provider espone spesso tree UUID opachi (niente smb:// nello Shared URI):
        // il riconoscimento PC vs NAS è solo lato MediaManager (share noto / nome cartella).
        val hintIsKnownShare = shares.any { share ->
            hint.equals(share, ignoreCase = true) || pathTrim.equals(share, ignoreCase = true)
        }
        val isPcCatalogShare = hintIsKnownShare || shares.any { share ->
            smbShare == share ||
                Regex("""\b${Regex.escape(share)}\b""").containsMatchIn(blob) ||
                blob.contains("%2f$share") ||
                blob.contains("/$share")
        }
        return when {
            // Share Catalogo Windows (nome Avvio o legacy) → PC Windows.
            isPcCatalogShare -> DEVICE_PC_NOTEBOOK
            // LOCK S25 #1: disco/share di rete (anche cartella «MediaManager(sda1)») = NAS, non PC.
            blob.contains("synology") ||
                blob.contains("qnap") ||
                blob.contains("truenas") ||
                Regex("""\bnas\b""").containsMatchIn(blob) -> DEVICE_NETWORK_NAS
            // Provider CIFS senza share Catalogo noto: NAS generico (non promuovere a PC).
            blob.contains(AUTH_CIFS_PROVIDER) -> DEVICE_NETWORK_NAS
            else -> DEVICE_NETWORK_NAS
        }
    }

    fun isCifsDocumentsProviderUri(uriString: String?): Boolean {
        if (uriString.isNullOrBlank()) return false
        return runCatching {
            providerOf(Uri.parse(uriString)) == SafProvider.NETWORK_NAS &&
                uriString.contains(AUTH_CIFS_PROVIDER, ignoreCase = true)
        }.getOrDefault(false)
    }

    /** Ultimo segmento path di `smb://host/share` (decodificato). */
    fun extractSmbShareSegment(uriString: String): String? {
        val decoded = decode(uriString)
        val match = Regex("""smb://[^/]+/([^/?#]+)""", RegexOption.IGNORE_CASE)
            .find(decoded)
            ?: return null
        return match.groupValues.getOrNull(1)?.trim()?.trim('/')?.takeIf { it.isNotEmpty() }
    }

    /**
     * Host/share tipico da URI CIFS (`smb://HOST/share`) o dal path, per spezzare i gruppi PC.
     */
    fun extractNetworkHostHint(
        uriString: String,
        displayHint: String? = null,
        path: String = "",
    ): String? {
        val decoded = decode(uriString)
        val smbHost = Regex("""smb://([^/]+)""", RegexOption.IGNORE_CASE)
            .find(decoded)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
            ?.trim('/')
        if (!smbHost.isNullOrBlank() && !looksIllegible(smbHost)) {
            // smb://192.168.68.72:445 → host senza porta (CIFS Documents Provider).
            return smbHost.substringBefore(':').ifBlank { smbHost }
        }
        // Fallback: primo segmento path tipo HOST/share
        val candidate = path.ifBlank { displayHint.orEmpty() }
            .trim()
            .trim('/')
            .substringBefore('/')
            .trim()
        return candidate.takeIf {
            it.isNotBlank() &&
                !it.equals("Cartella di rete", ignoreCase = true) &&
                !looksIllegible(it) &&
                it.length in 2..48
        }
    }

    fun providerOf(treeUri: Uri): SafProvider = providerOfAuthority(treeUri.authority)

    fun providerOfAuthority(authority: String?): SafProvider {
        val auth = authority.orEmpty().lowercase()
        return when {
            auth == AUTH_EXTERNAL -> SafProvider.LOCAL_STORAGE
            auth.startsWith(AUTH_DRIVE_PREFIX) -> SafProvider.GOOGLE_DRIVE
            auth == AUTH_DOWNLOADS || auth.contains("downloads") -> SafProvider.DOWNLOADS
            isNetworkAuthority(auth) -> SafProvider.NETWORK_NAS
            auth.isBlank() -> SafProvider.LOCAL_STORAGE
            else -> SafProvider.OTHER
        }
    }

    /** Provider SAF tipici di SMB/NAS / storage di rete. */
    fun isNetworkAuthority(authority: String): Boolean {
        val a = authority.lowercase()
        return a.contains("smb") ||
            a.contains("samba") ||
            a.contains("cif") ||
            a.contains("nas") ||
            a.contains("networkstorage") ||
            a.contains("network_storage") ||
            a.contains("sambadocuments") ||
            a.contains("synology") ||
            a.contains("qnap") ||
            a.contains("webdav") ||
            a.contains("share.") ||
            a.endsWith(".share") ||
            a.contains("lanstorage")
    }

    /**
     * Titolo cartella per UI. Per cloud usa il DISPLAY_NAME del documento albero
     * (es. test2_mediamanager su Drive) invece di inventare «Tutta la memoria».
     */
    fun folderTitle(context: Context, treeUri: Uri, fallback: String): String {
        queryTreeDisplayName(context, treeUri)?.let { name ->
            if (name.isNotBlank() && !looksIllegible(name)) return name
        }
        return folderTitleFromPath(treeUri, fallback)
    }

    /** Variante senza Context (test / path già noti). */
    fun folderTitleFromPath(treeUri: Uri, fallback: String): String {
        val provider = providerOf(treeUri)
        val path = humanPath(treeUri) ?: return fallback.ifBlank { defaultFolderFallback(provider) }
        if (provider != SafProvider.LOCAL_STORAGE) {
            // Path opachi cloud: non interpretare come radice «Tutta la memoria».
            val leaf = path.substringAfterLast('/').ifBlank { path }
            return if (leaf.equals("Memoria principale", ignoreCase = true) ||
                leaf.equals(ROOT_FOLDER_TITLE, ignoreCase = true)
            ) {
                fallback.ifBlank { defaultFolderFallback(provider) }
            } else {
                leaf
            }
        }
        val relative = path.substringAfter('/', missingDelimiterValue = "")
        return if (relative.isBlank()) {
            ROOT_FOLDER_TITLE
        } else {
            relative.substringAfterLast('/').ifBlank { relative }
        }
    }

    fun humanPath(treeUri: Uri): String? {
        val provider = providerOf(treeUri)
        val docId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull()
            ?: return decodeLastSegment(treeUri)
        if (provider != SafProvider.LOCAL_STORAGE) {
            // DocumentId Drive/cloud è opaco: non mapparlo su «Memoria principale».
            return null
        }
        return humanizeDocumentId(docId)
    }

    /**
     * Percorso UI **senza** ripetere il nome dispositivo (quello sta su [deviceLabelForTree]).
     * Locale: es. `Memoria principale/Pictures/Camera`.
     * Drive/NAS: percorso/cartella intera, non «Google Drive/…» duplicato.
     */
    fun humanPathOrCloud(context: Context, treeUri: Uri): String = storagePathLabel(context, treeUri)

    /** Percorso storage (riga «percorso» in Acquisisci), distinto dal dispositivo. */
    fun storagePathLabel(context: Context, treeUri: Uri): String {
        humanPath(treeUri)?.let { return stripVolumeWords(stripDevicePrefix(it)) }
        val docId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull()
        val decoded = docId?.let { decode(it) }.orEmpty()
        // Alcuni provider rete/Drive usano id tipo share:path/to/folder
        if (decoded.contains(':')) {
            val after = decoded.substringAfter(':').trim('/').replace('\\', '/')
            if (after.isNotBlank() && after.contains('/')) {
                return stripVolumeWords(stripDevicePrefix(after))
            }
            if (after.isNotBlank() && providerOf(treeUri) != SafProvider.LOCAL_STORAGE) {
                val folder = folderTitle(context, treeUri, after)
                // Preferisci percorso dopo ':' se leggibile; altrimenti titolo cartella.
                val path = when {
                    after.contains('/') -> after
                    !folder.equals(after, ignoreCase = true) && !looksIllegible(folder) -> folder
                    else -> after
                }
                return stripVolumeWords(stripDevicePrefix(path))
            }
        } else if (decoded.contains('/') || decoded.contains('\\')) {
            return stripVolumeWords(stripDevicePrefix(decoded.trim('/').replace('\\', '/')))
        }
        return stripVolumeWords(
            stripDevicePrefix(
                folderTitle(context, treeUri, defaultFolderFallback(providerOf(treeUri))),
            ),
        )
    }

    /**
     * Rimuove un eventuale prefisso «Google Drive/» (o altri device) dal percorso,
     * così non si ripete sotto la riga dispositivo.
     */
    fun stripDevicePrefix(path: String): String {
        val trimmed = path.trim().trimStart('/')
        val prefixes = listOf(
            DEVICE_GOOGLE_DRIVE,
            DEVICE_DOWNLOADS,
            DEVICE_PC_NOTEBOOK,
            DEVICE_NETWORK_NAS,
            DEVICE_CLOUD_OTHER,
            "Rete / NAS", // etichetta legacy pre-0.16.51
            "Disco di Rete",
            "Network Storage",
        )
        for (prefix in prefixes) {
            if (trimmed.equals(prefix, ignoreCase = true)) return trimmed
            val withSlash = "$prefix/"
            if (trimmed.startsWith(withSlash, ignoreCase = true)) {
                return trimmed.substring(withSlash.length).ifBlank { trimmed }
            }
        }
        return trimmed
    }

    /**
     * Percorso UI sotto il nome (come in Importa): niente prefisso dispositivo
     * e niente foglia finale uguale al nome in grassetto (file/cartella).
     * Esempio: nome `FilmoraGo`, path grezzo `Google Drive/FilmoraGo` → `""`
     * (il dispositivo è già sulla riga sopra); nome `MediaManager_test`,
     * path `Memoria principale/Pictures/MediaManager_test` → `Memoria principale/Pictures`.
     */
    fun pathUnderName(path: String, displayName: String, deviceLabel: String = ""): String {
        var p = stripVolumeWords(stripDevicePrefix(path))
        if (looksIllegible(p)) return ""
        if (deviceLabel.isNotBlank() && p.equals(deviceLabel, ignoreCase = true)) return ""
        // Ripeti strip se il path inizia ancora col device (es. dopo alias).
        if (deviceLabel.isNotBlank()) {
            val withSlash = "${deviceLabel.trim()}/"
            if (p.startsWith(withSlash, ignoreCase = true)) {
                p = p.substring(withSlash.length)
            }
            // LOCK S25 #4: togli host dopo «·» (es. device «Disco di rete / NAS · test3_SDA1»).
            val host = deviceLabel.substringAfter('·', missingDelimiterValue = "").trim()
            if (host.isNotBlank()) {
                if (p.equals(host, ignoreCase = true)) return ""
                val hostSlash = "$host/"
                if (p.startsWith(hostSlash, ignoreCase = true)) {
                    p = p.substring(hostSlash.length)
                }
            }
        }
        val leaf = displayName.trim()
        if (leaf.isEmpty()) return p
        if (p.equals(leaf, ignoreCase = true)) return ""
        val suffix = "/$leaf"
        if (p.length > suffix.length &&
            p.regionMatches(p.length - suffix.length, suffix, 0, suffix.length, ignoreCase = true)
        ) {
            p = p.substring(0, p.length - suffix.length)
        }
        val cleaned = stripVolumeWords(p.trim().trim('/'))
        return if (looksIllegible(cleaned)) "" else cleaned
    }

    fun humanizeDocumentId(docId: String): String {
        val decoded = decode(docId)
        if (!decoded.contains(':')) {
            // Non è un id volume:path (tipico Drive) → non inventare Memoria principale.
            return decoded.ifBlank { docId }
        }
        val path = decoded.substringAfter(':', missingDelimiterValue = "")
            .trim('/')
            .replace('\\', '/')
        // Nota ter: niente «Memoria principale» / «Scheda SD» nel percorso UI —
        // il badge SD e il dispositivo stanno sulla riga meta, non nel path.
        return path
    }

    fun volumeLabel(volume: String): String = when (volume.lowercase()) {
        "", "primary" -> "Memoria principale"
        "home" -> "Memoria principale"
        else -> when {
            volume.startsWith("sdcard", ignoreCase = true) -> "Scheda SD"
            volume.length >= 4 && volume.all { it.isDigit() || it in 'A'..'F' || it in 'a'..'f' || it == '-' } ->
                "Scheda SD / memoria secondaria"
            else -> volume
        }
    }

    /**
     * Nota ter: rimuove dal path termini volume («Memoria principale», «Scheda SD», …)
     * che non devono comparire in Acquisisci/Importa/Dettaglio.
     */
    fun stripVolumeWords(path: String): String {
        var p = path.trim().trimStart('/').replace('\\', '/')
        val volumeWords = listOf(
            "Scheda SD / memoria secondaria",
            "Scheda SD / Memoria secondaria",
            "memoria secondaria",
            "Memoria secondaria",
            "Scheda SD",
            "scheda Sd",
            "scheda sd",
            "Memoria principale",
            "memoria principale",
        )
        for (word in volumeWords) {
            if (p.equals(word, ignoreCase = true)) return ""
            val withSlash = "$word/"
            if (p.startsWith(withSlash, ignoreCase = true)) {
                p = p.substring(withSlash.length)
            }
        }
        return p.trim('/').trim()
    }

    fun queryTreeDisplayName(context: Context, treeUri: Uri): String? {
        val docId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull()
            ?: return null
        val docUri = runCatching {
            DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
        }.getOrNull() ?: return null
        return runCatching {
            context.contentResolver.query(
                docUri,
                arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
                null,
                null,
                null,
            )?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val idx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                if (idx < 0) null else cursor.getString(idx)?.trim()?.takeIf { it.isNotEmpty() }
            }
        }.getOrNull()
    }

    fun looksIllegible(name: String): Boolean {
        if (name.isBlank()) return true
        val lower = name.lowercase()
        return lower.contains("encoded=") ||
            lower.startsWith("acc=") ||
            lower.contains("doc=") ||
            (name.length > 40 && !name.contains('/') && name.count { it == '-' || it == '_' } > 4)
    }

    private fun defaultFolderFallback(provider: SafProvider): String = when (provider) {
        SafProvider.GOOGLE_DRIVE -> "Cartella Drive"
        SafProvider.DOWNLOADS -> "Download"
        SafProvider.NETWORK_NAS -> "Cartella di rete"
        SafProvider.OTHER -> "Cartella cloud"
        SafProvider.LOCAL_STORAGE -> "Cartella"
    }

    private fun decodeLastSegment(uri: Uri): String? {
        val last = uri.lastPathSegment ?: return null
        if (last.contains("encoded=", ignoreCase = true) || last.startsWith("acc=")) {
            return null
        }
        return humanizeDocumentId(last)
    }

    private fun decode(value: String): String =
        runCatching {
            URLDecoder.decode(value, StandardCharsets.UTF_8.name())
        }.getOrDefault(value)

    enum class SafProvider {
        LOCAL_STORAGE,
        GOOGLE_DRIVE,
        DOWNLOADS,
        NETWORK_NAS,
        OTHER,
    }
}
