package dev.pulsarconnect

import android.bluetooth.BluetoothClass
import java.util.Locale

object BluetoothDeviceClassifier {
    fun fromBluetoothClass(deviceClass: BluetoothClass?): String {
        if (deviceClass == null) return "Unknown device"
        val device = deviceClass.deviceClass
        return when (deviceClass.majorDeviceClass) {
            BluetoothClass.Device.Major.PHONE -> "Mobile phone"
            BluetoothClass.Device.Major.COMPUTER -> "Computer"
            BluetoothClass.Device.Major.AUDIO_VIDEO -> when (device) {
                BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES,
                BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET,
                BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER -> "Headphones / audio"
                BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO -> "Car audio"
                BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO -> "Hi-fi audio"
                BluetoothClass.Device.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER,
                BluetoothClass.Device.AUDIO_VIDEO_VIDEO_MONITOR,
                BluetoothClass.Device.AUDIO_VIDEO_VIDEO_CONFERENCING -> "Display / video"
                else -> "Audio / video"
            }
            BluetoothClass.Device.Major.PERIPHERAL -> "Input device"
            BluetoothClass.Device.Major.IMAGING -> "Printer / imaging"
            BluetoothClass.Device.Major.WEARABLE -> "Wearable"
            BluetoothClass.Device.Major.NETWORKING -> "Network device"
            else -> "Bluetooth device"
        }
    }

    fun fromAdvertisedServices(uuids: List<String>): String {
        val ids = uuids.map { it.lowercase(Locale.ROOT) }
        return when {
            ids.any { it.startsWith("0000110b-") || it.startsWith("0000110d-") ||
                it.startsWith("0000111e-") || it.startsWith("00001108-") } -> "Headphones / audio"
            ids.any { it.startsWith("00001812-") } -> "Keyboard / mouse"
            ids.any { it.startsWith("0000180d-") || it.startsWith("00001816-") } -> "Fitness device"
            ids.any { it.startsWith("0000180f-") } -> "Battery device"
            ids.any { it.startsWith("0000180a-") } -> "Smart device"
            ids.any { it.startsWith("0000181a-") } -> "Sensor"
            else -> "Unknown device"
        }
    }
}
