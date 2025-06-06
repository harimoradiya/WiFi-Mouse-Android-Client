package com.harimoradiya.wifimouseclientandroid.ui.frags

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.harimoradiya.wifimouseclientandroid.MacApp
import com.harimoradiya.wifimouseclientandroid.R
import com.harimoradiya.wifimouseclientandroid.ui.activities.MainActivity.Companion.connectionManager
import com.harimoradiya.wifimouseclientandroid.ui.adapters.MacAppAdapter
import com.harimoradiya.wifimouseclientandroid.viewmodel.ConnectionStatusViewModel
import com.harimoradiya.wifimouseclientandroid.viewmodel.SharedViewModel
import org.json.JSONArray

class AppLauncherFragment : Fragment() {
    private val TAG = "AppLauncherFragment"
    private lateinit var recyclerView: RecyclerView
    private val appList = mutableListOf<MacApp>()
    private val filteredAppList = mutableListOf<MacApp>()
    private lateinit var adapter: MacAppAdapter
    private lateinit var searchEditText: TextInputEditText
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private val connectionStatusViewModel: ConnectionStatusViewModel by activityViewModels()
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_app_launcher, container, false)

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewApps)
        searchEditText = view.findViewById(R.id.searchEditText)
        progressBar = view.findViewById(R.id.progressBar)
        emptyView = view.findViewById(R.id.emptyView)

        recyclerView.layoutManager = GridLayoutManager(context, 3)
        adapter = MacAppAdapter(filteredAppList) { app ->
            launchApp(app)
        }
        recyclerView.adapter = adapter

        searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                filterApps(s?.toString() ?: "")
            }
        })

        connectionStatusViewModel.isConnected.observe(viewLifecycleOwner) { connected ->
            if (connected) {
                requestAppList() // Fetch app list if connected
            } else {
                showDisconnectedUI()
            }
        }

        sharedViewModel.appList.observe(viewLifecycleOwner) { appListJson ->
            if (connectionStatusViewModel.isConnected.value ==true){
                Log.d("AppsFragment", "Received app list: $appListJson")
                // Update UI with the app list

                Log.d("AppLauncherFragment", "Received app list update: $appListJson")
                try {
                    Log.d(TAG, "Received app list update from server")

                    val jsonArray = JSONArray(appListJson)
                    appList.clear()
                    for (i in 0 until jsonArray.length()) {
                        val appObject = jsonArray.getJSONObject(i)
                        appList.add(
                            MacApp(
                                name = appObject.getString("name"),
                                bundleId = appObject.getString("bundleId"),
                                iconBase64 = appObject.getString("iconBase64")
                            )
                        )
                    }
                    filterApps(searchEditText.text?.toString() ?: "")
                    progressBar.visibility = View.GONE
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse app list", e)
                    Toast.makeText(context, "Failed to parse app list", Toast.LENGTH_SHORT).show()
                    showDisconnectedUI()
                }

            }
        }



    }

    private fun requestAppList() {
        Log.d(TAG, "Requesting application list from server")
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.INVISIBLE
        emptyView.visibility = View.GONE
        connectionManager?.sendCommand("APP:LIST")
    }

    @SuppressLint("SetTextI18n")
    private fun showDisconnectedUI() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.INVISIBLE
        emptyView.visibility = View.VISIBLE
        emptyView.text = "No apps found. Please connect first."
    }

    private fun launchApp(app: MacApp) {
        Log.d(TAG, "Launching app: ${app.name} (${app.bundleId})")
        connectionManager?.sendCommand("APP:LAUNCH:${app.name}")
        Toast.makeText(context, "Launching ${app.name}", Toast.LENGTH_SHORT).show()
    }

    private fun filterApps(query: String) {
        filteredAppList.clear()
        filteredAppList.addAll(if (query.isEmpty()) appList else appList.filter {
            it.name.contains(query, ignoreCase = true) || it.bundleId.contains(
                query,
                ignoreCase = true
            )
        })

        if (filteredAppList.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.INVISIBLE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }

        adapter.notifyDataSetChanged()
    }
}


