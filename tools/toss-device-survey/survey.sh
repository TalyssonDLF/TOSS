#!/usr/bin/env bash
set -u

# TOSS Phase 0 Device Survey
# Passive/read-only evidence collector for Android devices reachable through ADB.
# This script intentionally does not unlock, flash, remount, root, disable packages,
# change SELinux, write sysfs nodes, or modify Android settings.

ADB="${ADB:-adb}"
STAMP="$(date +%Y%m%d-%H%M%S)"
OUT="${1:-survey-$STAMP}"

mkdir -p "$OUT" "$OUT/thermal" "$OUT/input" "$OUT/media"

log() { printf '[TOSS] %s\n' "$*"; }
collect() {
  local name="$1"; shift
  log "collecting $name"
  "$@" >"$OUT/$name" 2>&1 || true
}
sh_collect() {
  local name="$1"; shift
  log "collecting $name"
  "$ADB" shell "$@" >"$OUT/$name" 2>&1 || true
}

if ! command -v "$ADB" >/dev/null 2>&1; then
  echo "ADB not found. Install Android Platform Tools or set ADB=/path/to/adb." >&2
  exit 1
fi

STATE="$($ADB get-state 2>/dev/null || true)"
if [ "$STATE" != "device" ]; then
  echo "No authorized ADB device detected. Run: adb devices" >&2
  exit 2
fi

log "TOSS Device Survey started"
collect "adb-devices.txt" "$ADB" devices -l
sh_collect "getprop.txt" getprop
sh_collect "cpuinfo.txt" cat /proc/cpuinfo
sh_collect "meminfo.txt" cat /proc/meminfo
sh_collect "kernel.txt" uname -a
sh_collect "device-tree-model.txt" cat /proc/device-tree/model
sh_collect "df.txt" df -h
sh_collect "mounts.txt" mount
sh_collect "packages.txt" pm list packages -f
sh_collect "services.txt" service list
sh_collect "processes.txt" ps -A
sh_collect "selinux.txt" getenforce
sh_collect "features.txt" pm list features
sh_collect "input/input-devices.txt" cat /proc/bus/input/devices
sh_collect "thermal/thermal-class.txt" ls -la /sys/class/thermal
sh_collect "thermal/hwmon-class.txt" ls -la /sys/class/hwmon
sh_collect "media/media-codecs-vendor.txt" cat /vendor/etc/media_codecs.xml
sh_collect "media/media-codecs-performance-vendor.txt" cat /vendor/etc/media_codecs_performance.xml
sh_collect "media/media-codecs-system.txt" cat /system/etc/media_codecs.xml
sh_collect "partitions-by-name.txt" ls -la /dev/block/by-name
sh_collect "lshal.txt" lshal

cat >"$OUT/README.txt" <<EOF
TOSS Phase 0 Device Survey
Generated: $(date -Iseconds 2>/dev/null || date)

This directory contains passive evidence collected through ADB.
Missing commands/files are expected on some Android builds; their error output is
preserved because availability itself is useful evidence.

NOT collected automatically because they require interactive/manual analysis:
- getevent -l special-key mapping
- logcat while operating focus/keystone/HDMI
- Device Owner provisioning test
- media playback comparison / dropped frames
- network capture
- backup/restore validation
- firmware/partition writes
EOF

log "Survey complete: $OUT"
log "No firmware changes were intentionally performed."
