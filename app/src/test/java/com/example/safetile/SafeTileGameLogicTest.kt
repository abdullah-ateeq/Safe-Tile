package com.example.safetile

import com.example.safetile.engine.RoundGenerator
import com.example.safetile.engine.ScoreCalculator
import com.example.safetile.model.Difficulty
import com.example.safetile.model.TileData
import com.example.safetile.model.TileState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SafeTileGameLogicTest {

    @Test
    fun testScoreCalculation_deterministicExampleFromPrompt() {
        // safeTiles = 10, remainingTime = 5, round = 3
        // Expected: base = 1000, speed = 100, multiplier = 1.10, roundScore = 1210
        val safeTiles = 10
        val remainingTime = 5.0
        val round = 3

        val base = ScoreCalculator.calculateBaseScore(safeTiles)
        val speed = ScoreCalculator.calculateSpeedBonus(remainingTime)
        val multiplier = ScoreCalculator.calculateRoundMultiplier(round)
        val roundScore = ScoreCalculator.calculateRoundScore(
            safeTiles = safeTiles,
            remainingTimeSeconds = remainingTime,
            round = round,
            streak = 0
        )

        assertEquals(1000, base)
        assertEquals(100, speed)
        assertEquals(1.10, multiplier, 0.001)
        assertEquals(1210, roundScore)
    }

    @Test
    fun testScoreCalculation_secondExampleFromPrompt() {
        // safeTiles = 12, remainingTime = 5, round = 5
        // Expected: base = 1200, speed = 100, multiplier = 1.20, roundScore = 1560
        val safeTiles = 12
        val remainingTime = 5.0
        val round = 5

        val base = ScoreCalculator.calculateBaseScore(safeTiles)
        val speed = ScoreCalculator.calculateSpeedBonus(remainingTime)
        val multiplier = ScoreCalculator.calculateRoundMultiplier(round)
        val roundScore = ScoreCalculator.calculateRoundScore(
            safeTiles = safeTiles,
            remainingTimeSeconds = remainingTime,
            round = round,
            streak = 0
        )

        assertEquals(1200, base)
        assertEquals(100, speed)
        assertEquals(1.20, multiplier, 0.001)
        assertEquals(1560, roundScore)
    }

    @Test
    fun testStreakBonusClamping() {
        // Streak bonus is min(streak * 5%, 50%)
        assertEquals(0, ScoreCalculator.calculateStreakBonusPercent(0))
        assertEquals(10, ScoreCalculator.calculateStreakBonusPercent(2))
        assertEquals(25, ScoreCalculator.calculateStreakBonusPercent(5))
        assertEquals(50, ScoreCalculator.calculateStreakBonusPercent(10))
        assertEquals(50, ScoreCalculator.calculateStreakBonusPercent(20)) // Clamped at 50%
    }

    @Test
    fun testGridAndSafeTileCalculation() {
        // For a 4 x 5 grid, total = 20, danger = 5 -> safe = 15
        val rows = 4
        val cols = 5
        val totalTiles = rows * cols
        val dangerous = 5
        val safe = totalTiles - dangerous

        assertEquals(20, totalTiles)
        assertEquals(15, safe)
    }

    @Test
    fun testDuplicateDangerPrevention() {
        val random = Random(42)
        val totalTiles = 16
        val dangerCount = 5

        val dangerIndices = RoundGenerator.generateDangerIndices(
            totalTiles = totalTiles,
            dangerCount = dangerCount,
            random = random
        )

        // All indices must be unique
        assertEquals(dangerCount, dangerIndices.size)
        // All indices must be strictly within [0, totalTiles)
        assertTrue(dangerIndices.all { it in 0 until totalTiles })
    }

    @Test
    fun testDangerLayoutChangesBetweenConsecutiveRounds() {
        val random = Random(12345)
        val config = RoundGenerator.createRoundConfig(Difficulty.EASY, round = 1)

        val roundA = RoundGenerator.generateRound(config, random = random)
        val dangerA = roundA.filter { it.isDangerous }.map { it.index }.toSet()

        val roundB = RoundGenerator.generateRound(config, random = random, previousDangerIndices = dangerA)
        val dangerB = roundB.filter { it.isDangerous }.map { it.index }.toSet()

        assertNotEquals("Consecutive rounds must not have identical danger layouts", dangerA, dangerB)
    }

    @Test
    fun testDifficultyProgressionAndClamping() {
        // EASY
        val easyR1 = RoundGenerator.createRoundConfig(Difficulty.EASY, 1)
        assertEquals(3, easyR1.rows)
        assertEquals(3, easyR1.columns)
        assertTrue(easyR1.safeTilesCount >= 1)
        assertTrue(easyR1.roundTimeMs >= 8000L)
        assertTrue(easyR1.roundTimeMs <= 22000L)
        assertTrue(easyR1.memorizeTimeMs >= 1000L)

        // EASY Round 10: dimension should clamp at max 5x5
        val easyR10 = RoundGenerator.createRoundConfig(Difficulty.EASY, 10)
        assertEquals(5, easyR10.rows)
        assertEquals(5, easyR10.columns)
        assertEquals(1600L, easyR10.memorizeTimeMs)

        // EASY Round 20: should hit minimum clamp 1.0s (1000ms)
        val easyR20 = RoundGenerator.createRoundConfig(Difficulty.EASY, 20)
        assertEquals(1000L, easyR20.memorizeTimeMs) // Min memorize time clamp 1.0s

        // MEDIUM
        val medR1 = RoundGenerator.createRoundConfig(Difficulty.MEDIUM, 1)
        assertEquals(4, medR1.rows)
        assertEquals(4, medR1.columns)
        assertTrue(medR1.roundTimeMs <= 18000L)

        // HARD
        val hardR1 = RoundGenerator.createRoundConfig(Difficulty.HARD, 1)
        assertEquals(5, hardR1.rows)
        assertEquals(5, hardR1.columns)
        assertTrue(hardR1.roundTimeMs <= 15000L)

        val hardR20 = RoundGenerator.createRoundConfig(Difficulty.HARD, 20)
        assertEquals(7, hardR20.rows)
        assertEquals(7, hardR20.columns)
        assertTrue(hardR20.rows <= 7) // Grid size must not exceed 7x7
        assertTrue(hardR20.safeTilesCount >= 1)
        assertEquals(1000L, hardR20.memorizeTimeMs)
    }

    @Test
    fun testSafeTileTappingLogic() {
        val config = RoundGenerator.createRoundConfig(Difficulty.EASY, 1)
        val tiles = RoundGenerator.generateRound(config, random = Random(999))

        val safeTiles = tiles.filter { !it.isDangerous }
        val dangerTiles = tiles.filter { it.isDangerous }

        assertTrue("Must have safe tiles", safeTiles.isNotEmpty())
        assertTrue("Must have danger tiles", dangerTiles.isNotEmpty())
        assertEquals(config.totalTiles, safeTiles.size + dangerTiles.size)

        // Simulate safe tapping
        var tappedCount = 0
        for (safeTile in safeTiles) {
            val updated = safeTile.copy(state = TileState.TAPPED_SAFE, tapped = true)
            assertTrue(updated.tapped)
            assertEquals(TileState.TAPPED_SAFE, updated.state)
            tappedCount++
        }

        assertEquals(config.safeTilesCount, tappedCount)
    }
}
