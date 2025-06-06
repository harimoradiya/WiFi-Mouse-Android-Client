package com.harimoradiya.wifimouseclientandroid.manager

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.io.OutputStreamWriter
import java.net.Socket
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.BufferedWriter
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

interface ConnectionListener {
    fun onConnectionStatusChanged(connected: Boolean)
    fun onAppListUpdated(appListJson: String)
}

class ConnectionManager(
    private val ip: String,
    private val port: Int,
    private val activity: AppCompatActivity
) {
    private val TAG = "ConnectionManager"
    private var socket: Socket? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null
    private var outputStream: DataOutputStream? = null
    private var inputStream: DataInputStream? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val listeners = mutableSetOf<ConnectionListener>()

    @Volatile
    private var isConnected = false

    fun addConnectionListener(listener: ConnectionListener) {
        listeners.add(listener)
    }

    fun removeConnectionListener(listener: ConnectionListener) {
        listeners.remove(listener)
    }

    fun connect(callback: (Boolean) -> Unit) {
        if (isConnected) {
            Log.d(TAG, "Already connected to $ip:$port")
            return
        }

        scope.launch {
            try {
                disconnect() // Ensure no stale connection
                socket = Socket(ip, port)
                writer = BufferedWriter(OutputStreamWriter(socket!!.getOutputStream()))
                reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
                outputStream = DataOutputStream(socket!!.getOutputStream())
                inputStream = DataInputStream(socket!!.getInputStream())

                isConnected = true
                activity.runOnUiThread { callback(true) }
                activity.runOnUiThread { notifyConnectionStatus(true) }
                // Send device name
                sendCommand("DEVICE_NAME:${getDeviceName()}")

                listenForMessages()

            } catch (e: IOException) {
                Log.e(TAG, "Connection failed", e)
                activity.runOnUiThread { callback(false) }
                disconnect()
            }
        }
    }

    private fun listenForMessages() {
        scope.launch {
            try {
                while (isConnected) {
                    val message = reader?.readLine() ?: break
                    Log.d(TAG, "Received: $message")

                    when {
                        message == "SERVER_SHUTDOWN" -> {
                            Log.d(TAG, "Server shutdown received")
                            disconnect()
                        }
                        message.startsWith("APP_LIST:") -> {
                            val appListJson = message.removePrefix("APP_LIST:")
                            activity.runOnUiThread { notifyAppListUpdated(appListJson) }
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Connection lost , ${e.message}")
                disconnect()
            }
        }
    }

    fun sendCommand(command: String) {
        if (command.isBlank() || !isConnected) {
            Log.e(TAG, "Invalid or empty command")
            return
        }

        scope.launch {
            try {
                Log.d(TAG, "Sending: $command")
                writer?.write("$command\n")
                writer?.flush()
            } catch (e: IOException) {
                Log.e(TAG, "Failed to send command: $command", e)
                disconnect()
            }
        }
    }

    private fun getDeviceName(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Settings.Global.getString(activity.contentResolver, Settings.Global.DEVICE_NAME)
                ?: Build.MODEL
        } else {
            Build.MODEL
        }
    }

    fun disconnect() {
        scope.launch {
            try {
                isConnected = false
                writer?.close()
                reader?.close()
                socket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing connection", e)
            } finally {
                scope.coroutineContext.cancelChildren()
                activity.runOnUiThread { notifyConnectionStatus(false) }
            }
        }
    }

    private fun notifyConnectionStatus(connected: Boolean) {
        listeners.forEach { it.onConnectionStatusChanged(connected) }
    }

    private fun notifyAppListUpdated(appListJson: String) {
        listeners.forEach { it.onAppListUpdated(appListJson) }
    }

    suspend fun sendFileData(buffer: ByteArray, bytesRead: Int) {
        //fileTransferManager.sendFileData(buffer, bytesRead)
    }

    fun getOutputStream(): DataOutputStream? = outputStream

    fun getInputStream(): DataInputStream? = inputStream
}
