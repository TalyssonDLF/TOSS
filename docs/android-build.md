# Android APK build

The first hardware target is the HCS350 running Android 11 / API 30.

## CI

Every push and pull request to `main` runs unit tests and builds the debug APK. Successful runs publish the `toss-home-debug` artifact.

## Local build

Requirements:

- JDK 17
- Android SDK platform 35
- Android build-tools 34.0.0
- Gradle 8.9

From the repository root:

```text
gradle --no-daemon test
gradle --no-daemon :apps:toss-home:assembleDebug
```

The APK is generated under:

```text
apps/toss-home/build/outputs/apk/debug/
```

For the HCS350 lab target, install only the normal debug APK during the first test. The privileged focus/keystone bridge is intentionally a separate deployment step and must not be introduced until its permissions and rollback path are validated.
