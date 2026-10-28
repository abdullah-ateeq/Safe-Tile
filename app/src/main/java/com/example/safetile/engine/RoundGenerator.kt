package com.example.safetile.engine

import com.example.safetile.model.Difficulty
import com.example.safetile.model.RoundConfig
import com.example.safetile.model.TileData
import com.example.safetile.model.TileState
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object RoundGenerator {

    fun createRoundConfig(difficulty: Difficulty, round: Int): RoundConfig {
        val safeRound = max(1, round)

        val (dimension, dangerRatio, baseMemorizeSec, baseRoundSec, perTileSec, maxRoundSec) = when (difficulty) {
            Difficulty.EASY -> {
                val size = when {
                    safeRound <= 3 -> 3
                    safeRound <= 6 -> 4
                    else -> 5
                }
                ConfigParams(
                    size = size,
                    dangerRatio = 0.22,
                    baseMemorizeSec = 2.5,
                    baseRoundSec = 8.0,
                    perTileSec = 1.0,
                    maxRoundSec = 22.0
                )
            }
            Difficulty.MEDIUM -> {
                val size = when {
                    safeRound <= 3 -> 4
                    safeRound <= 6 -> 5
                    else -> 6
                }
                ConfigParams(
                    size = size,
                    dangerRatio = 0.28,
                    baseMemorizeSec = 2.0,
                    baseRoundSec = 7.0,
                    perTileSec = 0.8,
                    maxRoundSec = 18.0
                )
            }
            Difficulty.HARD -> {
                val size = when {
                    safeRound <= 3 -> 5
                    safeRound <= 6 -> 6
                    else -> 7
                }
                ConfigParams(
                    size = size,
                    dangerRatio = 0.35,
                    baseMemorizeSec = 1.5,
                    baseRoundSec = 6.0,
                    perTileSec = 0.65,
                    maxRoundSec = 15.0
                )
            }
        }

        val totalTiles = dimension * dimension
        // Compute dangerous count based on difficulty and round increment
        val rawDanger = (totalTiles * dangerRatio).toInt()
        val dangerBonus = ((safeRound - 1) % 3) // slightly increase within bracket
        val dangerousCount = (rawDanger + dangerBonus).coerceIn(1, totalTiles - 1)

        val safeTiles = totalTiles - dangerousCount

        // Memorize timer: gradually decrease by 0.1s every round down to 1.0s
        val memorizeReduction = (safeRound - 1) * 0.1
        val memorizeTimeSec = max(1.0, baseMemorizeSec - memorizeReduction)
        val memorizeTimeMs = (memorizeTimeSec * 1000).toLong()

        // Round timer formula: baseTime + safeTiles * timePerTile, clamped between 8.0 and maxRoundSec
        val calculatedRoundTime = baseRoundSec + (safeTiles * perTileSec)
        val clampedRoundTime = calculatedRoundTime.coerceIn(8.0, maxRoundSec)
        val roundTimeMs = (clampedRoundTime * 1000).toLong()

        return RoundConfig(
            round = safeRound,
            rows = dimension,
            columns = dimension,
            dangerousCount = dangerousCount,
            memorizeTimeMs = memorizeTimeMs,
            roundTimeMs = roundTimeMs
        )
    }

    fun generateDangerIndices(
        totalTiles: Int,
        dangerCount: Int,
        random: Random,
        previousDangerIndices: Set<Int>? = null
    ): Set<Int> {
        require(dangerCount in 1 until totalTiles) {
            "Danger count must be between 1 and ${totalTiles - 1}"
        }

        val maxAttempts = 20
        var attempts = 0
        var result: Set<Int>

        do {
            val indices = mutableSetOf<Int>()
            while (indices.size < dangerCount) {
                indices.add(random.nextInt(totalTiles))
            }
            result = indices
            attempts++
        } while (
            previousDangerIndices != null &&
            previousDangerIndices.size == dangerCount &&
            result == previousDangerIndices &&
            attempts < maxAttempts &&
            totalTiles > dangerCount
        )

        return result
    }

    fun generateRound(
        config: RoundConfig,
        random: Random = Random.Default,
        previousDangerIndices: Set<Int>? = null,
        initialState: TileState = TileState.SAFE_REVEALED
    ): List<TileData> {
        val total = config.totalTiles
        val dangerIndices = generateDangerIndices(
            totalTiles = total,
            dangerCount = config.dangerousCount,
            random = random,
            previousDangerIndices = previousDangerIndices
        )

        return (0 until total).map { index ->
            val row = index / config.columns
            val col = index % config.columns
            val isDanger = index in dangerIndices
            val state = if (isDanger) TileState.DANGER_REVEALED else TileState.SAFE_REVEALED

            TileData(
                index = index,
                row = row,
                column = col,
                isDangerous = isDanger,
                state = state,
                tapped = false,
                isWronglyTapped = false
            )
        }
    }

    private data class ConfigParams(
        val size: Int,
        val dangerRatio: Double,
        val baseMemorizeSec: Double,
        val baseRoundSec: Double,
        val perTileSec: Double,
        val maxRoundSec: Double
    )
}
