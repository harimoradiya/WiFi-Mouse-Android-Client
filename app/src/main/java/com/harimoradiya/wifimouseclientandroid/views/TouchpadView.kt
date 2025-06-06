package com.harimoradiya.wifimouseclientandroid.views

import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign



class TouchpadView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    interface TouchpadListener {
        fun onTap() // Single finger tap (left click)
        fun onMove(deltaX: Float, deltaY: Float) // Finger movement
        fun onMultiFingerSwipe(direction: SwipeDirection, fingerCount: Int) // Multi-finger swipe
        fun onScroll(scrollAmount: Int) // Two-finger scroll
    }

    enum class SwipeDirection {
        LEFT, RIGHT
    }

    private var listener: TouchpadListener? = null
    private var startX = 0f
    private var startY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var lastMoveTime = 0L
    private var velocityX = 0f
    private var velocityY = 0f
    private val touchSlop = 8.dpToPx() // Minimum movement to consider as drag
    private val swipeThreshold = 80.dpToPx() // Minimum distance for swipe gesture
    private val minSwipeVelocity = 200f // Minimum velocity for swipe gesture (dp/second)
    private val maxScrollVelocity = 20f // Further reduced for even slower maximum speed
    private var activePointerId = -1
    private var fingerCount = 0
    private var hasTriggeredSwipe = false
    private var isScrolling = false
    private var scrollVelocityTracker = 0f

    fun setTouchpadListener(listener: TouchpadListener) {
        this.listener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                fingerCount = event.pointerCount
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    activePointerId = event.getPointerId(0)
                    startX = event.x
                    startY = event.y
                    lastX = event.x
                    lastY = event.y
                    lastMoveTime = System.currentTimeMillis()
                    hasTriggeredSwipe = false
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (fingerCount >= 3 && fingerCount <= 4 && !hasTriggeredSwipe) {
                    val currentTime = System.currentTimeMillis()
                    val deltaTime = (currentTime - lastMoveTime) / 1000f // Convert to seconds
                    val totalDeltaX = event.x - startX
                    val totalDeltaY = event.y - startY
                    
                    // Calculate velocity (dp/second)
                    velocityX = if (deltaTime > 0) abs(totalDeltaX) / deltaTime else 0f
                    
                    // Ensure the gesture is more horizontal than vertical and meets velocity threshold
                    if (abs(totalDeltaX) > swipeThreshold && 
                        abs(totalDeltaX) > abs(totalDeltaY) * 1.5f && 
                        velocityX > minSwipeVelocity) {
                        val direction = if (totalDeltaX > 0) SwipeDirection.RIGHT else SwipeDirection.LEFT
                        listener?.onMultiFingerSwipe(direction, fingerCount)
                        hasTriggeredSwipe = true
                    }
                    
                    lastMoveTime = currentTime
                } else if (fingerCount == 2) {
                    val deltaY = event.y - lastY
                    val currentTime = System.currentTimeMillis()
                    val deltaTime = (currentTime - lastMoveTime) / 1000f // Convert to seconds
                    
                    if (abs(deltaY) > 0) {
                        isScrolling = true
                        // Calculate velocity with even smoother acceleration
                        velocityY = if (deltaTime > 0) {
                            val rawVelocity = deltaY / deltaTime
                            val alpha = 0.15f // Further reduced for even smoother transitions
                            scrollVelocityTracker = scrollVelocityTracker * (1 - alpha) + rawVelocity * alpha
                            scrollVelocityTracker.coerceIn(-maxScrollVelocity, maxScrollVelocity)
                        } else {
                            scrollVelocityTracker
                        }
                        
                        // Further refined smooth acceleration curve for more natural scrolling
                        val baseSpeed = 0.15f  // Further reduced for even slower initial response
                        val minAcceleration = 1.05f // Further reduced for even gentler acceleration
                        val maxAcceleration = 1.5f // Further reduced for less aggressive max speed
                        
                        // Dynamic acceleration based on movement speed and velocity
                        val speed = abs(velocityY)
                        val normalizedSpeed = (speed / maxScrollVelocity).coerceIn(0f, 1f)
                        val acceleration = minAcceleration + (maxAcceleration - minAcceleration) * normalizedSpeed
                        
                        // Apply acceleration curve with improved smoothing
                        val speedMultiplier = (1 + speed.pow(acceleration) * 0.001f) // Further reduced multiplier
                        val smoothDelta = -velocityY * baseSpeed * speedMultiplier
                        
                        // Enhanced smoothing for small movements
                        val finalDelta = if (abs(smoothDelta) < 0.2f) { // Further reduced threshold
                            sign(smoothDelta) * 0.2f
                        } else {
                            smoothDelta
                        }
                        
                        listener?.onScroll(finalDelta.toInt())
                        lastY = event.y
                        lastMoveTime = currentTime
                    }
                } else if (fingerCount == 1) {
                    val deltaX = event.x - lastX
                    val deltaY = event.y - lastY
                    if (abs(deltaX) > 0 || abs(deltaY) > 0) {
                        listener?.onMove(deltaX, deltaY)
                        lastX = event.x
                        lastY = event.y
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                if (event.actionMasked == MotionEvent.ACTION_UP) {
                    fingerCount = 0
                    activePointerId = -1
                    // Reset scroll state
                    if (isScrolling) {
                        isScrolling = false
                        scrollVelocityTracker = 0f
                        velocityY = 0f
                    }
                } else if (event.actionMasked == MotionEvent.ACTION_POINTER_UP) {
                    fingerCount = event.pointerCount - 1
                }
                // Check if it's a tap (minimal movement)
                val totalMovementX = abs(event.x - startX)
                val totalMovementY = abs(event.y - startY)

                if (totalMovementX < touchSlop && totalMovementY < touchSlop) {
                    listener?.onTap()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun Int.dpToPx(): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            resources.displayMetrics
        )
    }
}