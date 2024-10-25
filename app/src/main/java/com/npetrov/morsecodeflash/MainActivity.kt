package com.npetrov.morsecodeflash

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

private const val TAG = "MainActivity"
private const val INITIAL_DURATION = 200

class MainActivity : AppCompatActivity() {
    private var pulseDuration = 200

    private lateinit var tvTopLabel: TextView
    private lateinit var textInputBox: EditText
    private lateinit var tvCodePreview: TextView
    private lateinit var seekBarDuration: SeekBar
    private lateinit var tvDurationLength: TextView
    private lateinit var playButton: ImageButton

    private val morseCodeMap = mapOf(
        'A' to "•–",    'B' to "–•••",  'C' to "–•–•",  'D' to "–••",   'E' to "•",
        'F' to "••–•",  'G' to "––•",   'H' to "••••",  'I' to "••",    'J' to "•–––",
        'K' to "–•–",   'L' to "•–••",  'M' to "––",    'N' to "–•",    'O' to "–––",
        'P' to "•––•",  'Q' to "––•–",  'R' to "•–•",   'S' to "•••",   'T' to "–",
        'U' to "••–",   'V' to "•••–",  'W' to "•––",   'X' to "–••–",  'Y' to "–•––",
        'Z' to "––••",  '1' to "•––––", '2' to "••–––", '3' to "•••––", '4' to "••••–",
        '5' to "•••••", '6' to "–••••", '7' to "––•••", '8' to "–––••", '9' to "––––•",
        '0' to "–––––", ' ' to " "
    )
    private var morseCode = " "
    private var isFlashlightOn = false
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())


    fun convertTextToMorse(text: String): String {
        val code = text.uppercase().map {
            morseCodeMap[it] ?: "" // If character not found, use an empty string
        }.joinToString("   ") // Join each Morse code letter with 3 spaces
        return code
    }

    private fun toggleFlashlight(on: Boolean ) {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
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

    private fun sendMorseCode() {
        var delay = pulseDuration.toLong()
        for (symbol in morseCode) {
            when (symbol) {
                '•' -> {
                    // Flash for dot 1 x pulseDuration
                    handler.post({ toggleFlashlight(true) })
                    Log.d(TAG, "Flashlight on")
                    Thread.sleep(delay)
                    handler.post({ toggleFlashlight(false) })
                    Log.d(TAG, "Flashlight off")
                }
                '–' -> {
                    // Flash for dash 3 x pulseDuration
                    handler.post({ toggleFlashlight(true) })
                    Log.d(TAG, "Flashlight on")
                    Thread.sleep(3*delay)
                    handler.post({ toggleFlashlight(false) })
                    Log.d(TAG, "Flashlight off")
                }
                ' ' -> {
                    // delay for empty 1 x pulseDuration
                    Thread.sleep(delay)
                    Log.d(TAG, "Flashlight off")
                }
            }
        }

        playButton.setImageResource(android.R.drawable.ic_media_play)
        isPlaying = false
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        tvTopLabel = findViewById(R.id.tvTopLabel)
        textInputBox = findViewById(R.id.textInputBox)
        tvCodePreview = findViewById(R.id.tvCodePreview)
        seekBarDuration = findViewById(R.id.seekBarDuration)
        tvDurationLength = findViewById(R.id.tvDurationLength)
        playButton = findViewById(R.id.playButton)

        tvDurationLength.text = "${INITIAL_DURATION}ms"

        seekBarDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                //Log.i(TAG, "onProgressChanged $progress")
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
                playButton.setImageResource(android.R.drawable.ic_media_pause)
                isPlaying = true
                handler.post({sendMorseCode()})
            } else {
                playButton.setImageResource(android.R.drawable.ic_media_play)
                isPlaying = false
            }
        }
    }
}