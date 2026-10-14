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
    const val PS1_FILE_NAME = "MediaManager_Setup_Catalogo.ps1"
    const val CMD_FILE_NAME = "MediaManager_Setup_Catalogo.cmd"
    const val RESULT_FILE_NAME = "MediaManager_setup_result.json"

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

    fun validateFolderPath(path: String): String? {
        val trimmed = path.trim()
        if (trimmed.isEmpty()) return "Indica il percorso della cartella sul PC"
        if (!pathRegex.matches(trimmed)) {
            return "Percorso non valido. Esempio: C:\\MediaManager\\Catalogo"
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

        return Package(
            params = params,
            ps1Content = buildPs1(params),
            cmdContent = buildCmd(),
        )
    }

    private fun buildCmd(): String = """
        @echo off
        setlocal
        title MediaManager - configurazione Catalogo
        echo.
        echo  MediaManager: prepara cartella Catalogo e accesso SMB
        echo  (account dedicato, senza privilegi di amministratore permanenti)
        echo.
        echo  Verra richiesta l'autorizzazione Amministratore una sola volta.
        echo.
        powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0$PS1_FILE_NAME"
        set ERR=%ERRORLEVEL%
        echo.
        if %ERR% NEQ 0 (
          echo  Configurazione NON completata. Codice: %ERR%
        ) else (
          echo  Operazione terminata. Puoi chiudere questa finestra e tornare all'app.
        )
        echo.
        pause
        exit /b %ERR%
    """.trimIndent() + "\r\n"

    private fun psQuote(value: String): String =
        "'" + value.replace("'", "''") + "'"

    private fun buildPs1(params: Params): String {
        val folder = psQuote(params.folderPath.trim())
        val share = psQuote(params.shareName.trim())
        val user = psQuote(params.userName.trim())
        val password = psQuote(params.temporaryPassword)
        val resultName = psQuote(RESULT_FILE_NAME)
        // Escape PowerShell `$` for Kotlin string templates.
        val d = "${'$'}"

        return """
# MediaManager - Setup Catalogo su PC Windows (idempotente)
# Generato dall'app. Non condividere questo file: contiene una password provvisoria.
# Target: Windows 10/11. Eseguire come Amministratore (il .cmd lo richiede).

#Requires -RunAsAdministrator
${d}ErrorActionPreference = 'Stop'
${d}FolderPath = $folder
${d}ShareName  = $share
${d}UserName   = $user
${d}PlainPassword = $password
${d}ResultFileName = $resultName

function Write-Step(${d}msg) { Write-Host ("[{0}] {1}" -f (Get-Date -Format 'HH:mm:ss'), ${d}msg) }
function Write-Fail(${d}msg) { Write-Host ("ERRORE: {0}" -f ${d}msg) -ForegroundColor Red }

${d}steps = New-Object System.Collections.Generic.List[object]
function Record(${d}name, ${d}ok, ${d}detail) {
  ${d}steps.Add([pscustomobject]@{ name = ${d}name; ok = [bool]${d}ok; detail = [string]${d}detail }) | Out-Null
}

try {
  Write-Host ""
  Write-Host "=== MediaManager: configurazione Catalogo ===" -ForegroundColor Cyan
  Write-Host ""

  Write-Step "Cartella Catalogo: ${d}FolderPath"
  if (-not (Test-Path -LiteralPath ${d}FolderPath)) {
    New-Item -ItemType Directory -Path ${d}FolderPath -Force | Out-Null
    Record 'folder' ${d}true 'creata'
    Write-Step "Cartella creata."
  } else {
    Record 'folder' ${d}true 'gia_presente'
    Write-Step "Cartella gia presente (nessuna sovrascrittura)."
  }

  Write-Step "Account locale: ${d}UserName"
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
  try { Remove-LocalGroupMember -Group 'Administrators' -Member ${d}UserName -ErrorAction SilentlyContinue } catch {}

  Write-Step "Condivisione SMB: ${d}ShareName"
  ${d}shareObj = Get-SmbShare -Name ${d}ShareName -ErrorAction SilentlyContinue
  if (${d}null -eq ${d}shareObj) {
    New-SmbShare -Name ${d}ShareName -Path ${d}FolderPath -FullAccess ${d}UserName -FolderEnumerationMode AccessBased | Out-Null
    Record 'share' ${d}true 'creata'
    Write-Step "Condivisione creata."
  } else {
    if (${d}shareObj.Path -ne ${d}FolderPath) {
      throw ("La condivisione '${d}ShareName' punta gia a '${d}(${d}shareObj.Path)'. Rimuovila o scegli un altro nome.")
    }
    Grant-SmbShareAccess -Name ${d}ShareName -AccountName ${d}UserName -AccessRight Full -Force | Out-Null
    Record 'share' ${d}true 'aggiornata'
    Write-Step "Condivisione gia presente: permessi aggiornati."
  }
  Get-SmbShareAccess -Name ${d}ShareName | Where-Object { ${d}_.AccountName -match 'Everyone|Tutti' } | ForEach-Object {
    Revoke-SmbShareAccess -Name ${d}ShareName -AccountName ${d}_.AccountName -Force -ErrorAction SilentlyContinue
  }

  Write-Step "Permessi NTFS sulla cartella"
  ${d}acl = Get-Acl -LiteralPath ${d}FolderPath
  ${d}acl.SetAccessRuleProtection(${d}true, ${d}false)
  ${d}rules = @(
    New-Object System.Security.AccessControl.FileSystemAccessRule(${d}UserName,'Modify','ContainerInherit,ObjectInherit','None','Allow'),
    New-Object System.Security.AccessControl.FileSystemAccessRule('SYSTEM','FullControl','ContainerInherit,ObjectInherit','None','Allow'),
    New-Object System.Security.AccessControl.FileSystemAccessRule('Administrators','FullControl','ContainerInherit,ObjectInherit','None','Allow')
  )
  ${d}acl.Access | ForEach-Object { [void]${d}acl.RemoveAccessRule(${d}_) }
  foreach (${d}r in ${d}rules) { ${d}acl.AddAccessRule(${d}r) }
  Set-Acl -LiteralPath ${d}FolderPath -AclObject ${d}acl
  Record 'ntfs' ${d}true 'impostati'
  Write-Step "ACL NTFS impostati."

  Write-Step "Rete e firewall (profilo Privato)"
  try {
    Get-NetConnectionProfile | Where-Object { ${d}_.NetworkCategory -ne 'Private' } | ForEach-Object {
      Set-NetConnectionProfile -InterfaceIndex ${d}_.InterfaceIndex -NetworkCategory Private -ErrorAction SilentlyContinue
    }
  } catch {}
  try {
    Enable-NetFirewallRule -DisplayGroup 'File and Printer Sharing' -ErrorAction SilentlyContinue
    Enable-NetFirewallRule -DisplayGroup 'Condivisione file e stampanti' -ErrorAction SilentlyContinue
  } catch {}
  try {
    Set-Service -Name LanmanServer -StartupType Automatic -ErrorAction SilentlyContinue
    Start-Service LanmanServer -ErrorAction SilentlyContinue
  } catch {}
  Record 'firewall' ${d}true 'verificato'
  Write-Step "Servizio file sharing verificato."

  ${d}pc = ${d}env:COMPUTERNAME
  ${d}ips = @()
  try {
    ${d}ips = Get-NetIPAddress -AddressFamily IPv4 |
      Where-Object { ${d}_.IPAddress -notlike '127.*' -and ${d}_.PrefixOrigin -ne 'WellKnown' } |
      Select-Object -ExpandProperty IPAddress
  } catch {}

  ${d}result = [pscustomobject]@{
    app = 'MediaManager'
    version = 1
    status = 'completed'
    computerName = ${d}pc
    folderPath = ${d}FolderPath
    shareName = ${d}ShareName
    userName = ${d}UserName
    userFormatHint = ("{0}\\{1}" -f ${d}pc, ${d}UserName)
    ipv4 = @(${d}ips)
    steps = ${d}steps
    completedAt = (Get-Date).ToString('o')
  }
  ${d}resultPath = Join-Path ${d}FolderPath ${d}ResultFileName
  ${d}result | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath ${d}resultPath -Encoding UTF8
  ${d}desktop = [Environment]::GetFolderPath('Desktop')
  if (${d}desktop) {
    Copy-Item -LiteralPath ${d}resultPath -Destination (Join-Path ${d}desktop ${d}ResultFileName) -Force -ErrorAction SilentlyContinue
  }

  Write-Host ""
  Write-Host "CONFIGURAZIONE COMPLETATA" -ForegroundColor Green
  Write-Host ("PC:            {0}" -f ${d}pc)
  Write-Host ("Cartella:      {0}" -f ${d}FolderPath)
  Write-Host ("Condivisione:  \\{0}\{1}" -f ${d}pc, ${d}ShareName)
  if (${d}ips.Count -gt 0) { Write-Host ("IP:            {0}" -f (${d}ips -join ', ')) }
  Write-Host ("Utente SMB:    {0}\{1}" -f ${d}pc, ${d}UserName)
  Write-Host ("Password:      {0}" -f ${d}PlainPassword) -ForegroundColor Yellow
  Write-Host ""
  Write-Host "Questa password e PROVVISORIA. Cambiala in Windows e aggiorna le credenziali in Archivio sul tablet."
  Write-Host ("Esito salvato in: {0}" -f ${d}resultPath)
  Write-Host "Torna all'app MediaManager e continua il Wizard."
  Write-Host ""
  exit 0
}
catch {
  Write-Fail ${d}_.Exception.Message
  Record 'fatal' ${d}false (${d}_.Exception.Message)
  try {
    ${d}fail = [pscustomobject]@{
      app = 'MediaManager'
      version = 1
      status = 'failed'
      folderPath = ${d}FolderPath
      shareName = ${d}ShareName
      userName = ${d}UserName
      steps = ${d}steps
      error = ${d}_.Exception.Message
      completedAt = (Get-Date).ToString('o')
    }
    ${d}failPath = Join-Path ([Environment]::GetFolderPath('Desktop')) ${d}ResultFileName
    ${d}fail | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath ${d}failPath -Encoding UTF8 -ErrorAction SilentlyContinue
  } catch {}
  exit 1
}
finally {
  ${d}PlainPassword = ${d}null
  ${d}secure = ${d}null
}
        """.trimIndent().replace("\n", "\r\n") + "\r\n"
    }
}
