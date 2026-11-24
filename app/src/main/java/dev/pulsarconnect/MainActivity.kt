package dev.pulsarconnect

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

private val Ink = Color(0xFF0B0D12)
private val Panel = Color(0xFF151922)
private val PanelRaised = Color(0xFF1C222D)
private val Accent = Color(0xFFFF4B4B)
private val Muted = Color(0xFF929BAA)
private val Mint = Color(0xFF56D6A2)

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
                gattLines.clear()
                gattLines.addAll(lines)
            } }
        )
        setContent {
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { result ->
                if (result.values.all { it }) probe.startScan()
                else status.value = "Bluetooth permissions were not granted."
            }

            val currentStatus = status.value
            val isConnected = currentStatus.startsWith("Connected") ||
                currentStatus.startsWith("GATT discovery complete")
            val isScanning = currentStatus.startsWith("Scanning")
            val statusColor = if (isConnected) Mint else if (isScanning) Accent else Muted

            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Accent,
                    onPrimary = Color.White,
                    secondary = Mint,
                    background = Ink,
                    surface = Panel,
                    onSurface = Color(0xFFF3F5F8),
                    onSurfaceVariant = Muted
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Ink
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF171C27), Ink, Ink)
                                )
                            ),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(Accent, RoundedCornerShape(14.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("P", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black)
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                        Text("Pulsar Connect", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .background(PanelRaised, CircleShape)
                                        .padding(horizontal = 11.dp, vertical = 7.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(Modifier.size(7.dp).background(statusColor, CircleShape))
                                        Text(
                                            when {
                                                isConnected -> "CONNECTED"
                                                isScanning -> "SCANNING"
                                                else -> "STANDBY"
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.8.sp,
                                            color = statusColor
                                        )
                                    }
                                }
                            }
                        }


                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Panel),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(15.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Box(
                                            modifier = Modifier.size(38.dp).background(PanelRaised, RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) { Text("⌁", color = Accent, fontSize = 25.sp, fontWeight = FontWeight.Bold) }
                                        Column {
                                            Text("Bluetooth discovery", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                        }
                                    }
                                    Text(
                                        currentStatus,
                                        color = statusColor,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                    Button(
                                        onClick = {
                                            devices.clear()
                                            gattLines.clear()
                                            if (hasPermissions()) probe.startScan() else launcher.launch(permissions())
                                        },
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        shape = RoundedCornerShape(15.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Accent)
                                    ) {
                                        Text(if (isScanning) "Restart scan" else "Scan for motorcycle", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { probe.stopScan() },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(13.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                        ) { Text("Stop scan") }
                                        OutlinedButton(
                                            onClick = { probe.disconnect(); gattLines.clear() },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(13.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                        ) { Text("Disconnect") }
                                    }
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("Nearby devices", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                                    Text("${devices.size} device(s) discovered", color = Muted, fontSize = 12.sp)
                                }
                            }
                        }

                        if (devices.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = Panel)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(22.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("No devices found", fontWeight = FontWeight.SemiBold)
                                        Text("Tap scan to search nearby.", color = Muted, fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            items(devices, key = { it.address }) { device ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = Panel)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(11.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                Text(device.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(device.address, color = Muted, fontSize = 11.sp)
                                            }
                                            Text(
                                                "${device.rssi} dBm",
                                                color = if (device.rssi > -65) Mint else Muted,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Button(
                                            onClick = {
                                                if (hasPermissions()) probe.connect(device) else launcher.launch(permissions())
                                            },
                                            modifier = Modifier.fillMaxWidth().height(44.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = PanelRaised, contentColor = Color.White)
                                        ) { Text("Connect & inspect", fontWeight = FontWeight.SemiBold) }
                                    }
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("GATT diagnostics", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier.background(PanelRaised, CircleShape).padding(horizontal = 9.dp, vertical = 5.dp)
                                ) {
                                    Text("${gattLines.size} rows", color = Muted, fontSize = 10.sp)
                                }
                            }
                        }

                        if (gattLines.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = Panel)
                                ) {
                                    Text(
                                        "Connect to a device to view its GATT profile here.",
                                        modifier = Modifier.padding(18.dp),
                                        color = Muted,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            items(gattLines) { line ->
                                val isService = line.startsWith("SERVICE")
                                Text(
                                    line,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isService) PanelRaised else Panel,
                                            RoundedCornerShape(if (isService) 10.dp else 7.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = if (isService) 11.dp else 8.dp),
                                    color = if (isService) Accent else Color(0xFFCBD2DC),
                                    fontSize = if (isService) 12.sp else 10.sp,
                                    fontWeight = if (isService) FontWeight.Bold else FontWeight.Normal,
                                    lineHeight = 15.sp
                                )
                            }
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
