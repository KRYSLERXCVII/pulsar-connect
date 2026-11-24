package dev.pulsarconnect

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private lateinit var probe: N250BleProbe
    private val devices = mutableStateListOf<NearbyBleDevice>()
    private val gattLines = mutableStateListOf<String>()
    private val status = mutableStateOf("Ready — keep the motorcycle parked during testing.")

    private fun permissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    private fun hasPermissions() = permissions().all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        probe = N250BleProbe(applicationContext,
            onStatus = { runOnUiThread { status.value = it } },
            onDevice = { found -> runOnUiThread {
                val i = devices.indexOfFirst { it.address == found.address }
                if (i >= 0) devices[i] = found else devices.add(found)
                devices.sortByDescending { it.rssi }
            } },
            onGattReport = { lines -> runOnUiThread {
                gattLines.clear(); gattLines.addAll(lines)
            } }
        )
        setContent {
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { result ->
                if (result.values.all { it }) probe.startScan()
                else status.value = "Bluetooth permissions were not granted."
            }
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Pulsar Connect", style = MaterialTheme.typography.headlineMedium)
                        Text("N250 · Phase 1 BLE discovery")
                        Text(status.value, style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                devices.clear(); gattLines.clear()
                                if (hasPermissions()) probe.startScan() else launcher.launch(permissions())
                            }) { Text("Scan nearby") }
                            OutlinedButton(onClick = { probe.stopScan() }) { Text("Stop") }
                            OutlinedButton(onClick = { probe.disconnect() }) { Text("Disconnect") }
                        }
                        Text("Nearby BLE devices", style = MaterialTheme.typography.titleMedium)
                        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(devices, key = { it.address }) { d ->
                                Card {
                                    Column(Modifier.fillMaxWidth().padding(10.dp)) {
                                        Text(d.name, style = MaterialTheme.typography.titleSmall)
                                        Text("RSSI ${d.rssi} dBm")
                                        Text(d.address, style = MaterialTheme.typography.bodySmall)
                                        OutlinedButton(onClick = {
                                            if (hasPermissions()) probe.connect(d) else launcher.launch(permissions())
                                        }) { Text("Connect and inspect GATT") }
                                    }
                                }
                            }
                        }
                        Text("GATT report", style = MaterialTheme.typography.titleMedium)
                        LazyColumn(Modifier.weight(1f)) {
                            items(gattLines) { Text(it, style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        if (::probe.isInitialized) probe.close()
        super.onDestroy()
    }
}
