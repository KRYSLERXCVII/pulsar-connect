package dev.pulsarconnect

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.os.Looper

@SuppressLint("MissingPermission")
class N250BleProbe(
    context: Context,
    private val onStatus: (String) -> Unit,
    private val onDevice: (NearbyBleDevice) -> Unit,
    private val onGattReport: (List<String>) -> Unit
) {
    private val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val handler = Handler(Looper.getMainLooper())
    private var gatt: BluetoothGatt? = null
    private var scanning = false
    private val stopRunnable = Runnable { stopScan() }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(type: Int, result: ScanResult) {
            val device = result.device
            val name = device.name?.takeIf { it.isNotBlank() }
                ?: result.scanRecord?.deviceName ?: "Unnamed BLE device"
            handler.post { onDevice(NearbyBleDevice(name, device.address, result.rssi, device)) }
        }
        override fun onScanFailed(errorCode: Int) {
            handler.post { onStatus("BLE scan failed: $errorCode") }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            handler.post {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    onStatus("GATT connection error: $status")
                    g.close()
                    if (gatt === g) gatt = null
                    return@post
                }
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        onStatus("Connected. Discovering GATT services…")
                        g.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        onStatus("Disconnected")
                        g.close()
                        if (gatt === g) gatt = null
                    }
                }
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            handler.post {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    onStatus("Service discovery failed: $status")
                    return@post
                }
                val report = mutableListOf<String>()
                g.services.forEach { service ->
                    report += "SERVICE ${service.uuid}"
                    service.characteristics.forEach { c ->
                        val p = c.properties
                        val flags = buildList {
                            if (p and BluetoothGattCharacteristic.PROPERTY_READ != 0) add("READ")
                            if (p and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) add("WRITE")
                            if (p and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) add("WRITE_NO_RESPONSE")
                            if (p and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) add("NOTIFY")
                            if (p and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) add("INDICATE")
                        }.joinToString().ifBlank { "no standard properties" }
                        report += "  CHAR ${c.uuid} [$flags]"
                        c.descriptors.forEach { d -> report += "    DESC ${d.uuid}" }
                    }
                }
                onGattReport(report)
                onStatus("GATT discovery complete. No data was written.")
            }
        }
    }

    fun startScan() {
        if (scanning) stopScan()
        val scanner = adapter?.bluetoothLeScanner
        if (scanner == null || adapter?.isEnabled != true) {
            onStatus("Bluetooth unavailable or disabled")
            return
        }
        try {
            scanner.startScan(null, ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), scanCallback)
            scanning = true
            onStatus("Scanning for 12 seconds…")
            handler.removeCallbacks(stopRunnable)
            handler.postDelayed(stopRunnable, 12_000L)
        } catch (e: SecurityException) {
            onStatus("Bluetooth permission missing: ${e.message}")
        } catch (e: Exception) {
            onStatus("Scan error: ${e.message}")
        }
    }

    fun stopScan() {
        handler.removeCallbacks(stopRunnable)
        if (!scanning) return
        try { adapter?.bluetoothLeScanner?.stopScan(scanCallback) }
        catch (_: SecurityException) { }
        finally { scanning = false }
        onStatus("Scan stopped")
    }

    fun connect(device: NearbyBleDevice) {
        stopScan()
        disconnect()
        try {
            onStatus("Connecting to ${device.name}…")
            gatt = device.nativeDevice.connectGatt(
                context = null, autoConnect = false, callback = gattCallback
            )
        } catch (e: Exception) {
            onStatus("Connect error: ${e.message}")
        }
    }

    fun disconnect() {
        try { gatt?.disconnect(); gatt?.close() }
        catch (_: SecurityException) { }
        finally { gatt = null }
    }

    fun close() { stopScan(); disconnect() }
}
