param(
    [string]$Output = "survey-$(Get-Date -Format 'yyyyMMdd-HHmmss')",
    [string]$Adb = "adb"
)

# TOSS Phase 0 Device Survey
# Passive/read-only evidence collector for Windows/PowerShell.
# No unlock, flash, remount, root, package disable, SELinux change or sysfs write.

$ErrorActionPreference = "Continue"
New-Item -ItemType Directory -Force -Path $Output, "$Output/thermal", "$Output/input", "$Output/media" | Out-Null

function Collect-AdbShell {
    param([string]$Name, [string[]]$Command)
    Write-Host "[TOSS] collecting $Name"
    $target = Join-Path $Output $Name
    try {
        & $Adb shell @Command 2>&1 | Out-File -Encoding utf8 $target
    } catch {
        $_ | Out-File -Encoding utf8 $target
    }
}

try {
    $state = (& $Adb get-state 2>$null)
} catch {
    Write-Error "ADB not found. Install Android Platform Tools or pass -Adb with its path."
    exit 1
}

if ($state -ne "device") {
    Write-Error "No authorized ADB device detected. Run: adb devices"
    exit 2
}

Write-Host "[TOSS] Device Survey started"
& $Adb devices -l 2>&1 | Out-File -Encoding utf8 (Join-Path $Output "adb-devices.txt")
Collect-AdbShell "getprop.txt" @("getprop")
Collect-AdbShell "cpuinfo.txt" @("cat", "/proc/cpuinfo")
Collect-AdbShell "meminfo.txt" @("cat", "/proc/meminfo")
Collect-AdbShell "kernel.txt" @("uname", "-a")
Collect-AdbShell "device-tree-model.txt" @("cat", "/proc/device-tree/model")
Collect-AdbShell "df.txt" @("df", "-h")
Collect-AdbShell "mounts.txt" @("mount")
Collect-AdbShell "packages.txt" @("pm", "list", "packages", "-f")
Collect-AdbShell "services.txt" @("service", "list")
Collect-AdbShell "processes.txt" @("ps", "-A")
Collect-AdbShell "selinux.txt" @("getenforce")
Collect-AdbShell "features.txt" @("pm", "list", "features")
Collect-AdbShell "input/input-devices.txt" @("cat", "/proc/bus/input/devices")
Collect-AdbShell "thermal/thermal-class.txt" @("ls", "-la", "/sys/class/thermal")
Collect-AdbShell "thermal/hwmon-class.txt" @("ls", "-la", "/sys/class/hwmon")
Collect-AdbShell "media/media-codecs-vendor.txt" @("cat", "/vendor/etc/media_codecs.xml")
Collect-AdbShell "media/media-codecs-performance-vendor.txt" @("cat", "/vendor/etc/media_codecs_performance.xml")
Collect-AdbShell "media/media-codecs-system.txt" @("cat", "/system/etc/media_codecs.xml")
Collect-AdbShell "partitions-by-name.txt" @("ls", "-la", "/dev/block/by-name")
Collect-AdbShell "lshal.txt" @("lshal")

@"
TOSS Phase 0 Device Survey
Generated: $(Get-Date -Format o)

This directory contains passive evidence collected through ADB.
Missing commands/files are expected on some Android builds; error output is kept
because command/file availability is useful evidence too.

Manual/interactive work intentionally not automated here:
- getevent -l special-key mapping
- logcat while operating focus/keystone/HDMI
- Device Owner provisioning test
- media playback comparison / dropped frames
- network capture
- backup/restore validation
- firmware/partition writes
"@ | Out-File -Encoding utf8 (Join-Path $Output "README.txt")

Write-Host "[TOSS] Survey complete: $Output"
Write-Host "[TOSS] No firmware changes were intentionally performed."
