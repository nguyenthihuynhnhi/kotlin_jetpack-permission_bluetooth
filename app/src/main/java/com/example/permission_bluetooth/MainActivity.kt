package com.example.permission_bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity() {

    private val bluetoothPermissions = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_CONNECT // Quan trọng cho Android 12+
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BluetoothPermissionApp()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BluetoothPermissionApp() {
        // State để theo dõi trạng thái cấp quyền và bật Bluetooth
        var bluetoothPermissionsGranted by remember { mutableStateOf(false) }
        var bluetoothEnabled by remember { mutableStateOf(false) }

        // Bluetooth Manager và Adapter
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        // Launcher để yêu cầu quyền Bluetooth
        val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Cập nhật trạng thái sau khi yêu cầu quyền Bluetooth
            bluetoothPermissionsGranted = permissions[Manifest.permission.BLUETOOTH] == true &&
                    permissions[Manifest.permission.BLUETOOTH_ADMIN] == true &&
                    permissions[Manifest.permission.BLUETOOTH_CONNECT] == true

            // Nếu quyền Bluetooth được cấp, yêu cầu bật Bluetooth
            if (bluetoothPermissionsGranted) {
                enableBluetoothIfNeeded(bluetoothAdapter)
                bluetoothEnabled = bluetoothAdapter.isEnabled
            }
        }

        Scaffold(
            topBar = { TopAppBar(title = { Text("Bluetooth Permission App") }) },
            content = { contentPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(onClick = { bluetoothPermissionLauncher.launch(bluetoothPermissions) }) {
                        Text("Request Bluetooth Permissions")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = when {
                            bluetoothEnabled -> "Bluetooth is enabled!"
                            bluetoothPermissionsGranted -> "Bluetooth permissions granted, enabling Bluetooth..."
                            else -> "Please grant Bluetooth permissions."
                        }
                    )
                }
            }
        )
    }

    private fun enableBluetoothIfNeeded(bluetoothAdapter: BluetoothAdapter) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            // Kiểm tra quyền `BLUETOOTH_CONNECT` nếu API >= 31
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (!bluetoothAdapter.isEnabled) {
                    val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivity(enableBluetoothIntent)
                }
            } else {
                println("Permission BLUETOOTH_CONNECT not granted.")
            }
        } else {
            // Không cần quyền `BLUETOOTH_CONNECT` cho các API < 31
            if (!bluetoothAdapter.isEnabled) {
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivity(enableBluetoothIntent)
            }
        }
    }
}
