package com.harimoradiya.wifimouseclientandroid

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.harimoradiya.wifimouseclientandroid.utils.PreferenceManager

class WifiMouse :Application() {


    override fun onCreate() {
        super.onCreate()
        PreferenceManager.init(this)
        DynamicColors.applyToActivitiesIfAvailable(this@WifiMouse)
    }
}