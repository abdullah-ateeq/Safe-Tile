package com.example.safetile.engine

import kotlin.math.min
import kotlin.math.roundToInt

object ScoreCalculator {

    fun calculateRoundScore(
        safeTiles: Int,
        remainingTimeSeconds: Double,
        round: Int,
        streak: Int = 0
    ): Int {
        val baseScore = safeTiles * 100.0
        val clampedTime = remainingTimeSeconds.coerceAtLeast(0.0)
        val speedBonus = clampedTime * 20.0
        val roundMultiplier = 1.0 + ((round - 1).coerceAtLeast(0) * 0.05)
        val streakBonus = if (streak > 0) min(streak * 0.05, 0.50) else 0.0

        val scoreBeforeStreak = (baseScore + speedBonus) * roundMultiplier
        val finalScore = scoreBeforeStreak * (1.0 + streakBonus)
        return finalScore.roundToInt()
    }

    fun calculateBaseScore(safeTiles: Int): Int = safeTiles * 100

    fun calculateSpeedBonus(remainingTimeSeconds: Double): Int =
        (remainingTimeSeconds.coerceAtLeast(0.0) * 20.0).roundToInt()

    fun calculateRoundMultiplier(round: Int): Double =
        1.0 + ((round - 1).coerceAtLeast(0) * 0.05)

    fun calculateStreakBonusPercent(streak: Int): Int =
        (min(streak.coerceAtLeast(0) * 0.05, 0.50) * 100).roundToInt()
}
