package com.example.safetile.engine

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticController(private val context: Context) {

    var isHapticEnabled: Boolean = true

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun vibrateSafeTap() {
        if (!isHapticEnabled) return
        try {
            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    } else {
                        v.vibrate(VibrationEffect.createOneShot(18, 120))
                    }
                }
            }
        } catch (_: Throwable) {}
    }

    fun vibrateDangerTap() {
        if (!isHapticEnabled) return
        try {
            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                    } else {
                        val pattern = longArrayOf(0, 70, 40, 120)
                        val amplitudes = intArrayOf(0, 200, 0, 255)
                        v.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
                    }
                }
            }
        } catch (_: Throwable) {}
    }

    fun vibrateRoundComplete() {
        if (!isHapticEnabled) return
        try {
            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    val pattern = longArrayOf(0, 30, 40, 50)
                    val amplitudes = intArrayOf(0, 160, 0, 220)
                    v.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
                }
            }
        } catch (_: Throwable) {}
    }

    fun vibrateGameOver() {
        if (!isHapticEnabled) return
        try {
            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    val pattern = longArrayOf(0, 120, 60, 200)
                    val amplitudes = intArrayOf(0, 220, 0, 255)
                    v.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
                }
            }
        } catch (_: Throwable) {}
    }
}
