package com.example.handcontrol

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.*


class MainActivity : AppCompatActivity() {
    private val permission = 100
    private lateinit var returnedText: TextView
    private lateinit var recordButton: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var speech: SpeechRecognizer
    private lateinit var recognizerIntent: Intent
    private lateinit var langSelectRadioGroup: RadioGroup
    private var logTag = "VoiceRecognitionActivity"

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "VoiceRecognition"
        returnedText = findViewById(R.id.textView)
        progressBar = findViewById(R.id.progressBar)
        recordButton = findViewById(R.id.imageView)
        langSelectRadioGroup = findViewById(R.id.radioLang)

        progressBar.visibility = View.VISIBLE
        progressBar.isIndeterminate = false
        progressBar.max = 100
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            progressBar.min = 0
        }
        speech = SpeechRecognizer.createSpeechRecognizer(this)
        returnedText.text = ""
        Log.i(logTag, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this))
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "RU-ru")
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "RU-ru")
        langSelectRadioGroup.setOnCheckedChangeListener { group, id ->
            recognizerIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE, when (findViewById<RadioButton>(id).text) {
                    "Русский" -> "RU-ru"
                    else -> "US-en"
                }
            )
        }
        recognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        recordButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        permission
                    )
                }
                MotionEvent.ACTION_UP -> {
                    speech.stopListening()
                }
            }
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permission -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager
                    .PERMISSION_GRANTED
            ) {
                speech.setRecognitionListener(getListener())
                speech.startListening(recognizerIntent)
            } else {
                Toast.makeText(
                    this@MainActivity, "Permission Denied!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        speech.destroy()
        Log.i(logTag, "destroy")
    }

    private fun getErrorText(error: Int): String {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Didn't understand, please try again."
        }
    }

    private fun getListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Unit //"Not yet implemented")
        }

        override fun onBeginningOfSpeech() {
            Unit //"Not yet implemented")
        }

        override fun onRmsChanged(rmsdB: Float) {
            // if rmsDb is within -10 .. 10, then move it to positive numbers and stretch to 100
            progressBar.progress = ((rmsdB + 10) / 20 * 100).toInt()
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            Unit //"Not yet implemented")
        }

        override fun onEndOfSpeech() {
            Unit //"Not yet implemented")
        }

        override fun onError(error: Int) {
            if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_CLIENT)
                return // just ignore
            val errorMessage: String = getErrorText(error)
            Log.d(logTag, "FAILED $errorMessage")
            returnedText.text = errorMessage
        }

        override fun onResults(results: Bundle?) {
            results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let {
                returnedText.text = it
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            Unit //"Not yet implemented")
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            Unit //"Not yet implemented")
        }

    }
}