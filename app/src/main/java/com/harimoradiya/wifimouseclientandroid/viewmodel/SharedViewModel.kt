package com.harimoradiya.wifimouseclientandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _appList = MutableLiveData<String>()
    val appList: LiveData<String> get() = _appList

    fun updateAppList(appListJson: String) {
        _appList.postValue(appListJson)
    }
}