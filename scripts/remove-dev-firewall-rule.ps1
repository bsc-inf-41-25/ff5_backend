$ErrorActionPreference = 'Stop'

$identity = [Security.Principal.WindowsIdentity]::GetCurrent()
$principal = [Security.Principal.WindowsPrincipal]::new($identity)
if (-not $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    throw 'Run this script from an elevated PowerShell window.'
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
    $rule = $devRules[0]
    $rule | Set-NetFirewallRule `
        -NewDisplayName 'Flutter app test' `
        -Profile Any `
        -ErrorAction Stop |
        Out-Null
    $rule |
        Get-NetFirewallAddressFilter -ErrorAction Stop |
        Set-NetFirewallAddressFilter -RemoteAddress Any -ErrorAction Stop |
        Out-Null
    Write-Output 'Restored the original Flutter app test allow rule scope.'
} else {
    Write-Output 'The Ticketmaster dev 8080 firewall rule was not found; no allow rule was changed.'
}

$javaPath = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe'
$javaBlockRules = foreach ($candidate in Get-NetFirewallRule `
        -PolicyStore ActiveStore -Direction Inbound -Action Block) {
    if ($candidate.Profile -notmatch 'Private') {
        continue
    }

    $applicationFilter = $candidate | Get-NetFirewallApplicationFilter
    $portFilter = $candidate | Get-NetFirewallPortFilter
    if ($applicationFilter.Program -ieq $javaPath -and $portFilter.Protocol -eq 'TCP') {
        $candidate
    }
}
$javaBlockRules = @($javaBlockRules)
if ($javaBlockRules.Count -ne 1) {
    throw "Expected one Private TCP block rule for '$javaPath'; found $($javaBlockRules.Count)."
}

$blockRule = Get-NetFirewallRule -Name $javaBlockRules[0].Name -ErrorAction Stop
if ($blockRule.Enabled -ne 'True') {
    $blockRule | Set-NetFirewallRule -Enabled True -ErrorAction Stop | Out-Null
}

$verifiedBlockRule = Get-NetFirewallRule -Name $blockRule.Name -ErrorAction Stop
if ($verifiedBlockRule.Enabled -ne 'True') {
    throw 'Could not re-enable the Java TCP block rule.'
}

Write-Output 'Re-enabled the original Java TCP block rule on the Private profile.'
