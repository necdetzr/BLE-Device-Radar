package com.necdetzr.radar

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope

@Stable
class RadarStateHolder(
    val context: Context,
    val scope: CoroutineScope,
    val snackbarHostState: SnackbarHostState
) {
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter : BluetoothAdapter? = bluetoothManager.adapter

    val permissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )
    fun hasAllPermissions(): Boolean{
        return permissions.all { permission->
            ContextCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    fun createEnableBtIntent(): Intent{
        return Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    }
}
@Composable
fun BleHardwareEffect(
    stateHolder: RadarStateHolder,
    onBluetoothTurnedOff: () -> Unit
) {
    DisposableEffect(stateHolder.context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_TURNING_OFF) {
                        onBluetoothTurnedOff()
                    }
                }
            }
        }
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        ContextCompat.registerReceiver(
            stateHolder.context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            stateHolder.context.unregisterReceiver(receiver)
        }
    }
}

@Composable
fun rememberRadarStateHolder(
    context: Context = LocalContext.current,
    scope: CoroutineScope = rememberCoroutineScope(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }

) : RadarStateHolder{
    return remember(context,scope,snackbarHostState){
        RadarStateHolder(
            context = context,
            scope = scope,
            snackbarHostState = snackbarHostState
        )
    }
}
