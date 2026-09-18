# ============================================================
#  LAN Access Helper
#
#  Prints the URLs to open from a phone / another computer on the
#  same Wi-Fi, and self-tests each one through the LAN IP.
#
#  Launched by 局域网访问.bat (double-click that, not this file).
#
#  Why this is a .ps1 and not plain batch: escaping PowerShell
#  pipelines (^|) inside a `for /f "usebackq"` block is fragile and
#  silently passes the caret through. Keeping the logic here avoids it.
# ============================================================

$ErrorActionPreference = 'Continue'

function Get-LanIp {
    # Prefer the adapter that actually has Internet connectivity, so
    # virtual adapters (VMware / VirtualBox / vEthernet / ZeroTier)
    # don't win by accident.
    try {
        $profiles = Get-NetConnectionProfile -ErrorAction Stop
        $online = $profiles | Where-Object { $_.IPv4Connectivity -eq 'Internet' } | Select-Object -First 1
        if ($online) {
            $ip = Get-NetIPAddress -InterfaceIndex $online.InterfaceIndex -AddressFamily IPv4 -ErrorAction Stop |
                Select-Object -First 1 -ExpandProperty IPAddress
            if ($ip) { return $ip }
        }
    } catch { }

    # Fallback: any non-loopback, non-link-local IPv4
    try {
        $ip = Get-NetIPAddress -AddressFamily IPv4 -ErrorAction Stop |
            Where-Object { $_.IPAddress -notlike '127.*' -and $_.IPAddress -notlike '169.254.*' } |
            Select-Object -First 1 -ExpandProperty IPAddress
        if ($ip) { return $ip }
    } catch { }

    return $null
}

function Test-Url {
    param([string]$Name, [string]$Url)
    try {
        $r = Invoke-WebRequest -Uri $Url -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
        if ($r.StatusCode -eq 200) {
            Write-Host ("  [ OK ] {0}  {1}" -f $Name, $Url) -ForegroundColor Green
            return $true
        }
    } catch { }
    Write-Host ("  [FAIL] {0}  {1}" -f $Name, $Url) -ForegroundColor Red
    return $false
}

Write-Host "============================================"
Write-Host "  LAN Access Addresses"
Write-Host "============================================"
Write-Host ""

$lanIp = Get-LanIp
if (-not $lanIp) {
    Write-Host "[ERROR] Could not detect a LAN IP." -ForegroundColor Red
    Write-Host "        Are you connected to Wi-Fi or a network cable?"
    Write-Host ""
    exit 1
}

Write-Host ("  This computer : {0}" -f $lanIp)
Write-Host ""
Write-Host "  ---------- Open these on your PHONE ----------" -ForegroundColor Cyan
Write-Host ""
Write-Host ("  Tenant / Owner (H5)  :  http://{0}:8081/" -f $lanIp)
Write-Host ("  Admin console        :  http://{0}/" -f $lanIp)
Write-Host ("  Backend API (direct) :  http://{0}:48080/admin-api" -f $lanIp)
Write-Host ""
Write-Host "  ---------- Accounts ----------" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Admin  : admin / admin123"
Write-Host "  Owner  : 13800000002 / 123456   (has a rented-out house)"
Write-Host "  Tenant : 13900000001 / 123456   (has an active contract)"
Write-Host ""
Write-Host "  -------------------------------"
Write-Host "  Start everything first:"
Write-Host "    start.bat   -> Redis + backend(48080) + admin(80)"
Write-Host "    H5 launcher -> H5(8081)"
Write-Host ""

Write-Host "  ---------- Self test (through the LAN IP) ----------" -ForegroundColor Cyan
Write-Host ""
$results = @()
$results += Test-Url "backend 48080" ("http://{0}:48080/admin-api/rental/tenant-app/house/page?pageNo=1&pageSize=1" -f $lanIp)
$results += Test-Url "H5      8081" ("http://{0}:8081/" -f $lanIp)
$results += Test-Url "admin   80  " ("http://{0}/" -f $lanIp)
Write-Host ""

if ($results -contains $false) {
    Write-Host "  Some services are not reachable. Checklist:" -ForegroundColor Yellow
    Write-Host "    1. Are they actually started? (see above)"
    Write-Host "    2. Windows Defender Firewall must allow inbound for"
    Write-Host "       java.exe and node.exe on the CURRENT network profile."
    Write-Host "       Check with:  Get-NetConnectionProfile"
    Write-Host "       This machine's rules allow them on the Public profile."
    Write-Host "    3. If the profile is Private/Domain, add a rule:"
    Write-Host "         New-NetFirewallRule -DisplayName 'Rental 8081' -Direction Inbound -LocalPort 8081 -Protocol TCP -Action Allow"
    Write-Host ""
} else {
    Write-Host "  All three are reachable. Open the H5 URL on your phone." -ForegroundColor Green
    Write-Host ""
}
