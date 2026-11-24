# Pulsar Connect

Independent Android companion application research project for the **2024+ Bajaj Pulsar N250**.

Current milestone: Phase 1 — read-only BLE discovery and GATT inspection.

## What is in this repository

- Native Android application scaffold (Kotlin + Jetpack Compose).
- BLE scanner with a bounded 12-second scan window.
- Device selection and BLE GATT connection.
- GATT service, characteristic, property and descriptor report.
- PulsarNav protocol research notes and N250 hardware test checklist.
- Architecture plan for the future navigation bridge.

## Safety and current limitations

This build is a **read-only discovery probe**. It does not write characteristics or send commands to the motorcycle. Perform discovery while parked. Never write to unknown or vehicle-control characteristics.

The UUIDs and 48-byte TBT frame described in `docs/REFERENCE_RESEARCH.md` are reverse-engineered reference leads from PulsarNav, not an official Bajaj specification and not yet verified against this particular N250 cluster.

## Open in Android Studio

1. Clone this repository.
2. Open the repository root in Android Studio.
3. Allow Gradle sync and install the configured Android SDK.
4. Run the `app` configuration on an Android phone with BLE.
5. Grant Nearby Devices permissions.
6. Turn on the bike while parked, scan, select the candidate device and inspect GATT.

A Gradle wrapper has not yet been committed. If Gradle is installed locally, generate one with `gradle wrapper`.

## Project details

- App name: Pulsar Connect
- Package: `dev.pulsarconnect`
- Minimum Android version: API 26
- Compile SDK: 35
- Language: Kotlin
- Bike profile: N250 2024+ (unverified until hardware discovery)

## Documentation

- [Reference research](docs/REFERENCE_RESEARCH.md)
- [Phase 1 hardware test plan](docs/PHASE_1_TEST_PLAN.md)
- [Architecture](docs/ARCHITECTURE.md)

## Next milestone

Collect the real bike's advertised BLE name and GATT database. Compare the discovered services with the research leads, determine the correct cluster profile, then design a safe and validated TBT-only protocol test.
