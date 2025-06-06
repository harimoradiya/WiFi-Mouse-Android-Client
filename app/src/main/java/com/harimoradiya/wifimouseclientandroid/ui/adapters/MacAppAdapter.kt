package com.harimoradiya.wifimouseclientandroid.ui.adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.harimoradiya.wifimouseclientandroid.MacApp
import com.harimoradiya.wifimouseclientandroid.R

class MacAppAdapter(
    private val apps: List<MacApp>,
    private val onAppClick: (MacApp) -> Unit
) : RecyclerView.Adapter<MacAppAdapter.ViewHolder>() {

    class ViewHolder(val cardView: MaterialCardView) : RecyclerView.ViewHolder(cardView) {
        val appIcon: ImageView = cardView.findViewById(R.id.appIcon)
        val appName: TextView = cardView.findViewById(R.id.appName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mac_app, parent, false) as MaterialCardView
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.appName.text = app.name

        try {
            if (app.iconBase64.isNotEmpty()) {
                val imageBytes = Base64.decode(app.iconBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                if (bitmap != null) {
                    holder.appIcon.setImageBitmap(bitmap)
                    holder.appIcon.scaleType = ImageView.ScaleType.FIT_CENTER
                } else {
                    holder.appIcon.setImageResource(R.drawable.ic_app_placeholder)
                    holder.appIcon.scaleType = ImageView.ScaleType.CENTER_INSIDE
                }
            } else {
                holder.appIcon.setImageResource(R.drawable.ic_app_placeholder)
                holder.appIcon.scaleType = ImageView.ScaleType.CENTER_INSIDE
            }
        } catch (e: Exception) {
            Log.e("MacAppAdapter", "Failed to load app icon for ${app.name}", e)
            holder.appIcon.setImageResource(R.drawable.ic_launcher_background)
        }

        holder.cardView.setOnClickListener { onAppClick(app) }
    }

    override fun getItemCount() = apps.size
}