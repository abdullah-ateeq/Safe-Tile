package com.example.safetile.engine

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundPlayer {

    var isSoundEnabled: Boolean = true

    private val audioScope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 44100

    fun playSafeTile() {
        if (!isSoundEnabled) return
        audioScope.launch {
            // Pleasant marimba-like bell tone (880 Hz decaying quickly)
            playTone(frequency = 880.0, durationMs = 70, decay = true)
        }
    }

    fun playDangerTile() {
        if (!isSoundEnabled) return
        audioScope.launch {
            // Low alert buzz
            playTone(frequency = 160.0, durationMs = 220, decay = false, waveType = WaveType.SQUARE)
        }
    }

    fun playTick() {
        if (!isSoundEnabled) return
        audioScope.launch {
            // Soft high woodblock click
            playTone(frequency = 1320.0, durationMs = 30, decay = true)
        }
    }

    fun playRoundComplete() {
        if (!isSoundEnabled) return
        audioScope.launch {
            // Cheerful ascending arpeggio (C5 -> E5 -> G5 -> C6)
            val notes = listOf(523.25, 659.25, 783.99, 1046.50)
            for (freq in notes) {
                playTone(frequency = freq, durationMs = 60, decay = true)
            }
        }
    }

    fun playGameOver() {
        if (!isSoundEnabled) return
        audioScope.launch {
            // Descending tone
            val notes = listOf(440.0, 370.0, 311.0, 220.0)
            for (freq in notes) {
                playTone(frequency = freq, durationMs = 80, decay = true)
            }
        }
    }

    private enum class WaveType {
        SINE,
        SQUARE
    }

    private fun playTone(
        frequency: Double,
        durationMs: Int,
        decay: Boolean,
        waveType: WaveType = WaveType.SINE
    ) {
        try {
            val numSamples = (durationMs * sampleRate) / 1000
            val buffer = ShortArray(numSamples)
            val angularFrequency = 2.0 * Math.PI * frequency / sampleRate

            for (i in 0 until numSamples) {
                val envelope = if (decay) {
                    val progress = i.toDouble() / numSamples
                    (1.0 - progress) * (1.0 - progress)
                } else {
                    1.0
                }

                val sample = when (waveType) {
                    WaveType.SINE -> sin(angularFrequency * i)
                    WaveType.SQUARE -> if (sin(angularFrequency * i) >= 0) 0.6 else -0.6
                }

                buffer[i] = (sample * envelope * Short.MAX_VALUE * 0.45).toInt().toShort()
            }

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val track = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()

            // Release after playing
            Thread.sleep(durationMs.toLong() + 20)
            track.stop()
            track.release()
        } catch (_: Throwable) {
            // Gracefully ignore audio device exceptions
        }
    }
}
