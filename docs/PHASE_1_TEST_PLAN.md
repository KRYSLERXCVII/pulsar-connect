# Phase 1 — N250 hardware validation

## Safety
- Perform all tests while parked.
- First session is read-only: no characteristic writes.
- Never test unknown characteristics.
- Never write to ECU, ABS, traction control, immobilizer, security or other vehicle-control functions.
- Keep BLE addresses private and redact them from shared logs.
- Stop if the cluster reports an error or normal behavior changes.

## Test A — Discovery
- [ ] Turn ignition on and enable bike Bluetooth as described in the owner's manual.
- [ ] Open Pulsar Connect and grant Nearby Devices permission.
- [ ] Scan for 12 seconds.
- [ ] Record candidate advertised name and RSSI.
- [ ] Repeat three times and note name/address stability (do not publish address).
- [ ] If not visible, verify discoverability/pairing state using the owner's manual; do not guess hidden pairing steps.

## Test B — GATT
- [ ] Connect to the likely bike device.
- [ ] Record all service UUIDs.
- [ ] Record characteristic UUIDs and READ / WRITE / WRITE_NO_RESPONSE / NOTIFY / INDICATE properties.
- [ ] Record descriptor UUIDs, especially CCCD `00002902-0000-1000-8000-00805f9b34fb`.
- [ ] Disconnect cleanly.
- [ ] Repeat after power-cycling the bike.

## Test C — Compare
- [ ] Check OTC service UUID from REFERENCE_RESEARCH.md.
- [ ] Check TBT characteristic UUID from REFERENCE_RESEARCH.md.
- [ ] Note any additional services/characteristics; do not assume their purpose.
- [ ] Do not classify LCD/TFT solely from model name.

## Send back
1. Advertised device name (redact address).
2. GATT report screenshot or copied text.
3. Android phone model and Android version.
4. Whether Bajaj Ride Connect is paired/connected at the same time.
