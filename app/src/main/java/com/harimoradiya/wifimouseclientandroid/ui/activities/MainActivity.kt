package com.harimoradiya.wifimouseclientandroid.ui.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.harimoradiya.wifimouseclientandroid.R
import com.harimoradiya.wifimouseclientandroid.databinding.ActivityMainBinding
import com.harimoradiya.wifimouseclientandroid.manager.ConnectionListener
import com.harimoradiya.wifimouseclientandroid.manager.ConnectionManager
import com.harimoradiya.wifimouseclientandroid.utils.PreferenceManager
import com.harimoradiya.wifimouseclientandroid.viewmodel.ConnectionStatusViewModel
import com.harimoradiya.wifimouseclientandroid.viewmodel.SharedViewModel


class MainActivity : AppCompatActivity() {
    companion object{
         var connectionManager: ConnectionManager? = null
    }

    private lateinit var binding: ActivityMainBinding

    private var progressDialog: AlertDialog? = null
    private lateinit var navController: NavController
    private val tag = "MainActivity"
    private val connectionStatusViewModel: ConnectionStatusViewModel by viewModels()
    private lateinit var sharedViewModel: SharedViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        val navHostFragment =
            supportFragmentManager.findFragmentById(
                R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupNavigation()
        setupToolbar()

    }

    private fun setupNavigation() {
        binding.bottomNavigation.setupWithNavController(navController)
        binding.bottomNavigation.setOnItemReselectedListener { /* Do nothing */ }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_connect -> {
                    if (connectionStatusViewModel.isConnected.value == true) disconnect() else showConnectionDialog()
                    true
                }

                R.id.menu_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }



    private fun connectToServer(ip: String, port: Int) {
        showProgressDialog("Connecting...")

        connectionManager?.disconnect()
        connectionManager = ConnectionManager(ip, port, this).apply {
            addConnectionListener(object : ConnectionListener {
                override fun onConnectionStatusChanged(connected: Boolean) {

                    runOnUiThread {
                        if (connected){
                            Log.d(tag, "onConnectionStatusChanged: $connected")
                            updateConnectionStatus(connected)
                            connectionStatusViewModel.setConnectedStatus(true)
                            showSnackbar("Server connected successfully")
                        }else{
                            updateConnectionStatus(connected)
                            connectionStatusViewModel.setConnectedStatus(false)
                            showSnackbar("Server disconnected")
                        }
                    }
                }

                override fun onAppListUpdated(appListJson: String) {
                    Log.d(tag, "onAppListUpdated: $appListJson")
                    sharedViewModel.updateAppList(appListJson) // Update ViewModel
                }
            })
        }

        connectionManager?.connect { success ->
            dismissProgressDialog()
            if (success) {
                connectionStatusViewModel.setConnectedStatus(true)
                showSnackbar("Connected successfully!")
            } else {
                showConnectionErrorDialog(ip, port)
            }
        }
    }

    private fun showConnectionDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_connect, null)
        val etIp = dialogView.findViewById<TextInputEditText>(R.id.etIpAddress)
        val etPort = dialogView.findViewById<TextInputEditText>(R.id.etPort)

        etPort.setText("8080")  // Default port

        MaterialAlertDialogBuilder(this)
            .setTitle("Connect to Server")
            .setView(dialogView)
            .setPositiveButton("Connect") { dialog, _ ->
                val ip = etIp.text?.toString()?.trim() ?: ""
                val port = etPort.text?.toString()?.toIntOrNull() ?: 8080

                if (validateInput(ip, port)) {
                    connectToServer(ip, port)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun validateInput(ip: String, port: Int): Boolean {
        val ipPattern = "^([0-9]{1,3}\\.){3}[0-9]{1,3}\$"
        return when {
            !ip.matches(ipPattern.toRegex()) -> {
                showSnackbar("Invalid IP address format")
                false
            }
            port !in 1..65535 -> {
                showSnackbar("Port must be between 1-65535")
                false
            }
            else -> true
        }
    }

    private fun updateConnectionStatus(connected: Boolean) {
        Log.d(tag, "updateConnectionStatus: $connected")
        val menuItem = binding.toolbar.menu.findItem(R.id.menu_status)
        menuItem?.icon?.let { drawable ->
            val newColor = if (connected) Color.GREEN else Color.RED
            val wrappedDrawable = DrawableCompat.wrap(drawable) // Ensure mutability
            DrawableCompat.setTint(wrappedDrawable, newColor)
            menuItem.icon = wrappedDrawable
            Log.d(tag, "Applied color: $newColor")
        }

        binding.toolbar.menu.findItem(R.id.menu_connect)?.title =
            if (connected) "Disconnect" else "Connect"
    }

    private fun showConnectionErrorDialog(ip: String, port: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Connection Failed")
            .setMessage("Could not connect to $ip:$port")
            .setPositiveButton("Retry") { _, _ -> connectToServer(ip, port) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun showSnackbar(message: String) {
        Snackbar.make(findViewById(R.id.nav_host_fragment), message, Snackbar.LENGTH_LONG)
            .setAnchorView(R.id.bottom_navigation)
            .show()
    }

    private fun showProgressDialog(message: String) {
        progressDialog = MaterialAlertDialogBuilder(this)
            .setView(R.layout.dialog_progress)
            .setMessage(message)
            .setCancelable(false)
            .show()
    }

    private fun dismissProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)



        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.menu_connect)?.title =
            if (connectionStatusViewModel.isConnected.value == true) "Disconnect" else "Connect"
        if (PreferenceManager.isConnectionStatusVisible()){
            menu.findItem(R.id.menu_status)?.isVisible
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_connect -> {
                if (connectionStatusViewModel.isConnected.value == true) disconnect() else showConnectionDialog()
                true
            }
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun disconnect() {
        connectionManager?.disconnect()
        updateConnectionStatus(false)
        showSnackbar("Disconnected")
    }

    override fun onDestroy() {
        super.onDestroy()
        connectionManager?.disconnect()
    }

}
