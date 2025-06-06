package com.harimoradiya.wifimouseclientandroid.ui.frags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.harimoradiya.wifimouseclientandroid.databinding.FragmentKeyboardBinding
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.activityViewModels
import com.harimoradiya.wifimouseclientandroid.ui.activities.MainActivity
import com.harimoradiya.wifimouseclientandroid.ui.activities.MainActivity.Companion.connectionManager
import com.harimoradiya.wifimouseclientandroid.viewmodel.ConnectionStatusViewModel

class KeyboardFragment : Fragment() {
    val TAG = KeyboardFragment::class.simpleName
    private var _binding: FragmentKeyboardBinding? = null
    private val binding get() = _binding!!
    private val connectionStatusViewModel: ConnectionStatusViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKeyboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")

        // Setup keyboard input field with TextWatcher for real-time input
        binding.keyboardInput.addTextChangedListener(object : TextWatcher {
            private var previousText = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                previousText = s?.toString() ?: ""
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val currentText = s?.toString() ?: ""
                if (currentText.length > previousText.length) {
                    // New character typed
                    val newChar = currentText.last()
                    connectionManager?.sendCommand("KEY:TYPE:$newChar")
                } else if (currentText.length < previousText.length) {
                    // Backspace pressed
                    connectionManager?.sendCommand("KEY:SPECIAL:BACKSPACE")
                }
            }
        })

        // Special keys
        binding.btnEnter.setOnClickListener {
            performAction {
                connectionManager?.sendCommand("KEY:SPECIAL:ENTER")
                binding.keyboardInput.text?.clear()
            }
        }

        binding.btnBackspace.setOnClickListener {
            performAction {

                connectionManager?.sendCommand("KEY:SPECIAL:BACKSPACE")
            }
        }

        binding.btnSpace.setOnClickListener {
            performAction {
                connectionManager?.sendCommand("KEY:SPECIAL:SPACE")
                binding.keyboardInput.append(" ")
            }
        }

        binding.btnTab.setOnClickListener {
            performAction {
                connectionManager?.sendCommand("KEY:SPECIAL:TAB")
                binding.keyboardInput.append("\t")
            }
        }

        // Arrow keys with updated command format
        binding.btnUp.setOnClickListener {
            performAction {
                connectionManager?.sendCommand("KEY:SPECIAL:UP")
            }
        }

        binding.btnDown.setOnClickListener {
            performAction {
                connectionManager?.sendCommand("KEY:SPECIAL:DOWN")
            }
        }

        binding.btnLeft.setOnClickListener {
            performAction {
                connectionManager?.sendCommand("KEY:SPECIAL:LEFT")
            }
        }

        binding.btnRight.setOnClickListener {
            performAction {
                connectionManager?.sendCommand("KEY:SPECIAL:RIGHT")
            }
        }

        // Function keys
        binding.btnEsc.setOnClickListener {
            performAction {
                connectionManager?.sendCommand("KEY:FUNCTION:ESC")
            }
        }

        // Modifier keys
        binding.btnCtrl.setOnClickListener {
            performAction {
                connectionManager?.sendCommand("KEY:SPECIAL:CONTROL")
            }
        }

        binding.btnAlt.setOnClickListener {
            performAction {
                connectionManager?.sendCommand("KEY:SPECIAL:ALT")
            }
        }

        binding.btnShift.setOnClickListener {
            performAction {
            connectionManager?.sendCommand("KEY:SPECIAL:SHIFT")
            }
        }


    }

    private fun performAction(action: () -> Unit) {
        if (connectionStatusViewModel.isConnected.value == true) {
            Log.d("KeyboardFragment", "performAction: ")
            action()
        } else {
            Log.d(TAG, "performAction: false")
            (activity as? MainActivity)?.showSnackbar("Please connect first!")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}