package com.renatizzi.photovideomanager.data.hub

import java.security.SecureRandom

/**
 * Generates an idempotent Windows setup package (PowerShell + CMD launcher)
 * for MediaManager catalog folder + dedicated local SMB user.
 */
object WindowsCatalogSetupScript {
    const val DEFAULT_USER = "MediaManager"
    const val DEFAULT_SHARE = "MediaManagerCatalog"
    const val DEFAULT_FOLDER = "C:\\MediaManager\\Catalogo"
    /** File da avviare (doppio clic). Nome PPT: CONFIGURA. */
    const val CMD_FILE_NAME = "CONFIGURA.cmd"
    /** Supporto interno (non aprire). Nome PPT: sup. */
    const val PS1_FILE_NAME = "sup.ps1"
    /** Prefissi Download da ripulire (attuali + nomi legacy). */
    val DOWNLOAD_SCRUB_PREFIXES: List<String> = listOf(
        "CONFIGURA",
        "sup",
        "AVVIA_Configurazione_Catalogo",
        "_non_aprire_supporto_setup",
        "MediaManager_Setup_Catalogo",
    )
    const val RESULT_FILE_NAME = "MediaManager_setup_result.json"
    const val LOG_FILE_NAME = "MediaManager_setup.log"

    private val pathRegex =
        Regex("""^[A-Za-z]:\\(?:[^<>:"/\\|?*\r\n]+\\)*[^<>:"/\\|?*\r\n]*$""")
    private val shareRegex = Regex("""^[A-Za-z0-9][A-Za-z0-9._-]{0,79}$""")
    private val userRegex = Regex("""^[A-Za-z][A-Za-z0-9_-]{2,19}$""")

    data class Params(
        val folderPath: String = DEFAULT_FOLDER,
        val shareName: String = DEFAULT_SHARE,
        val userName: String = DEFAULT_USER,
        val temporaryPassword: String,
    )

    data class Package(
        val params: Params,
        val ps1FileName: String = PS1_FILE_NAME,
        val cmdFileName: String = CMD_FILE_NAME,
        val ps1Content: String,
        val cmdContent: String,
    )

    fun generatePassword(length: Int = 14): String {
        val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#%+"
        val random = SecureRandom()
        return buildString(length) {
            repeat(length) { append(alphabet[random.nextInt(alphabet.length)]) }
        }
    }

    fun validateFolderPath(path: String, requireDriveC: Boolean = false): String? {
        val trimmed = path.trim()
        if (trimmed.isEmpty()) return "Indica il percorso della cartella sul PC"
        if (!pathRegex.matches(trimmed)) {
            return "Percorso non valido. Esempio: C:\\MediaManager\\Catalogo"
        }
        if (requireDriveC && !trimmed.uppercase().startsWith("C:\\")) {
            return "In questa fase la cartella deve essere sul disco C: (esempio: C:\\MediaManager\\Catalogo)"
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

    fun build(params: Params): Package {
        validateFolderPath(params.folderPath)?.let { error(it) }
        validateShareName(params.shareName)?.let { error(it) }
        validateUserName(params.userName)?.let { error(it) }
        require(params.temporaryPassword.length >= 8) { "Password troppo corta" }

        val pkg = Package(
            params = params,
            ps1Content = buildPs1(params),
            cmdContent = buildCmd(),
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

    private fun buildCmd(): String = crlf(
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
        powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0$PS1_FILE_NAME"
        set ERR=%ERRORLEVEL%
        echo.
        if %ERR% NEQ 0 (
          echo  Configurazione NON completata. Codice: %ERR%
          echo  10=cartella 20=utente 30=condivisione 40=NTFS 50=rete/firewall 60=verifica
        ) else (
          echo  Tutto pronto. Puoi chiudere questa finestra e tornare all'app.
        )
        echo.
        pause
        exit /b %ERR%
        """.trimIndent(),
    )

    private fun psQuote(value: String): String =
        "'" + value.replace("'", "''") + "'"

    private fun buildPs1(params: Params): String {
        val folder = psQuote(params.folderPath.trim())
        val share = psQuote(params.shareName.trim())
        val user = psQuote(params.userName.trim())
        val password = psQuote(params.temporaryPassword)
        val resultName = psQuote(RESULT_FILE_NAME)
        val logName = psQuote(LOG_FILE_NAME)
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
${d}ResultFileName = $resultName
${d}LogFileName    = $logName
${d}FirewallGroup  = '@FirewallAPI.dll,-28502'
${d}ExitCode       = 1

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

try {
  Write-Host ""
  Write-Host "=== MediaManager: configurazione Catalogo ===" -ForegroundColor Cyan
  Write-Host "Passo 1 di 6 - Controlli preliminari" -ForegroundColor Cyan
  Write-Host ""

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

  Write-Host "Passo 4 di 6 - Condivisione SMB" -ForegroundColor Cyan
  Write-Step "Condivisione SMB: ${d}ShareName"
  try {
    ${d}shareObj = Get-SmbShare -Name ${d}ShareName -ErrorAction SilentlyContinue
    if (${d}null -eq ${d}shareObj) {
      New-SmbShare -Name ${d}ShareName -Path ${d}FolderPath -FullAccess ${d}UserName -FolderEnumerationMode AccessBased | Out-Null
      Record 'share' ${d}true 'creata'
      Write-Step "Condivisione creata."
    } else {
      # Modifica percorso: stessa condivisione gia usata da un Catalogo precedente.
      # Non fallire: rimuovi e ricrea sul nuovo FolderPath (i file vecchi restano al loro posto).
      ${d}existingPath = ([string]${d}shareObj.Path).TrimEnd([char]92)
      ${d}wantedPath = ([string]${d}FolderPath).TrimEnd([char]92)
      if (${d}existingPath -ieq ${d}wantedPath) {
        Grant-SmbShareAccess -Name ${d}ShareName -AccountName ${d}UserName -AccessRight Full -Force | Out-Null
        Record 'share' ${d}true 'aggiornata'
        Write-Step "Condivisione gia presente: permessi aggiornati."
      } else {
        Write-Step ("Condivisione gia su '${d}existingPath': la ripunto a '${d}FolderPath'.")
        Remove-SmbShare -Name ${d}ShareName -Force
        New-SmbShare -Name ${d}ShareName -Path ${d}FolderPath -FullAccess ${d}UserName -FolderEnumerationMode AccessBased | Out-Null
        Record 'share' ${d}true 'ripruntata'
        Write-Step "Condivisione ripuntata al nuovo percorso."
      }
    }
    Get-SmbShareAccess -Name ${d}ShareName | Where-Object { ${d}_.AccountName -match 'Everyone|Tutti' } | ForEach-Object {
      Revoke-SmbShareAccess -Name ${d}ShareName -AccountName ${d}_.AccountName -Force -ErrorAction SilentlyContinue
    }
  } catch {
    Fail-Step 30 ("Condivisione SMB non riuscita: " + ${d}_.Exception.Message)
  }

  Write-Host "Passo 5 di 6 - Permessi NTFS" -ForegroundColor Cyan
  Write-Step "Permessi NTFS sulla cartella"
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
    Write-Step "ACL NTFS impostati."
  } catch {
    Fail-Step 40 ("Permessi NTFS non riusciti: " + ${d}_.Exception.Message)
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
  try {
    ${d}defaultRoute = Get-NetRoute -DestinationPrefix '0.0.0.0/0' -ErrorAction SilentlyContinue |
      Sort-Object RouteMetric, InterfaceMetric |
      Select-Object -First 1
    if (${d}null -ne ${d}defaultRoute) {
      ${d}primaryIp = Get-NetIPAddress -InterfaceIndex ${d}defaultRoute.InterfaceIndex -AddressFamily IPv4 -ErrorAction SilentlyContinue |
        Where-Object { ${d}_.IPAddress -notlike '127.*' -and ${d}_.IPAddress -notlike '169.254.*' } |
        Select-Object -ExpandProperty IPAddress -First 1
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
  ${d}result = [pscustomobject]@{
    app = 'MediaManager'
    version = 1
    status = 'completed'
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
  if (${d}desktop) {
    Copy-Item -LiteralPath ${d}ResultPath -Destination (Join-Path ${d}desktop ${d}ResultFileName) -Force -ErrorAction SilentlyContinue
  }

  Write-Host ""
  Write-Host "Tutto pronto" -ForegroundColor Green
  Write-Host ("PC:            {0}" -f ${d}pc)
  Write-Host ("Cartella:      {0}" -f ${d}FolderPath)
  # Build UNC with [char]92 so backslashes are never doubled by escaping.
  Write-Host ("Condivisione:  " + [string][char]92 + [char]92 + ${d}pc + [char]92 + ${d}ShareName)
  if (${d}primaryIp) { Write-Host ("IP:            {0}" -f ${d}primaryIp) }
  Write-Host ("Utente SMB:    {0}" -f ${d}userHint)
  Write-Host ("Password:      {0}" -f ${d}PlainPassword) -ForegroundColor Yellow
  Write-Host ""
  Write-Host "Password PROVVISORIA: cambiala in Windows e aggiorna Archivio sul tablet."
  Write-Host ("Esito (fuori dalla condivisione): {0}" -f ${d}ResultPath)
  Write-Host ("Log: {0}" -f ${d}LogPath)
  Write-Host "AVVISO: se il PC va in sospensione, la cartella sparisce dalla rete."
  Write-Host "Torna all'app MediaManager e continua il Wizard."
  Write-Host ""

  # Rimuove lo .ps1 (contiene la password) dopo successo.
  try {
    ${d}self = ${d}MyInvocation.MyCommand.Path
    if (${d}self -and (Test-Path -LiteralPath ${d}self)) {
      Remove-Item -LiteralPath ${d}self -Force -ErrorAction SilentlyContinue
      Write-Step "File di supporto eliminato dal PC (conteneva la password)."
    }
  } catch {}

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
    ${d}fail | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath ${d}ResultPath -Encoding UTF8 -ErrorAction SilentlyContinue
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
