package com.harimoradiya.wifimouseclientandroid.ui.frags

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.harimoradiya.wifimouseclientandroid.databinding.FragmentMouseBinding
import com.harimoradiya.wifimouseclientandroid.ui.activities.MainActivity
import com.harimoradiya.wifimouseclientandroid.ui.activities.MainActivity.Companion.connectionManager
import com.harimoradiya.wifimouseclientandroid.viewmodel.ConnectionStatusViewModel
import com.harimoradiya.wifimouseclientandroid.views.TouchpadView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sqrt


class MouseControlFragment : Fragment() {
    val TAG = "MouseControlFragment"
    private var _binding: FragmentMouseBinding? = null
    private val binding get() = _binding!!
    // Touch tracking variables
    private var lastX = 0f
    private var lastY = 0f
    private var isTracking = false
    private val sensitivity = 0.5f // Adjust this value to control cursor speed

    // Add these constants at the top of your class
    private val MAX_TAP_DURATION = 200L // milliseconds
    private val MAX_TAP_DISTANCE_DP = 20f // dp
    private val maxTapDistancePx by lazy {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            MAX_TAP_DISTANCE_DP,
            binding.root.context.resources.displayMetrics
        )
    }
    private var scrollJob: Job? = null
    // ViewModel to observe connection status
    private val connectionStatusViewModel: ConnectionStatusViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMouseBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.touchpadView.setTouchpadListener(object : TouchpadView.TouchpadListener {
            override fun onTap() {
                Log.d(TAG, "onViewCreated: btnLeftClick")
                performAction {

                    connectionManager?.sendCommand("MOUSE:CLICK:1")
                }
            }

            override fun onMove(deltaX: Float, deltaY: Float) {
                performAction {
                    val sensitivity = 2f
                    val scaledDx = deltaX * sensitivity
                    val scaledDy = deltaY * sensitivity
                    connectionManager?.sendCommand("MOUSE:MOVE:${scaledDx.toInt()}:${scaledDy.toInt()}")
                }

            }

            override fun onMultiFingerSwipe(direction: TouchpadView.SwipeDirection, fingerCount: Int) {
                Log.d(TAG, "onMultiFingerSwipe: direction=$direction, fingers=$fingerCount")

                performAction {
                    val command = when (direction) {
                        TouchpadView.SwipeDirection.LEFT -> "GESTURE:WORKSPACE:LEFT"
                        TouchpadView.SwipeDirection.RIGHT -> "GESTURE:WORKSPACE:RIGHT"
                    }
                    connectionManager?.sendCommand(command)
                }

            }

            override fun onScroll(scrollAmount: Int) {
                Log.d(TAG, "onScroll: amount=$scrollAmount")
                performAction {
                    connectionManager?.sendCommand("MOUSE:SCROLL:$scrollAmount")
                }

            }
        })

        binding.btnLeftClick.setOnClickListener {
            Log.d(TAG, "onViewCreated: btnLeftClick")
            performAction {
                connectionManager?.sendCommand("MOUSE:CLICK:1")
            }

        }

        binding.btnRightClick.setOnClickListener {
            Log.d(TAG, "onViewCreated: btnRightClick")
            performAction {
                connectionManager?.sendCommand("MOUSE:CLICK:3")
            }

        }

        binding.btnTakeSS.setOnClickListener {
            Log.d(TAG, "onViewCreated: Taking screenshot")
            performAction {

            showScreenshotDialog()
            }
        }



    }

    private fun performAction(action: () -> Unit) {
        if (connectionStatusViewModel.isConnected.value == true) {
            Log.d(TAG, "performAction: ")
            action()
        } else {
            (activity as? MainActivity)?.showSnackbar("Please connect first!")
        }
    }


    private fun showScreenshotDialog() {
        val options = arrayOf("Full Screen", "Portion", "Window")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Screenshot Type")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> connectionManager?.sendCommand("SCREENSHOT:1")
                    1 -> connectionManager?.sendCommand("SCREENSHOT:2")
                    2 -> connectionManager?.sendCommand("SCREENSHOT:3")
                }
            }
            .show()
    }




    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}