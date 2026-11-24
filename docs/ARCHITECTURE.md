# Pulsar Connect architecture

## Principles
1. Navigation parsing is independent of UI lifecycle.
2. One owner per GATT connection and one serialized operation queue.
3. Every reconnect creates a new session; stale callbacks cannot mutate a new session.
4. N250 cluster profile remains unknown until GATT evidence is collected.
5. No protocol writes until characteristic, packet format and safe test conditions are verified.
6. Optional telemetry never blocks the navigation path.

## Planned packages
```
dev.pulsarconnect/
  app/                 MainActivity and UI state
  core/model/          NavigationInstruction, BikeProfile
  core/diagnostics/    EventTrace and latency metrics
  ble/                 Scanner, connection manager, GATT session, operation queue
  protocol/            Cluster resolver, TBT encoder, checksum, fixtures
  navigation/          Event source, notification parser, maneuver normalizer, deduplicator
  service/             Background navigation bridge service
  data/                Room database and DataStore settings
```

## Pipeline
Notification listener -> immutable NavigationEvent -> parser -> normalized maneuver -> deduplicator -> protocol encoder -> serialized BLE write queue.

The UI observes state but is not part of the critical path.

## Metrics
Record notification received, parsed, encoded, GATT write accepted and callback timestamps; event-to-write latency; duplicate suppression count; GATT failures; disconnect count; reconnect duration.

A successful Android write request does not prove that the motorcycle cluster physically rendered the instruction.
