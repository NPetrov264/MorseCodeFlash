package com.npetrov.morsecodeflash.fragments

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import com.npetrov.morsecodeflash.R

private const val TAG = "MainActivity"
private const val INITIAL_DURATION = 200

class SendMessage : Fragment() {
    private var pulseDuration = INITIAL_DURATION

    private lateinit var tvTopLabel: TextView
    private lateinit var textInputBox: EditText
    private lateinit var tvCodePreview: TextView
    private lateinit var seekBarDuration: SeekBar
    private lateinit var tvDurationLength: TextView
    private lateinit var playButton: ImageButton
    private lateinit var switchRepeat: Switch
    private lateinit var sosButton: ImageButton
    private lateinit var ivFlashLight: ImageView

    private val morseCodeMap = mapOf(
        'A' to "•–",    'B' to "–•••",  'C' to "–•–•",  'D' to "–••",   'E' to "•",
        'F' to "••–•",  'G' to "––•",   'H' to "••••",  'I' to "••",    'J' to "•–––",
        'K' to "–•–",   'L' to "•–••",  'M' to "––",    'N' to "–•",    'O' to "–––",
        'P' to "•––•",  'Q' to "––•–",  'R' to "•–•",   'S' to "•••",   'T' to "–",
        'U' to "••–",   'V' to "•••–",  'W' to "•––",   'X' to "–••–",  'Y' to "–•––",
        'Z' to "––••",  '1' to "•––––", '2' to "••–––", '3' to "•••––", '4' to "••••–",
        '5' to "•••••", '6' to "–••••", '7' to "––•••", '8' to "–––••", '9' to "––––•",
        '0' to "–––––", ' ' to "  "
    )
    private var morseCode = " "
    private var isFlashlightOn = false
    private var isPlaying = false
    private var isRepeating = false
    private val handler = Handler(Looper.getMainLooper())


    fun convertTextToMorse(text: String): String {
        val code = text.uppercase().map {
            morseCodeMap[it] ?: "" // If character not found, use an empty string
        }.joinToString("  ") // Join each Morse code letter with 2 spaces
        return code
    }

    private fun toggleFlashlight(on: Boolean ) {
        val cameraManager = requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]
        if (on) {
            try {
                cameraManager.setTorchMode(cameraId, true)
                isFlashlightOn = true

            } catch (e: CameraAccessException) {
                Log.e(TAG, "Failed to turn on flashlight")
            }
        } else {
            try {
                cameraManager.setTorchMode(cameraId, false)
                isFlashlightOn = false
            } catch (e: CameraAccessException) {
                Log.e(TAG, "Failed to turn off flashlight")
            }
        }
    }

    private fun sendMorseCode(isFirstRun: Boolean) {
        var delay = pulseDuration.toLong()
        if (isFirstRun || isRepeating) {
            for (symbol in morseCode) {
                when (symbol) {
                    '•' -> {
                        // Flash for dot 1 x pulseDuration
                        handler.postDelayed({ toggleFlashlight(true) }, delay)
                        handler.postDelayed(
                            { ivFlashLight.setImageResource(R.drawable.flashlight_on) },
                            delay
                        )
                        Log.d(TAG, "Flashlight on")
                        delay += pulseDuration
                        handler.postDelayed({ toggleFlashlight(false) }, delay)
                        handler.postDelayed(
                            { ivFlashLight.setImageResource(R.drawable.flashlight_off) },
                            delay
                        )
                        Log.d(TAG, "Flashlight off")
                        delay += pulseDuration
                    }

                    '–' -> {
                        // Flash for dash 3 x pulseDuration
                        handler.postDelayed({ toggleFlashlight(true) }, delay)
                        handler.postDelayed(
                            { ivFlashLight.setImageResource(R.drawable.flashlight_on) },
                            delay
                        )
                        Log.d(TAG, "Flashlight on")
                        delay += 3 * pulseDuration
                        handler.postDelayed({ toggleFlashlight(false) }, delay)
                        handler.postDelayed(
                            { ivFlashLight.setImageResource(R.drawable.flashlight_off) },
                            delay
                        )
                        Log.d(TAG, "Flashlight off")
                        delay += pulseDuration
                    }

                    ' ' -> {
                        // delay for space 1 x pulseDuration
                        delay += pulseDuration
                    }
                }
            }
            handler.postDelayed({sendMorseCode(false)}, delay + 10*pulseDuration) // add delay between repeats
            if ( !isRepeating) {
                handler.postDelayed({ playButton.setImageResource(android.R.drawable.ic_media_play) }, delay)
                handler.postDelayed({ isPlaying = false }, delay)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_send_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTopLabel = view.findViewById(R.id.tvTopLabel)
        textInputBox = view.findViewById(R.id.textInputBox)
        tvCodePreview = view.findViewById(R.id.tvCodePreview)
        seekBarDuration = view.findViewById(R.id.seekBarDuration)
        tvDurationLength = view.findViewById(R.id.tvDurationLength)
        playButton = view.findViewById(R.id.playButton)
        switchRepeat = view.findViewById(R.id.switchRepeat)
        sosButton = view.findViewById(R.id.sosButton)
        ivFlashLight = view.findViewById(R.id.ivFlashLight)

        tvDurationLength.text = "${INITIAL_DURATION}ms"

        seekBarDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                pulseDuration = INITIAL_DURATION + progress
                tvDurationLength.text = "${pulseDuration}ms"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        textInputBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                Log.i(TAG, "afterTextChanged $s")
                morseCode = convertTextToMorse("$s")
                tvCodePreview.text = morseCode
                Log.i(TAG, morseCode)
            }
        })

        playButton.setOnClickListener {
            if (!isPlaying) {
                isPlaying = true
                playButton.setImageResource(android.R.drawable.ic_media_pause)
                sendMorseCode(true)
            } else {
                handler.removeCallbacksAndMessages(null) // Cancel all scheduled tasks
                handler.post { toggleFlashlight(false) } // turn off flashlight
                isPlaying = false
                playButton.setImageResource(android.R.drawable.ic_media_play)
            }
        }

        sosButton.setOnClickListener {
            handler.removeCallbacksAndMessages(null) // Cancel all scheduled tasks
            handler.post { toggleFlashlight(false) }
            textInputBox.setText("SOS")
            morseCode = "•••  –––  •••"
            isPlaying = true
            playButton.setImageResource(android.R.drawable.ic_media_pause)
            switchRepeat.setChecked(true)
            sendMorseCode(true)
        }

        switchRepeat.setOnCheckedChangeListener { _, isChecked ->
            isRepeating = isChecked
        }

    }

}