$ErrorActionPreference = 'Stop'

$addresses = @(
    Get-NetIPAddress -AddressFamily IPv4 |
        Where-Object { $_.IPAddress -notlike '127.*' } |
        Sort-Object InterfaceAlias, IPAddress
)

Write-Output 'IPv4 addresses:'
if ($addresses.Count -eq 0) {
    Write-Output '  No non-loopback IPv4 addresses found.'
} else {
    $addresses |
        Select-Object InterfaceAlias, IPAddress, PrefixLength |
        Format-Table -AutoSize
}

Write-Output 'TCP 8080 listeners:'
$listeners = @(Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue)
if ($listeners.Count -eq 0) {
    Write-Output '  No listener found on TCP 8080.'
} else {
    $listeners |
        Select-Object LocalAddress, LocalPort, OwningProcess |
        Format-Table -AutoSize
}

Write-Output 'Enabled inbound firewall rules whose port filter includes 8080 or Any:'
$firewallRules = @()
$firewallReadSucceeded = $true
try {
    foreach ($portFilter in Get-NetFirewallPortFilter -ErrorAction Stop) {
        $localPorts = @($portFilter.LocalPort)
        if ($portFilter.Protocol -in @('TCP', '6', 'Any') -and
            ($localPorts -contains '8080' -or $localPorts -contains 'Any')) {
            $firewallRules += $portFilter | Get-NetFirewallRule |
                Where-Object { $_.Enabled -eq 'True' -and $_.Direction -eq 'Inbound' } |
                Select-Object DisplayName, Direction, Action, Enabled, Profile
        }
    }
} catch {
    $firewallReadSucceeded = $false
    Write-Warning "Could not read Windows Firewall port filters: $($_.Exception.Message)"
    Write-Warning 'Run this script in an elevated PowerShell window to inspect firewall rules.'
}

if ($firewallRules) {
    $firewallRules | Sort-Object DisplayName -Unique | Format-Table -AutoSize
} elseif ($firewallReadSucceeded) {
    Write-Output '  No matching enabled inbound rules found.'
}

Write-Output 'From the other PC, run:'
$wifiAddress = $addresses |
    Where-Object { $_.InterfaceAlias -eq 'Wi-Fi' } |
    Select-Object -First 1 -ExpandProperty IPAddress
if ($wifiAddress) {
    Write-Output "  Test-NetConnection $wifiAddress -Port 8080"
} else {
    Write-Output '  Test-NetConnection <this-PC-IPv4> -Port 8080'
}
