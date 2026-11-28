package com.renatizzi.photovideomanager.data.hub

import java.security.SecureRandom

/**
 * Generates an idempotent Windows setup package (PowerShell + CMD launcher)
 * for MediaManager catalog folder + dedicated local SMB user.
 *
 * LOCK share disco: la cartella Catalogo resta `X:\Nome`; la condivisione SMB
 * punta alla **radice del disco** (`X:\`) con lo stesso nome share = Nome Catalogo,
 * così in CIFS si navigano Catalogo e cartelle foto senza una procedura per cartella.
 */
object WindowsCatalogSetupScript {
    /** Account locale dedicato + share + cartella: un solo vocabolo (LOCK Setup PC). */
    const val DEFAULT_USER = "MyMedia"
    const val DEFAULT_SHARE = "MyMedia"
    const val DEFAULT_FOLDER = "C:\\MyMedia"
    /** Solo testi help. Non usare come User CIFS da copiare. */
    const val CIFS_USER_PC_PLACEHOLDER = "NOMEPC"
    /**
     * Folder CIFS legacy (vuoto): espone l’elenco share — non usare per la sede Catalogo.
     * Preferire [cifsFolderPin] / [PcCatalogSeatGate.cifsFolderPin] = nome share.
     */
    const val CIFS_FOLDER_EMPTY = ""

    /** Folder CIFS da copiare: nome share SMB (pin), non path Windows e non vuoto. */
    fun cifsFolderPin(shareName: String = DEFAULT_SHARE): String =
        PcCatalogSeatGate.cifsFolderPin(shareName)
    /** File da avviare (doppio clic). Nome PPT: CONFIGURA. */
    const val CMD_FILE_NAME = "CONFIGURA.cmd"
    /** Supporto interno (non aprire). Nome PPT: sup. */
    const val PS1_FILE_NAME = "sup.ps1"
    /**
     * Prefissi da ripulire **solo dopo** Setup OK ([WindowsSetupScriptExporter.scrubDownloadsScripts]).
     * Non usare in export: cancellare `DATI_CIFS%` mentre il telefono e' aperto via MTP
     * crea file fantasma 0 byte / data 1970 non eliminabili da Esplora file.
     */
    val DOWNLOAD_SCRUB_PREFIXES: List<String> = listOf(
        "CONFIGURA",
        "sup.ps1", // non "sup" nudo (troppo ampio)
        "DATI_CIFS",
        "no_pw",
        "AVVIA_Configurazione_Catalogo",
        "_non_aprire_supporto_setup",
        "MediaManager_Setup_Catalogo",
        "QRCoder",
        "MediaManager_setup_handoff",
        "MediaManager_cifs_bundle",
        "MediaManager_setup_result",
    )
    /** Result senza password (BL-BT-01): non importare come bundle. */
    const val RESULT_FILE_NAME = "no_pw.json"
    /** Nome legacy del result (pre-no_pw); ancora cercato in lettura. */
    const val LEGACY_RESULT_FILE_NAME = "MediaManager_setup_result.json"
    /** URI pvmsetup:2;… + istruzioni (Desktop). */
    const val HANDOFF_FILE_NAME = "MediaManager_setup_handoff.txt"
    /** QR PNG generato a «Tutto pronto» (Desktop). */
    const val HANDOFF_QR_FILE_NAME = "MediaManager_setup_handoff.png"
    /** Encoder QR (MIT) esportato insieme a CONFIGURA/sup. */
    const val QRCODER_DLL_FILE_NAME = "QRCoder.dll"
    /** Solo i 3 file dello script in export (mai DATI_CIFS / no_pw). */
    val SETUP_SET_FILE_NAMES: List<String> = listOf(
        CMD_FILE_NAME,
        PS1_FILE_NAME,
        QRCODER_DLL_FILE_NAME,
    )
    const val LOG_FILE_NAME = "MediaManager_setup.log"
    /** Porte annuncio dopo Tutto pronto (allineate a [SetupLanAnnounce]). */
    const val SETUP_ANNOUNCE_UDP_PORT = SetupLanAnnounce.UDP_PORT
    const val SETUP_ANNOUNCE_HTTP_PORT = SetupLanAnnounce.HTTP_PORT

    private val pathRegex =
        Regex("""^[A-Za-z]:\\(?:[^<>:"/\\|?*\r\n]+\\)*[^<>:"/\\|?*\r\n]*$""")
    private val shareRegex = Regex("""^[A-Za-z0-9][A-Za-z0-9._-]{0,79}$""")
    private val userRegex = Regex("""^[A-Za-z][A-Za-z0-9_-]{2,19}$""")

    data class Params(
        val folderPath: String = DEFAULT_FOLDER,
        val shareName: String = DEFAULT_SHARE,
        val userName: String = DEFAULT_USER,
        val temporaryPassword: String,
        /** Token sessione wizard: collega «Tutto pronto» all’ascolto in app. */
        val setupToken: String = "",
    )

    data class Package(
        val params: Params,
        val ps1FileName: String = PS1_FILE_NAME,
        val cmdFileName: String = CMD_FILE_NAME,
        val ps1Content: String,
        val cmdContent: String,
        /** Nome fisso del bundle JSON prodotto a «Tutto pronto» ([SetupCifsBundle.BUNDLE_FILE_NAME]). */
        val bundleFileName: String = SetupCifsBundle.BUNDLE_FILE_NAME,
    )

    fun generatePassword(length: Int = 14): String {
        val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#%+"
        val random = SecureRandom()
        return buildString(length) {
            repeat(length) { append(alphabet[random.nextInt(alphabet.length)]) }
        }
    }

    /**
     * Percorso Windows assoluto su qualsiasi lettera disco (interno o esterno
     * collegato al PC: USB, SATA, chiavetta, microSD, …).
     * [requireDriveC] resta solo per test/legacy; il wizard PC non lo usa più.
     */
    fun validateFolderPath(path: String, requireDriveC: Boolean = false): String? {
        val trimmed = path.trim()
        if (trimmed.isEmpty()) return "Indica il percorso della cartella sul PC"
        if (!pathRegex.matches(trimmed)) {
            return "Percorso non valido. Esempio: C:\\MyMedia"
        }
        if (requireDriveC && !trimmed.uppercase().startsWith("C:\\")) {
            return "In questa fase la cartella deve essere sul disco C: (esempio: C:\\MyMedia)"
        }
        return null
    }

    fun validateShareName(name: String): String? {
        val trimmed = name.trim()
        if (!shareRegex.matches(trimmed)) {
            return "Nome condivisione non valido (lettere, numeri, . _ -)"
        }
        return null
    }

    fun validateUserName(name: String): String? {
        val trimmed = name.trim()
        if (!userRegex.matches(trimmed)) {
            return "Nome utente non valido"
        }
        return null
    }

    /**
     * Nome Catalogo usato anche come share + account Windows + Name/Folder CIFS.
     * Più restrittivo dello share SMB: deve soddisfare anche [validateUserName]
     * (niente spazi, @, *, punti, accenti, ecc.).
     */
    fun validateCatalogNameForPc(name: String): String? {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return "Indica un nome per il Catalogo"
        validateShareName(trimmed)?.let { return it }
        validateUserName(trimmed)?.let { return it }
        return null
    }

    /**
     * User CIFS: prova OK = `COMPUTERNAME\account` (es. `MINI_IT13\MyMedia`).
     * Non anteporre il placeholder letterale `NOMEPC` (non funziona in CIFS).
     */
    fun cifsUserCopyText(userName: String, computerName: String? = null): String {
        val user = userName.trim().ifBlank { DEFAULT_USER }
        if ('\\' in user) return user
        val pc = computerName?.trim().orEmpty()
        return if (pc.isNotEmpty()) "$pc\\$user" else user
    }

    fun build(params: Params): Package {
        validateFolderPath(params.folderPath)?.let { error(it) }
        validateShareName(params.shareName)?.let { error(it) }
        validateUserName(params.userName)?.let { error(it) }
        require(params.temporaryPassword.length >= 8) { "Password troppo corta" }
        require(params.setupToken.matches(Regex("^[A-Za-z0-9_-]{8,64}$"))) {
            "Token setup non valido"
        }

        val bundleFileName = SetupCifsBundle.BUNDLE_FILE_NAME
        val pkg = Package(
            params = params,
            ps1Content = buildPs1(params, bundleFileName),
            cmdContent = buildCmd(bundleFileName),
            bundleFileName = bundleFileName,
        )
        assertAsciiDeliverable(pkg.ps1Content, "ps1")
        assertAsciiDeliverable(pkg.cmdContent, "cmd")
        assertPs1QuoteBalance(pkg.ps1Content)
        return pkg
    }

    /**
     * Windows PowerShell 5.1 may mis-parse UTF-8 without BOM if non-ASCII is present.
     * Deliverables must be ASCII-only; exporter still writes UTF-8 with BOM.
     */
    fun assertAsciiDeliverable(content: String, label: String) {
        val offenders = content.withIndex()
            .filter { it.value.code > 127 }
            .take(8)
            .map { "U+${it.value.code.toString(16).uppercase()}@${it.index}" }
        require(offenders.isEmpty()) {
            "Script $label contiene caratteri non ASCII (romperebbero PowerShell 5.1): $offenders"
        }
    }

    /** Lightweight guard: odd double-quote count usually means a broken string (as in the 0.16.23 failure). */
    fun assertPs1QuoteBalance(ps1: String) {
        val doubleQuotes = ps1.count { it == '"' }
        require(doubleQuotes % 2 == 0) {
            "Script ps1: numero dispari di virgolette doppie ($doubleQuotes) - possibile stringa spezzata"
        }
    }

    /** Bytes for .ps1: UTF-8 with BOM + ASCII body (CRLF). */
    fun utf8BomBytes(content: String): ByteArray {
        assertAsciiDeliverable(content, "utf8Bom")
        val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        return bom + crlf(content).toByteArray(Charsets.UTF_8)
    }

    /** Bytes for .cmd: ASCII only, no BOM, CRLF (BOM breaks @echo off). */
    fun asciiCrlfNoBomBytes(content: String): ByteArray {
        assertAsciiDeliverable(content, "cmdBytes")
        return crlf(content).toByteArray(Charsets.US_ASCII)
    }

    private fun crlf(content: String): String =
        content.replace("\r\n", "\n").replace("\n", "\r\n").let {
            if (it.endsWith("\r\n")) it else it + "\r\n"
        }

    /**
     * Script secondario avviato a Tutto pronto: HTTP + UDP con placeholder
     * `__PVM_PAYLOAD__` / `__PVM_UDP__` / `__PVM_HTTP__`.
     */
    internal fun buildAnnouncerScript(): String {
        val s = "${'$'}"
        // TcpListener (no URL ACL) + UDP broadcast: l'app recupera IP/User in LAN.
        return """
            ${s}ErrorActionPreference = 'SilentlyContinue'
            ${s}payload = '__PVM_PAYLOAD__'
            ${s}udpPort = __PVM_UDP__
            ${s}httpPort = __PVM_HTTP__
            try {
              New-NetFirewallRule -DisplayName 'MediaManager Setup HTTP' -Direction Inbound -Action Allow -Protocol TCP -LocalPort ${s}httpPort -Profile Any -ErrorAction SilentlyContinue | Out-Null
              New-NetFirewallRule -DisplayName 'MediaManager Setup UDP' -Direction Inbound -Action Allow -Protocol UDP -LocalPort ${s}udpPort -Profile Any -ErrorAction SilentlyContinue | Out-Null
            } catch {}
            ${s}tcp = ${s}null
            try {
              ${s}tcp = New-Object System.Net.Sockets.TcpListener ([System.Net.IPAddress]::Any, ${s}httpPort)
              ${s}tcp.Start()
            } catch { ${s}tcp = ${s}null }
            ${s}udp = New-Object System.Net.Sockets.UdpClient
            ${s}udp.EnableBroadcast = ${s}true
            ${s}bytes = [System.Text.Encoding]::UTF8.GetBytes(${s}payload)
            ${s}end = (Get-Date).AddMinutes(5)
            function Send-UdpAll {
              try {
                ${s}epAll = New-Object System.Net.IPEndPoint ([System.Net.IPAddress]::Broadcast, ${s}udpPort)
                [void]${s}udp.Send(${s}bytes, ${s}bytes.Length, ${s}epAll)
              } catch {}
              try {
                Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue |
                  Where-Object { ${s}_.IPAddress -notlike '127.*' -and ${s}_.IPAddress -notlike '169.254.*' } |
                  ForEach-Object {
                    ${s}parts = ${s}_.IPAddress.Split('.')
                    if (${s}parts.Count -eq 4) {
                      ${s}bcast = ('{0}.{1}.{2}.255' -f ${s}parts[0], ${s}parts[1], ${s}parts[2])
                      ${s}ep = New-Object System.Net.IPEndPoint ([System.Net.IPAddress]::Parse(${s}bcast), ${s}udpPort)
                      [void]${s}udp.Send(${s}bytes, ${s}bytes.Length, ${s}ep)
                    }
                  }
              } catch {}
            }
            ${s}lastUdp = Get-Date
            while ((Get-Date) -lt ${s}end) {
              if (((Get-Date) - ${s}lastUdp).TotalMilliseconds -ge 250) {
                Send-UdpAll
                ${s}lastUdp = Get-Date
              }
              if (${s}tcp -ne ${s}null) {
                try {
                  if (${s}tcp.Pending()) {
                    ${s}client = ${s}tcp.AcceptTcpClient()
                    try {
                      ${s}stream = ${s}client.GetStream()
                      ${s}req = New-Object byte[] 4096
                      try { [void]${s}stream.Read(${s}req, 0, ${s}req.Length) } catch {}
                      ${s}body = [System.Text.Encoding]::UTF8.GetBytes(${s}payload)
                      ${s}hdr = [System.Text.Encoding]::ASCII.GetBytes(
                        ('HTTP/1.1 200 OK' + [char]13 + [char]10 +
                         'Content-Type: application/json; charset=utf-8' + [char]13 + [char]10 +
                         ('Content-Length: {0}' -f ${s}body.Length) + [char]13 + [char]10 +
                         'Connection: close' + [char]13 + [char]10 + [char]13 + [char]10)
                      )
                      ${s}stream.Write(${s}hdr, 0, ${s}hdr.Length)
                      ${s}stream.Write(${s}body, 0, ${s}body.Length)
                      ${s}stream.Flush()
                    } finally {
                      try { ${s}client.Close() } catch {}
                    }
                  } else {
                    Start-Sleep -Milliseconds 40
                  }
                } catch { Start-Sleep -Milliseconds 40 }
              } else {
                Start-Sleep -Milliseconds 250
              }
            }
            try { if (${s}tcp -ne ${s}null) { ${s}tcp.Stop() } } catch {}
            try { ${s}udp.Close() } catch {}
        """.trimIndent().replace("\n", "\r\n") + "\r\n"
    }

    private fun buildCmd(bundleFileName: String): String = crlf(
        """
        @echo off
        setlocal
        title MediaManager - configurazione Catalogo
        net session >nul 2>&1
        if %errorlevel% NEQ 0 (
          echo Richiesta autorizzazione Amministratore...
          powershell -NoProfile -Command "Start-Process -FilePath '%~f0' -Verb RunAs"
          exit /b
        )
        echo.
        echo  MediaManager: prepara cartella Catalogo e accesso SMB
        echo  (account dedicato, senza privilegi di amministratore permanenti)
        echo.
        rem Windows / MTP crea spesso "sup (1).ps1" invece di sovrascrivere:
        rem cerca il canonico, altrimenti la variante (n) piu recente.
        set "SUP=%~dp0$PS1_FILE_NAME"
        if not exist "%SUP%" (
          for /f "delims=" %%F in ('dir /b /o-d "%~dp0sup (*).ps1" 2^>nul') do (
            set "SUP=%~dp0%%F"
            goto :pvm_have_sup
          )
        )
        :pvm_have_sup
        if not exist "%SUP%" (
          echo  Mancante: $PS1_FILE_NAME accanto a CONFIGURA ^(anche come "sup (1).ps1"^).
          echo  Copia di nuovo i 3 file dal Download del Telefono e sovrascrivi.
          pause
          exit /b 1
        )
        powershell -NoProfile -ExecutionPolicy Bypass -File "%SUP%"
        set ERR=%ERRORLEVEL%
        echo.
        if %ERR% NEQ 0 (
          echo  Configurazione NON completata. Codice: %ERR%
          echo  10=cartella 20=utente 30=condivisione 40=NTFS 50=rete/firewall 60=verifica
        ) else (
          echo  Tutto pronto. PROSSIMO PASSO: USB
          echo  1^) Sul Desktop trovi $bundleFileName
          echo  2^) Copia SOLO quel file nella cartella Download del Telefono via cavo USB
          echo  3^) Ignora no_pw.json ^(senza password^)
          echo  4^) Nell'app: Importa dati CIFS e seleziona $bundleFileName
        )
        echo.
        pause
        rem Pulisci solo il supporto canonicamente nominato (non toccare (1)/(2) qui:
        rem Maggiordomo in sup.ps1 li consolida e rimuove).
        if exist "%~dp0$PS1_FILE_NAME" del "%~dp0$PS1_FILE_NAME" >nul 2>&1
        exit /b %ERR%
        """.trimIndent(),
    )

    private fun psQuote(value: String): String =
        "'" + value.replace("'", "''") + "'"

    private fun buildPs1(params: Params, bundleFileName: String): String {
        val folder = psQuote(params.folderPath.trim())
        val share = psQuote(params.shareName.trim())
        val user = psQuote(params.userName.trim())
        val password = psQuote(params.temporaryPassword)
        val setupToken = psQuote(params.setupToken.trim())
        val resultName = psQuote(RESULT_FILE_NAME)
        val bundleNameQuoted = psQuote(bundleFileName)
        val logName = psQuote(LOG_FILE_NAME)
        val announceUdp = SETUP_ANNOUNCE_UDP_PORT
        val announceHttp = SETUP_ANNOUNCE_HTTP_PORT
        val announceMagic = SetupLanAnnounce.MAGIC
        val announcerB64 = java.util.Base64.getEncoder()
            .encodeToString(buildAnnouncerScript().toByteArray(Charsets.US_ASCII))
        val d = "${'$'}"

        // PowerShell body: keep parameters at top; exit codes 10/20/30/40/50/60.
        return """
# MediaManager - Setup Catalogo su PC Windows (idempotente)
# Generato dall'app. Contiene una password provvisoria: eliminato a fine OK.
# Target: Windows 10/11. Avviare tramite il .cmd (auto-elevazione UAC).

#Requires -RunAsAdministrator
${d}ErrorActionPreference = 'Stop'

# --- Parametri (blocco unico) ---
${d}FolderPath     = $folder
${d}ShareName      = $share
${d}UserName       = $user
${d}PlainPassword  = $password
${d}SetupToken     = $setupToken
${d}ResultFileName = $resultName
${d}BundleFileName = $bundleNameQuoted
${d}LogFileName    = $logName
${d}AnnounceUdp    = $announceUdp
${d}AnnounceHttp   = $announceHttp
${d}AnnounceMagic  = '$announceMagic'
${d}FirewallGroup  = '@FirewallAPI.dll,-28502'
${d}ExitCode       = 1
# Radice disco da condividere (es. C:\): Catalogo resta in FolderPath sotto il disco.
${d}ShareRoot      = [System.IO.Path]::GetPathRoot(${d}FolderPath)
if ([string]::IsNullOrWhiteSpace(${d}ShareRoot)) {
  Write-Error 'Radice disco non determinata dal percorso Catalogo.'
  exit 10
}

${d}ParentDir = Split-Path -Parent ${d}FolderPath
if (-not ${d}ParentDir) { ${d}ParentDir = 'C:\MediaManager' }
if (-not (Test-Path -LiteralPath ${d}ParentDir)) {
  New-Item -ItemType Directory -Path ${d}ParentDir -Force | Out-Null
}
${d}LogPath = Join-Path ${d}ParentDir ${d}LogFileName
${d}ResultPath = Join-Path ${d}ParentDir ${d}ResultFileName

function Write-Log(${d}msg) {
  ${d}line = ("[{0}] {1}" -f (Get-Date -Format 'yyyy-MM-dd HH:mm:ss'), ${d}msg)
  Add-Content -LiteralPath ${d}LogPath -Value ${d}line -Encoding UTF8 -ErrorAction SilentlyContinue
}
function Write-Step(${d}msg) {
  Write-Host ("[{0}] {1}" -f (Get-Date -Format 'HH:mm:ss'), ${d}msg)
  Write-Log ${d}msg
}
function Write-Fail(${d}msg) {
  Write-Host ("ERRORE:`n{0}" -f ${d}msg) -ForegroundColor Red
  Write-Log ("ERRORE: " + ${d}msg)
}
function Fail-Step {
  param([int]${d}Code, [string]${d}Message)
  ${d}script:ExitCode = ${d}Code
  ${d}pos = ${d}null
  if (${d}Error -and ${d}Error[0]) { ${d}pos = ${d}Error[0].InvocationInfo.PositionMessage }
  ${d}full = ${d}Message
  if (${d}pos) { ${d}full = ${d}Message + "`n" + ${d}pos }
  throw (${d}full)
}

${d}steps = New-Object System.Collections.Generic.List[object]
function Record(${d}name, ${d}ok, ${d}detail) {
  ${d}steps.Add([pscustomobject]@{ name = ${d}name; ok = [bool]${d}ok; detail = [string]${d}detail }) | Out-Null
}

function Test-PvmSetVariantName([string]${d}fileName, [string]${d}need) {
  # Due pattern reali: "CONFIGURA (1).cmd" E "CONFIGURA.cmd (1)" (MTP Windows).
  if (${d}fileName -ieq ${d}need) { return ${d}true }
  ${d}stem = [IO.Path]::GetFileNameWithoutExtension(${d}need)
  ${d}ext = [IO.Path]::GetExtension(${d}need)
  ${d}patBefore = '^' + [regex]::Escape(${d}stem) + '\s*\(\d+\)' + [regex]::Escape(${d}ext) + '${'$'}'
  ${d}patAfter = '^' + [regex]::Escape(${d}need) + '\s*\(\d+\)${'$'}'
  return [bool]((${d}fileName -match ${d}patBefore) -or (${d}fileName -match ${d}patAfter))
}

function Invoke-PvmMaggiordomo([string]${d}destDir) {
  # Strategia anti-(1)/(2): leggi bytes, elimina TUTTI i match, scrivi solo il canonic.
  # Copy-Item -Force sul file in uso fallisce; ReadAllBytes+WriteAllBytes no.
  ${d}desk = [Environment]::GetFolderPath('Desktop')
  ${d}dl = Join-Path ${d}env:USERPROFILE 'Downloads'
  ${d}recv1 = Join-Path ${d}env:USERPROFILE 'Documents\Bluetooth Exchange Folder'
  ${d}recv2 = Join-Path ${d}env:USERPROFILE 'Documents\File ricevuti'
  ${d}dirs = New-Object System.Collections.Generic.List[string]
  foreach (${d}cand in @(${d}PSScriptRoot, ${d}desk, ${d}dl, ${d}recv1, ${d}recv2, ${d}destDir)) {
    if (${d}cand -and (Test-Path -LiteralPath ${d}cand) -and -not (${d}dirs -contains ${d}cand)) {
      ${d}dirs.Add(${d}cand) | Out-Null
    }
  }
  ${d}runningSup = ${d}null
  try {
    if (${d}PSCommandPath -and (Test-Path -LiteralPath ${d}PSCommandPath)) {
      ${d}runningSup = Get-Item -LiteralPath ${d}PSCommandPath -ErrorAction SilentlyContinue
    } elseif (${d}MyInvocation -and ${d}MyInvocation.MyCommand -and ${d}MyInvocation.MyCommand.Path) {
      ${d}runningSup = Get-Item -LiteralPath ${d}MyInvocation.MyCommand.Path -ErrorAction SilentlyContinue
    }
  } catch {}
  foreach (${d}need in @('CONFIGURA.cmd','sup.ps1','QRCoder.dll')) {
    ${d}hit = ${d}null
    if (${d}need -ieq 'sup.ps1' -and ${d}runningSup) { ${d}hit = ${d}runningSup }
    ${d}beside = Join-Path ${d}PSScriptRoot ${d}need
    if ((-not ${d}hit) -and (Test-Path -LiteralPath ${d}beside)) {
      ${d}hit = Get-Item -LiteralPath ${d}beside -ErrorAction SilentlyContinue
    }
    foreach (${d}dir in ${d}dirs) {
      Get-ChildItem -LiteralPath ${d}dir -File -ErrorAction SilentlyContinue |
        Where-Object { Test-PvmSetVariantName -fileName ${d}_.Name -need ${d}need } |
        ForEach-Object {
          if (-not ${d}hit -or ${d}_.LastWriteTime -gt ${d}hit.LastWriteTime) { ${d}hit = ${d}_ }
        }
    }
    if (-not ${d}hit) { continue }
    try {
      ${d}bytes = [IO.File]::ReadAllBytes(${d}hit.FullName)
    } catch { continue }
    foreach (${d}dir in ${d}dirs) {
      Get-ChildItem -LiteralPath ${d}dir -File -ErrorAction SilentlyContinue |
        Where-Object { Test-PvmSetVariantName -fileName ${d}_.Name -need ${d}need } |
        ForEach-Object {
          try { Remove-Item -LiteralPath ${d}_.FullName -Force -ErrorAction SilentlyContinue } catch {}
        }
      try {
        [IO.File]::WriteAllBytes((Join-Path ${d}dir ${d}need), ${d}bytes)
      } catch {}
    }
  }
}

function Invoke-PvmIcaclsGrant([string]${d}path, [string]${d}user, [switch]${d}Recurse, [int]${d}TimeoutSec = 45) {
  # icacls /T su profili enormi si blocca: timeout e continua.
  if (-not (Test-Path -LiteralPath ${d}path)) { return ${d}true }
  ${d}flags = if (${d}Recurse) { ' /T' } else { '' }
  ${d}arg = '/c icacls "' + ${d}path + '" /grant ' + ${d}user + ':(OI)(CI)RX' + ${d}flags + ' /C /Q >nul 2>&1'
  try {
    ${d}p = Start-Process -FilePath 'cmd.exe' -ArgumentList ${d}arg -PassThru -WindowStyle Hidden
    if (-not ${d}p) { return ${d}false }
    if (-not ${d}p.WaitForExit(${d}TimeoutSec * 1000)) {
      try { Stop-Process -Id ${d}p.Id -Force -ErrorAction SilentlyContinue } catch {}
      Write-Step ('  ACL timeout ' + ${d}TimeoutSec + 's: ' + ${d}path + ' (continuo)')
      return ${d}false
    }
    return (${d}p.ExitCode -eq 0)
  } catch {
    return ${d}false
  }
}

function Get-PvmShellDisplayName([string]${d}path) {
  # Esplora: Immagini; SMB: Pictures. Titolo shell = nome locale.
  try {
    ${d}shell = New-Object -ComObject Shell.Application
    ${d}ns = ${d}shell.NameSpace(${d}path)
    if (${d}null -eq ${d}ns) { return ${d}null }
    ${d}title = [string]${d}ns.Title
    if (${d}title) { return ${d}title.Trim() }
  } catch {}
  return ${d}null
}

function Invoke-PvmLocalizedFolderAliases([string]${d}profilePath, [string]${d}user) {
  # Junction localizzati (Immagini -> Pictures, ecc.) cosi' CIFS mostra gli stessi nomi di Windows.
  if (-not (Test-Path -LiteralPath ${d}profilePath)) { return }
  ${d}knownFs = @('Pictures','Documents','Videos','Music','Downloads','Desktop')
  ${d}invalid = [IO.Path]::GetInvalidFileNameChars()
  foreach (${d}fs in ${d}knownFs) {
    ${d}target = Join-Path ${d}profilePath ${d}fs
    if (-not (Test-Path -LiteralPath ${d}target)) { continue }
    ${d}display = Get-PvmShellDisplayName -path ${d}target
    if (-not ${d}display) { continue }
    if (${d}display -ieq ${d}fs) { continue }
    if (${d}display.IndexOfAny(${d}invalid) -ge 0) { continue }
    ${d}alias = Join-Path ${d}profilePath ${d}display
    if (Test-Path -LiteralPath ${d}alias) { continue }
    try {
      ${d}linkArg = '/c mklink /J "' + ${d}alias + '" "' + ${d}target + '"'
      ${d}lp = Start-Process -FilePath 'cmd.exe' -ArgumentList ${d}linkArg -PassThru -WindowStyle Hidden -Wait
      if (${d}lp -and ${d}lp.ExitCode -eq 0 -and (Test-Path -LiteralPath ${d}alias)) {
        Write-Step ('  Alias locale CIFS: ' + ${d}display + ' -> ' + ${d}fs)
        [void](Invoke-PvmIcaclsGrant -path ${d}alias -user ${d}user -TimeoutSec 10)
      }
    } catch {}
  }
}

try {
  Write-Host ""
  Write-Host "=== MediaManager: configurazione Catalogo ===" -ForegroundColor Cyan
  Write-Host "ATTESA: la routine puo richiedere alcuni minuti." -ForegroundColor Yellow
  Write-Host "Non chiudere questa finestra e non interrompere finche non compare Tutto pronto." -ForegroundColor Yellow
  Write-Host ""
  Write-Host "Passo 1 di 6 - Controlli preliminari" -ForegroundColor Cyan
  Write-Host ""
  try {
    Invoke-PvmMaggiordomo -destDir ${d}PSScriptRoot
    ${d}desk0 = [Environment]::GetFolderPath('Desktop')
    if (${d}desk0) { Invoke-PvmMaggiordomo -destDir ${d}desk0 }
    Write-Step 'Maggiordomo: file consolidati, duplicati (1)/(2) rimossi.'
  } catch {
    Write-Step 'Maggiordomo: salto (continua comunque).'
  }

  ${d}identity = [Security.Principal.WindowsIdentity]::GetCurrent()
  ${d}principal = New-Object Security.Principal.WindowsPrincipal(${d}identity)
  if (-not ${d}principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Fail-Step 50 'Esecuzione non elevata. Avvia il file .cmd e conferma Si al Controllo account utente.'
  }
  Write-Step ("Windows {0}" -f [Environment]::OSVersion.Version.ToString())

  Write-Host "Passo 2 di 6 - Cartella Catalogo" -ForegroundColor Cyan
  Write-Step "Cartella Catalogo: ${d}FolderPath"
  try {
    if (-not (Test-Path -LiteralPath ${d}FolderPath)) {
      New-Item -ItemType Directory -Path ${d}FolderPath -Force | Out-Null
      Record 'folder' ${d}true 'creata'
      Write-Step "Cartella creata."
    } else {
      Record 'folder' ${d}true 'gia_presente'
      Write-Step "Cartella gia presente (nessuna sovrascrittura)."
    }
  } catch {
    Fail-Step 10 ("Creazione cartella non riuscita: " + ${d}_.Exception.Message)
  }

  Write-Host "Passo 3 di 6 - Account dedicato" -ForegroundColor Cyan
  Write-Step "Account locale: ${d}UserName"
  try {
    ${d}secure = ConvertTo-SecureString ${d}PlainPassword -AsPlainText -Force
    ${d}existing = Get-LocalUser -Name ${d}UserName -ErrorAction SilentlyContinue
    if (${d}null -eq ${d}existing) {
      New-LocalUser -Name ${d}UserName -Password ${d}secure -FullName 'MediaManager Catalogo' `
        -Description 'Accesso SMB dedicato MediaManager' -PasswordNeverExpires:${d}true -UserMayNotChangePassword:${d}false | Out-Null
      Record 'user' ${d}true 'creato'
      Write-Step "Utente creato (senza privilegi di amministratore)."
    } else {
      Set-LocalUser -Name ${d}UserName -Password ${d}secure -PasswordNeverExpires ${d}true
      Record 'user' ${d}true 'password_aggiornata'
      Write-Step "Utente gia presente: password provvisoria aggiornata."
    }
    ${d}adminSid = New-Object System.Security.Principal.SecurityIdentifier('S-1-5-32-544')
    ${d}adminGroup = ${d}adminSid.Translate([System.Security.Principal.NTAccount]).Value
    try { Remove-LocalGroupMember -Group ${d}adminGroup -Member ${d}UserName -ErrorAction SilentlyContinue } catch {}
  } catch {
    Fail-Step 20 ("Account dedicato non riuscito: " + ${d}_.Exception.Message)
  }

  Write-Host "Passo 4 di 6 - Condivisione SMB (disco)" -ForegroundColor Cyan
  Write-Step ("Condivisione SMB: ${d}ShareName -> disco " + ${d}ShareRoot + " (Catalogo in " + ${d}FolderPath + ")")
  try {
    ${d}sharePath = ${d}ShareRoot
    ${d}shareObj = Get-SmbShare -Name ${d}ShareName -ErrorAction SilentlyContinue
    ${d}wantedPath = ([string]${d}sharePath).TrimEnd([char]92)
    if (${d}null -eq ${d}shareObj) {
      try {
        New-SmbShare -Name ${d}ShareName -Path ${d}sharePath -FullAccess ${d}UserName -FolderEnumerationMode AccessBased | Out-Null
        Record 'share' ${d}true 'creata_disco'
        Write-Step "Condivisione del disco creata."
      } catch {
        # Alcuni Windows rifiutano share sulla radice: fallback cartella Catalogo.
        Write-Step ("Condivisione disco non riuscita (" + ${d}_.Exception.Message + "); uso cartella Catalogo.")
        ${d}sharePath = ${d}FolderPath
        New-SmbShare -Name ${d}ShareName -Path ${d}sharePath -FullAccess ${d}UserName -FolderEnumerationMode AccessBased | Out-Null
        Record 'share' ${d}true 'creata_catalogo_fallback'
        Write-Step "Condivisione cartella Catalogo creata (fallback)."
      }
    } else {
      ${d}existingPath = ([string]${d}shareObj.Path).TrimEnd([char]92)
      if (${d}existingPath -ieq ${d}wantedPath) {
        Grant-SmbShareAccess -Name ${d}ShareName -AccountName ${d}UserName -AccessRight Full -Force | Out-Null
        Record 'share' ${d}true 'aggiornata'
        Write-Step "Condivisione disco gia presente: permessi aggiornati."
      } else {
        Write-Step ("Condivisione gia su '${d}existingPath': ripunto a '${d}sharePath'.")
        Remove-SmbShare -Name ${d}ShareName -Force
        try {
          New-SmbShare -Name ${d}ShareName -Path ${d}sharePath -FullAccess ${d}UserName -FolderEnumerationMode AccessBased | Out-Null
          Record 'share' ${d}true 'ripruntata_disco'
          Write-Step "Condivisione ripuntata alla radice del disco."
        } catch {
          ${d}sharePath = ${d}FolderPath
          New-SmbShare -Name ${d}ShareName -Path ${d}sharePath -FullAccess ${d}UserName -FolderEnumerationMode AccessBased | Out-Null
          Record 'share' ${d}true 'ripruntata_catalogo_fallback'
          Write-Step "Condivisione ripuntata alla cartella Catalogo (fallback)."
        }
      }
    }
    Get-SmbShareAccess -Name ${d}ShareName | Where-Object { ${d}_.AccountName -match 'Everyone|Tutti' } | ForEach-Object {
      Revoke-SmbShareAccess -Name ${d}ShareName -AccountName ${d}_.AccountName -Force -ErrorAction SilentlyContinue
    }
  } catch {
    Fail-Step 30 ("Condivisione SMB non riuscita: " + ${d}_.Exception.Message)
  }

  Write-Host "Passo 5 di 6 - Permessi NTFS" -ForegroundColor Cyan
  Write-Step "Permessi NTFS sulla cartella Catalogo"
  try {
    ${d}acl = Get-Acl -LiteralPath ${d}FolderPath
    ${d}acl.SetAccessRuleProtection(${d}true, ${d}false)
    ${d}admins = New-Object System.Security.Principal.SecurityIdentifier('S-1-5-32-544')
    ${d}system = New-Object System.Security.Principal.SecurityIdentifier('S-1-5-18')
    ${d}rules = @(
      [System.Security.AccessControl.FileSystemAccessRule]::new(${d}UserName, 'Modify', 'ContainerInherit,ObjectInherit', 'None', 'Allow'),
      [System.Security.AccessControl.FileSystemAccessRule]::new(${d}system, 'FullControl', 'ContainerInherit,ObjectInherit', 'None', 'Allow'),
      [System.Security.AccessControl.FileSystemAccessRule]::new(${d}admins, 'FullControl', 'ContainerInherit,ObjectInherit', 'None', 'Allow')
    )
    ${d}acl.Access | ForEach-Object { [void]${d}acl.RemoveAccessRule(${d}_) }
    foreach (${d}r in ${d}rules) { ${d}acl.AddAccessRule(${d}r) }
    Set-Acl -LiteralPath ${d}FolderPath -AclObject ${d}acl
    Record 'ntfs' ${d}true 'impostati'
    Write-Step "ACL NTFS Catalogo impostati."
  } catch {
    Fail-Step 40 ("Permessi NTFS non riusciti: " + ${d}_.Exception.Message)
  }
  Write-Step "Permessi lettura cartelle dati (niente ACL ricorsiva su Windows/Program Files)"
  try {
    try {
      ${d}usersSid = New-Object System.Security.Principal.SecurityIdentifier('S-1-5-32-545')
      ${d}usersGroup = ${d}usersSid.Translate([System.Security.Principal.NTAccount]).Value
      ${d}usersShort = (${d}usersGroup -split '\\')[-1]
      Add-LocalGroupMember -Group ${d}usersShort -Member ${d}UserName -ErrorAction SilentlyContinue
    } catch {}
    # Solo list/traverse sulla radice - MAI icacls /T su tutto il disco.
    cmd.exe /c ('icacls "' + ${d}ShareRoot + '" /grant ' + ${d}UserName + ':(RX) /C /Q >nul 2>&1') | Out-Null
    ${d}skip = @(
      'Windows', 'Program Files', 'Program Files (x86)', 'ProgramData',
      'System Volume Information', '${'$'}Recycle.Bin', 'Recovery', 'System Repair',
      'PerfLogs', 'Documents and Settings'
    )
    # Top-level (senza /T): rende visibili le cartelle in CIFS (AccessBased).
    Get-ChildItem -LiteralPath ${d}ShareRoot -Force -ErrorAction SilentlyContinue |
      Where-Object { ${d}_.PSIsContainer -and (${d}skip -notcontains ${d}_.Name) } |
      ForEach-Object {
        Write-Step ("  ACL dati: " + ${d}_.Name)
        [void](Invoke-PvmIcaclsGrant -path ${d}_.FullName -user ${d}UserName -TimeoutSec 20)
      }
    # Profili: RX sul profilo (visibilita AccessBased) SENZA /T sul profilo intero
    # (AppData/.android/... faceva hang). /T solo su cartelle media note, con timeout.
    ${d}usersRoots = @()
    foreach (${d}cand in @((Join-Path ${d}ShareRoot 'Users'), (Join-Path ${d}ShareRoot 'Utenti'))) {
      if (Test-Path -LiteralPath ${d}cand) { ${d}usersRoots += ${d}cand }
    }
    ${d}skipProfiles = @('Default','Public','Default User','All Users','Pubblica','Publico','defaultuser0')
    ${d}mediaNames = @(
      'Pictures','Immagini','Documents','Documenti','Desktop','Videos','Video',
      'Downloads','Download','Music','Musica','DCIM','Camera','Foto','Photos',
      'Dropbox','iCloud Drive','Google Drive','OneDrive','Saved Pictures','Immagini salvate'
    )
    foreach (${d}ur in ${d}usersRoots) {
      Get-ChildItem -LiteralPath ${d}ur -Force -ErrorAction SilentlyContinue |
        Where-Object { ${d}_.PSIsContainer -and (${d}skipProfiles -notcontains ${d}_.Name) } |
        ForEach-Object {
          ${d}profName = ${d}_.Name
          ${d}prof = ${d}_.FullName
          Write-Step ("  ACL profilo (radice, no /T): " + ${d}profName)
          [void](Invoke-PvmIcaclsGrant -path ${d}prof -user ${d}UserName -TimeoutSec 20)
          # Senza /T: veloce (v. tempi lunghi 0.16.96-97). RX+(OI)(CI) basta a elencare/aprire.
          foreach (${d}mn in ${d}mediaNames) {
            ${d}mp = Join-Path ${d}prof ${d}mn
            if (Test-Path -LiteralPath ${d}mp) {
              Write-Step ("  ACL media: " + ${d}profName + '\' + ${d}mn)
              [void](Invoke-PvmIcaclsGrant -path ${d}mp -user ${d}UserName -TimeoutSec 15)
            }
          }
          Get-ChildItem -LiteralPath ${d}prof -Force -Directory -ErrorAction SilentlyContinue |
            Where-Object { ${d}_.Name -like 'OneDrive*' } |
            ForEach-Object {
              Write-Step ("  ACL media: " + ${d}profName + '\' + ${d}_.Name)
              [void](Invoke-PvmIcaclsGrant -path ${d}_.FullName -user ${d}UserName -TimeoutSec 15)
            }
          # Nomi come in Esplora (Immagini/Documenti) anche via CIFS/SMB.
          Invoke-PvmLocalizedFolderAliases -profilePath ${d}prof -user ${d}UserName
        }
    }
    Record 'ntfs_disk' ${d}true 'rx_dati'
    Write-Step "ACL lettura su cartelle dati + profili/media ok."
  } catch {
    Write-Step "Avviso: permessi disco parziali; Catalogo ok, alcune cartelle foto potrebbero non comparire."
    Record 'ntfs_disk' ${d}false 'parziale'
  }

  Write-Host "Passo 6 di 6 - Rete, firewall e verifica" -ForegroundColor Cyan
  Write-Step "Rete e firewall (solo connessione con gateway predefinito)"
  try {
    ${d}defaultRoute = Get-NetRoute -DestinationPrefix '0.0.0.0/0' -ErrorAction SilentlyContinue |
      Sort-Object RouteMetric, InterfaceMetric |
      Select-Object -First 1
    if (${d}null -ne ${d}defaultRoute) {
      ${d}prof = Get-NetConnectionProfile -InterfaceIndex ${d}defaultRoute.InterfaceIndex -ErrorAction SilentlyContinue
      if (${d}null -ne ${d}prof -and ${d}prof.NetworkCategory -ne 'Private') {
        Write-Step ("Imposto profilo Privato su: " + ${d}prof.Name + " (era " + ${d}prof.NetworkCategory + ")")
        Set-NetConnectionProfile -InterfaceIndex ${d}defaultRoute.InterfaceIndex -NetworkCategory Private
      } elseif (${d}null -ne ${d}prof) {
        Write-Step ("Profilo gia Privato: " + ${d}prof.Name)
      }
    } else {
      Write-Step "Nessun gateway predefinito trovato: profilo di rete non modificato."
    }

    # Enable File and Printer Sharing only on Domain/Private (not Public).
    Get-NetFirewallRule -Group ${d}FirewallGroup -ErrorAction Stop | ForEach-Object {
      Set-NetFirewallRule -Name ${d}_.Name -Enabled True -Profile Domain,Private -ErrorAction Stop
    }
    Set-Service -Name LanmanServer -StartupType Automatic -ErrorAction Stop
    Start-Service LanmanServer -ErrorAction Stop
    Record 'firewall' ${d}true 'verificato'
    Write-Step "Firewall (condivisione file, profili Dominio/Privato) e servizio Server attivi."
  } catch {
    Fail-Step 50 ("Rete/firewall non riusciti: " + ${d}_.Exception.Message)
  }

  ${d}pc = ${d}env:COMPUTERNAME
  ${d}primaryIp = ${d}null
  ${d}allIps = @()
  try {
    ${d}allIps = @(Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue |
      Where-Object { ${d}_.IPAddress -notlike '127.*' -and ${d}_.IPAddress -notlike '169.254.*' } |
      Select-Object -ExpandProperty IPAddress -Unique)
    ${d}defaultRoute = Get-NetRoute -DestinationPrefix '0.0.0.0/0' -ErrorAction SilentlyContinue |
      Sort-Object RouteMetric, InterfaceMetric |
      Select-Object -First 1
    if (${d}null -ne ${d}defaultRoute) {
      ${d}primaryIp = Get-NetIPAddress -InterfaceIndex ${d}defaultRoute.InterfaceIndex -AddressFamily IPv4 -ErrorAction SilentlyContinue |
        Where-Object { ${d}_.IPAddress -notlike '127.*' -and ${d}_.IPAddress -notlike '169.254.*' } |
        Select-Object -ExpandProperty IPAddress -First 1
    }
    if (-not ${d}primaryIp -and ${d}allIps.Count -gt 0) { ${d}primaryIp = ${d}allIps[0] }
    if (${d}primaryIp -and (${d}allIps -notcontains ${d}primaryIp)) {
      ${d}allIps = @(${d}primaryIp) + ${d}allIps
    }
  } catch {}

  ${d}problemi = @()
  if (-not (Get-SmbShare -Name ${d}ShareName -ErrorAction SilentlyContinue)) { ${d}problemi += 'condivisione' }
  ${d}portOk = ${d}false
  try {
    ${d}portOk = (Test-NetConnection -ComputerName 'localhost' -Port 445 -WarningAction SilentlyContinue).TcpTestSucceeded
  } catch {}
  if (-not ${d}portOk) { ${d}problemi += 'porta 445' }
  ${d}fwOk = ${d}false
  try {
    # Enabled may be enum Enabled/Disabled (both truthy!) or True/False.
    ${d}fwOk = [bool](Get-NetFirewallRule -Group ${d}FirewallGroup -ErrorAction SilentlyContinue |
      Where-Object {
        ${d}_.Enabled -eq 'True' -or ${d}_.Enabled -eq 'Enabled' -or ${d}_.Enabled -eq ${d}true
      })
  } catch {}
  if (-not ${d}fwOk) { ${d}problemi += 'firewall' }
  if (${d}problemi.Count -gt 0) {
    Fail-Step 60 ('Verifica finale fallita: ' + (${d}problemi -join ', '))
  }
  Record 'verify' ${d}true 'ok'

  ${d}userHint = ("{0}\{1}" -f ${d}pc, ${d}UserName)
  ${d}ipText = $(if (${d}primaryIp) { [string]${d}primaryIp } else { '' })
  # Memorizza IP/User a Tutto pronto (file locale + annuncio Wi-Fi per l'app).
  ${d}result = [pscustomobject]@{
    app = 'MediaManager'
    version = 1
    status = 'completed'
    setupToken = ${d}SetupToken
    computerName = ${d}pc
    folderPath = ${d}FolderPath
    shareName = ${d}ShareName
    userName = ${d}UserName
    userFormatHint = ${d}userHint
    ipv4 = $(if (${d}primaryIp) { @(${d}primaryIp) } else { @() })
    steps = ${d}steps
    completedAt = (Get-Date).ToString('o')
  }
  ${d}result | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath ${d}ResultPath -Encoding UTF8
  ${d}desktop = [Environment]::GetFolderPath('Desktop')
  # BL-BT-02: overwrite unica versione sul Desktop (no_pw + DATI_CIFS + ripulisci legacy).
  if (${d}desktop) {
    Copy-Item -LiteralPath ${d}ResultPath -Destination (Join-Path ${d}desktop ${d}ResultFileName) -Force -ErrorAction SilentlyContinue
    Get-ChildItem -LiteralPath ${d}desktop -Filter 'MediaManager_setup_result*.json' -ErrorAction SilentlyContinue |
      Remove-Item -Force -ErrorAction SilentlyContinue
    Get-ChildItem -LiteralPath ${d}desktop -Filter 'MediaManager_cifs_bundle*.json' -ErrorAction SilentlyContinue |
      Remove-Item -Force -ErrorAction SilentlyContinue
    foreach (${d}cfgName in @('CONFIGURA.cmd','CONFIGURA.CMD','sup.ps1','sup.PS1')) {
      ${d}srcCfg = Join-Path ${d}PSScriptRoot ${d}cfgName
      if (Test-Path -LiteralPath ${d}srcCfg) {
        Copy-Item -LiteralPath ${d}srcCfg -Destination (Join-Path ${d}desktop ${d}cfgName) -Force -ErrorAction SilentlyContinue
      }
    }
  }

  Write-Host ""
  Write-Host "Tutto pronto" -ForegroundColor Green
  Write-Host ("PC:            {0}" -f ${d}pc)
  Write-Host ("Cartella Catalogo: {0}" -f ${d}FolderPath)
  Write-Host ("Disco condiviso:   {0}" -f ${d}ShareRoot)
  # Build UNC with [char]92 so backslashes are never doubled by escaping.
  Write-Host ("Condivisione:  " + [string][char]92 + [char]92 + ${d}pc + [char]92 + ${d}ShareName + " (= disco, non solo la cartella Catalogo)")
  if (${d}primaryIp) { Write-Host ("IP:            {0}" -f ${d}primaryIp) } else { Write-Host "IP:            (non rilevato; l'app usera il nome PC)" }
  Write-Host ("Utente:        {0}" -f ${d}userHint)
  Write-Host ("Password:      {0}" -f ${d}PlainPassword) -ForegroundColor Yellow
  Write-Host ""
  Write-Host "Password PROVVISORIA: cambiala in Windows e aggiorna Archivio sul tablet."
  Write-Host "AVVISO: se il PC va in sospensione, la cartella sparisce dalla rete."
  Write-Host ""
  Write-Host "PROSSIMO PASSO (USB):" -ForegroundColor Cyan
  Write-Host ("1) Sul Desktop trovi " + ${d}BundleFileName)
  Write-Host "2) Copia SOLO quel file nella cartella Download del Telefono via cavo USB"
  Write-Host ("3) Ignora " + ${d}ResultFileName + " (senza password)")
  Write-Host ("4) Nell'app: Importa dati CIFS e seleziona " + ${d}BundleFileName)
  Write-Host "Il Desktop tiene solo l'ultima versione (overwrite)."
  Write-Host ""

  # Bundle CIFS completo (con password) su volume script / Desktop = piano B USB.
  try {
    ${d}bundleObj = [ordered]@{
      v = 3
      m = ${d}AnnounceMagic
      t = ${d}SetupToken
      ip = ${d}ipText
      user = ${d}userHint
      password = ${d}PlainPassword
      share = ${d}ShareName
      pc = ${d}pc
      port = '445'
      folder = ${d}ShareName
      name = ${d}ShareName
      ips = (${d}allIps -join ',')
    }
    ${d}bundleJson = (${d}bundleObj | ConvertTo-Json -Compress)
    ${d}bundleName = ${d}BundleFileName
    ${d}bundleTargets = @(
      (Join-Path ${d}PSScriptRoot ${d}bundleName),
      (Join-Path ${d}ParentDir ${d}bundleName)
    )
    if (${d}desktop) { ${d}bundleTargets += (Join-Path ${d}desktop ${d}bundleName) }
    foreach (${d}bt in ${d}bundleTargets) {
      try {
        Set-Content -LiteralPath ${d}bt -Value ${d}bundleJson -Encoding UTF8 -Force
      } catch {}
    }
    try {
      ${d}deskClean = [Environment]::GetFolderPath('Desktop')
      if (${d}deskClean) { Invoke-PvmMaggiordomo -destDir ${d}deskClean }
      Invoke-PvmMaggiordomo -destDir ${d}PSScriptRoot
    } catch {}
    Write-Step ('Bundle CIFS scritto (' + ${d}bundleName + ')')
    Write-Host ("Bundle salvato anche sulla chiavetta/cartella di CONFIGURA: " + ${d}bundleName)
  } catch {
    Write-Step 'Bundle CIFS file non scritto.'
  }

  # Announce rete (fallback hotspot/LAN): HTTP+UDP ~5 min, bundle CON password.
  try {
    ${d}ipsText = (${d}allIps -join ',')
    ${d}payload = ([ordered]@{
      v = 3
      m = ${d}AnnounceMagic
      t = ${d}SetupToken
      ip = ${d}ipText
      user = ${d}userHint
      password = ${d}PlainPassword
      share = ${d}ShareName
      pc = ${d}pc
      port = '445'
      folder = ${d}ShareName
      name = ${d}ShareName
      ips = ${d}ipsText
    } | ConvertTo-Json -Compress)
    ${d}announcerPs1 = Join-Path ([System.IO.Path]::GetTempPath()) ('MediaManager_announce_' + ${d}SetupToken + '.ps1')
    ${d}annTpl = [System.Text.Encoding]::ASCII.GetString([System.Convert]::FromBase64String('$announcerB64'))
    ${d}annBody = ${d}annTpl.Replace('__PVM_PAYLOAD__', ${d}payload).Replace('__PVM_UDP__', [string]${d}AnnounceUdp).Replace('__PVM_HTTP__', [string]${d}AnnounceHttp)
    Set-Content -LiteralPath ${d}announcerPs1 -Value ${d}annBody -Encoding ASCII
    ${d}annProc = Start-Process -FilePath 'powershell.exe' -ArgumentList @('-NoProfile','-ExecutionPolicy','Bypass','-WindowStyle','Hidden','-File', ${d}announcerPs1) -WindowStyle Hidden -PassThru
    if (${d}annProc) {
      Write-Step ('Servizio bundle rete (fallback) avviato. IP=' + ${d}ipText)
    } else {
      Write-Step ('Servizio bundle rete non avviato: copia ' + ${d}BundleFileName + ' via USB sul Telefono (Download).')
    }
  } catch {
    Write-Step ('Servizio bundle rete non avviato: copia ' + ${d}BundleFileName + ' via USB sul Telefono (Download).')
  }

  ${d}ExitCode = 0
  exit 0
}
catch {
  ${d}msg = ${d}_.Exception.Message
  if (${d}_.InvocationInfo -and ${d}_.InvocationInfo.PositionMessage) {
    ${d}msg = ${d}msg + "`n" + ${d}_.InvocationInfo.PositionMessage
  }
  Write-Fail ${d}msg
  Record 'fatal' ${d}false ${d}msg
  try {
    ${d}fail = [pscustomobject]@{
      app = 'MediaManager'
      version = 1
      status = 'failed'
      exitCode = ${d}ExitCode
      folderPath = ${d}FolderPath
      shareName = ${d}ShareName
      userName = ${d}UserName
      steps = ${d}steps
      error = ${d}msg
      completedAt = (Get-Date).ToString('o')
    }
    ${d}fail | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath ${d}ResultPath -Encoding UTF8 -Force -ErrorAction SilentlyContinue
    ${d}desk = [Environment]::GetFolderPath('Desktop')
    if (${d}desk) {
      Copy-Item -LiteralPath ${d}ResultPath -Destination (Join-Path ${d}desk ${d}ResultFileName) -Force -ErrorAction SilentlyContinue
    }
  } catch {}
  if (${d}ExitCode -lt 10) { ${d}ExitCode = 1 }
  exit ${d}ExitCode
}
finally {
  ${d}PlainPassword = ${d}null
  ${d}secure = ${d}null
}
        """.trimIndent().replace("\n", "\r\n") + "\r\n"
    }
}
