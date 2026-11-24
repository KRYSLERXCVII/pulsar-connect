package dev.pulsarconnect

import android.bluetooth.BluetoothDevice

data class NearbyBleDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    internal val nativeDevice: BluetoothDevice
)
