param(
    [System.Net.IPAddress]$ClientAddress = '10.139.9.209'
)

$ErrorActionPreference = 'Stop'

$identity = [Security.Principal.WindowsIdentity]::GetCurrent()
$principal = [Security.Principal.WindowsPrincipal]::new($identity)
if (-not $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    throw 'Run this script from an elevated PowerShell window.'
}

$javaPath = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe'
$javaBlockRules = foreach ($rule in Get-NetFirewallRule `
        -PolicyStore ActiveStore -Direction Inbound -Action Block) {
    if ($rule.Profile -notmatch 'Private') {
        continue
    }

    $applicationFilter = $rule | Get-NetFirewallApplicationFilter
    $portFilter = $rule | Get-NetFirewallPortFilter
    if ($applicationFilter.Program -ieq $javaPath -and $portFilter.Protocol -eq 'TCP') {
        $rule
    }
}
$javaBlockRules = @($javaBlockRules)
if ($javaBlockRules.Count -ne 1) {
    throw "Expected one Private TCP block rule for '$javaPath'; found $($javaBlockRules.Count). No changes made."
}

$devRules = @(
    Get-NetFirewallRule -DisplayName 'Ticketmaster dev 8080' -ErrorAction SilentlyContinue
)
$oldRules = @(
    Get-NetFirewallRule -DisplayName 'Flutter app test' -ErrorAction SilentlyContinue
)
if ($devRules.Count -gt 1 -or $oldRules.Count -gt 1 -or
    ($devRules.Count -eq 1 -and $oldRules.Count -eq 1)) {
    throw 'Expected at most one dev allow rule. Resolve duplicate rules before continuing.'
}

if ($devRules.Count -eq 1) {
    $allowRule = $devRules[0]
} elseif ($oldRules.Count -eq 1) {
    $allowRule = $oldRules[0]
} else {
    throw 'Neither the existing Flutter app test rule nor Ticketmaster dev 8080 rule was found. No changes made.'
}

$allowPorts = $allowRule | Get-NetFirewallPortFilter
if ($allowPorts.Protocol -ne 'TCP' -or @($allowPorts.LocalPort) -notcontains '8080') {
    throw "The selected allow rule does not match TCP 8080. No changes made."
}

$allowRule | Set-NetFirewallRule `
    -NewDisplayName 'Ticketmaster dev 8080' `
    -Profile Private `
    -ErrorAction Stop |
    Out-Null
$allowRule |
    Get-NetFirewallAddressFilter |
    Set-NetFirewallAddressFilter -RemoteAddress $ClientAddress -ErrorAction Stop |
    Out-Null

$allowPortFilter = $allowRule | Get-NetFirewallPortFilter -ErrorAction Stop
if ($allowPortFilter.Protocol -ne 'TCP' -or
    @($allowPortFilter.LocalPort) -notcontains '8080') {
    throw 'The dev allow rule no longer matches TCP 8080. The Java block rule was left unchanged.'
}

$allowAddressFilter = $allowRule | Get-NetFirewallAddressFilter -ErrorAction Stop
if (@($allowAddressFilter.RemoteAddress) -notcontains $ClientAddress.ToString()) {
    throw "The dev allow rule is not restricted to $ClientAddress. The Java block rule was left unchanged."
}

$javaBlockRule = Get-NetFirewallRule -Name $javaBlockRules[0].Name -ErrorAction Stop
if ($javaBlockRule.Enabled -eq 'True') {
    $javaBlockRule | Set-NetFirewallRule -Enabled False -ErrorAction Stop | Out-Null
}

$verifiedBlockRule = Get-NetFirewallRule -Name $javaBlockRule.Name -ErrorAction Stop
if ($verifiedBlockRule.Enabled -ne 'False') {
    throw 'Could not disable the conflicting Java block rule. The allow rule remains scoped to the approved client.'
}

Write-Output "Configured Ticketmaster dev 8080 for TCP 8080 from $ClientAddress on the Private profile."
Write-Output 'Disabled the conflicting Java TCP block rule on the Private profile. Windows inbound policy and the scoped allow rule still limit access.'
