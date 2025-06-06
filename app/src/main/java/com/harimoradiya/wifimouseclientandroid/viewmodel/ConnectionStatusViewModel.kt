package com.harimoradiya.wifimouseclientandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.harimoradiya.wifimouseclientandroid.utils.PreferenceManager

class ConnectionStatusViewModel : ViewModel() {
    private val _isConnected = MutableLiveData<Boolean>().apply { value = false }
    val isConnected: LiveData<Boolean> get() = _isConnected

    fun setConnectedStatus(status: Boolean) {
        _isConnected.value = status
    }
}
