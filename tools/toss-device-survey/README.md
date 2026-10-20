# TOSS Device Survey

Read-only-first ADB evidence collector for TOSS Phase 0.

## Requirements

- Android Platform Tools (`adb`)
- an Android target with ADB enabled and authorized

## Windows / PowerShell

```powershell
adb devices
.\survey.ps1
```

Custom output folder:

```powershell
.\survey.ps1 -Output .\survey-hy320
```

## Linux / macOS

```bash
adb devices
chmod +x survey.sh
./survey.sh
```

The collector records command errors as evidence because vendor Android builds frequently omit tools or files. It does not attempt root, unlock, flashing, remounting, package disabling, SELinux changes or sysfs writes.

Interactive tests such as `getevent -l`, logcat observation during focus/keystone/HDMI actions, Device Owner provisioning, media playback comparison and recovery validation are intentionally separate manual steps.

## Privacy

Survey output can contain serials, build fingerprints, network/device identifiers or vendor-specific information. Review collected evidence before committing it. Generated `survey-*` directories are ignored by the repository `.gitignore` by default.
