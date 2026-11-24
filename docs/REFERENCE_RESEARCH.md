# PulsarNav reference research

Reference: https://github.com/nikhilmaity00/pulsarnav

## Source areas inspected
- `BajajBleManager.kt`: BLE scan/connect lifecycle, GATT operation queue, session handling, MTU negotiation, reconnect behavior, and multiple cluster strategies.
- `OtcPacketBuilder.kt`: 48-byte turn-by-turn packet builder and checksum logic.
- `BleProtocolSafety.kt`: protocol write safety predicates.
- `GoogleMapsNotificationParser.kt`: navigation notification extraction.
- `ManeuverMatcher.kt`: maneuver recognition, including Google Maps icon-resource fallback.
- Protocol tests and research scripts.

## Architecture lessons
The reference has useful BLE foundations: dedicated handler thread, serialized GATT operations, session IDs to reject stale callbacks, bounded scans, MTU-aware writes, and suppression of repeated navigation frames. Its BLE manager also coordinates many unrelated features. Pulsar Connect will split scanning, GATT sessions, queueing, navigation parsing and protocol encoding into independent components.

Google Maps drawable resource names are not a stable public API. Our parser should prefer notification text and structured extras. Icon recognition, if needed, stays an optional fallback isolated from the main processing path.

## UUID leads from the reference — NOT verified for this N250
OTC service:
`0010676e-6972-6565-6e69-676e4543544f`

| Reference characteristic purpose | UUID |
|---|---|
| Turn-by-turn navigation | `0110676e-6972-6565-6e69-676e4543544f` |
| General status | `0210676e-6972-6565-6e69-676e4543544f` |
| Missed calls | `0310676e-6972-6565-6e69-676e4543544f` |
| Alerts | `0410676e-6972-6565-6e69-676e4543544f` |
| Playlist info | `0510676e-6972-6565-6e69-676e4543544f` |
| Media info | `0610676e-6972-6565-6e69-676e4543544f` |
| Favourite contacts | `0910676e-6972-6565-6e69-676e4543544f` |
| Handlebar controls | `0a10676e-6972-6565-6e69-676e4543544f` |
| Recent calls | `0b10676e-6972-6565-6e69-676e4543544f` |

The reference treats N250 cluster identity as potentially ambiguous and resolves LCD/TFT behavior using discovered characteristics and overrides. Do not select a protocol solely from the model name.

## TBT packet notes
The reference documents a 48-byte TBT frame, checksum over bytes 0–46 stored at byte 47, and fields for maneuver, step distance, ETA, total distance, text and flags. This is reverse-engineered interoperability information, not an official Bajaj specification.

Before writing: confirm service and characteristic on the physical bike, inspect properties, compare with a known-good app/session, validate packet layout against tests and captures, and only then perform a controlled TBT test while parked.

Never write undocumented vehicle-control, ECU, ABS, traction-control, immobilizer or security characteristics.

## License
PulsarNav includes an MIT license. This starter does not copy its source implementation. If code is later adapted, preserve the original copyright and license notice in accordance with MIT terms.
