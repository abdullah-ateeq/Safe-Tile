package com.example.safetile.viewmodel

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.safetile.data.AppThemeMode
import com.example.safetile.data.UserPreferencesRepository
import com.example.safetile.data.UserSettings
import com.example.safetile.engine.HapticController
import com.example.safetile.engine.RoundGenerator
import com.example.safetile.engine.ScoreCalculator
import com.example.safetile.engine.SoundPlayer
import com.example.safetile.model.Difficulty
import com.example.safetile.model.GameState
import com.example.safetile.model.GameStatistics
import com.example.safetile.model.RoundConfig
import com.example.safetile.model.SafeTileGameResult
import com.example.safetile.model.TileData
import com.example.safetile.model.TileState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max

data class GameUiState(
    val difficulty: Difficulty = Difficulty.EASY,
    val round: Int = 1,
    val score: Int = 0,
    val currentStreak: Int = 0,
    val bestStreakInGame: Int = 0,
    val gameState: GameState = GameState.READY,
    val roundConfig: RoundConfig = RoundGenerator.createRoundConfig(Difficulty.EASY, 1),
    val tiles: List<TileData> = emptyList(),
    val safeTilesTappedInRound: Int = 0,
    val remainingTimeSeconds: Double = 0.0,
    val memorizeRemainingSeconds: Double = 0.0,
    val isTimerUrgent: Boolean = false,
    val transitionMessage: String? = null,
    val lastResult: SafeTileGameResult? = null,
    val isTimeout: Boolean = false,
    val wrongTileIndex: Int? = null,
    val perfectRoundsCount: Int = 0,
    val totalSafeTappedInGame: Int = 0
)

class SafeTileViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = UserPreferencesRepository(application)
    val soundPlayer = SoundPlayer()
    val hapticController = HapticController(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val settings: StateFlow<UserSettings> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = UserSettings()
    )

    private val _statistics = MutableStateFlow(GameStatistics())
    val statistics: StateFlow<GameStatistics> = _statistics.asStateFlow()

    private var previousDangerIndices: Set<Int>? = null
    private var timerJob: Job? = null
    private var memorizeJob: Job? = null

    // Monotonic timer tracking
    private var roundDurationMs: Long = 0
    private var roundStartTime: Long = 0
    private var pausedRemainingMs: Long = 0
    private var stateBeforePause: GameState = GameState.READY

    init {
        // Sync sound & haptic settings with soundPlayer and hapticController
        viewModelScope.launch {
            settings.collect { currentSettings ->
                soundPlayer.isSoundEnabled = currentSettings.soundEnabled
                hapticController.isHapticEnabled = currentSettings.hapticsEnabled
            }
        }

        // Load stats for current difficulty
        viewModelScope.launch {
            uiState.collect { state ->
                repository.getStatsFlow(state.difficulty).collect { stats ->
                    _statistics.value = stats
                }
            }
        }
    }

    fun selectDifficulty(difficulty: Difficulty) {
        if (_uiState.value.gameState == GameState.READY ||
            _uiState.value.gameState == GameState.GAME_OVER
        ) {
            _uiState.update { it.copy(difficulty = difficulty) }
            viewModelScope.launch {
                repository.getStatsFlow(difficulty).collect { stats ->
                    _statistics.value = stats
                }
            }
        }
    }

    fun startGame() {
        stopTimers()
        val difficulty = _uiState.value.difficulty
        val initialConfig = RoundGenerator.createRoundConfig(difficulty, 1)
        val initialTiles = RoundGenerator.generateRound(initialConfig, previousDangerIndices = null)
        previousDangerIndices = initialTiles.filter { it.isDangerous }.map { it.index }.toSet()

        _uiState.value = GameUiState(
            difficulty = difficulty,
            round = 1,
            score = 0,
            currentStreak = 0,
            bestStreakInGame = 0,
            gameState = GameState.MEMORIZE,
            roundConfig = initialConfig,
            tiles = initialTiles,
            safeTilesTappedInRound = 0,
            memorizeRemainingSeconds = initialConfig.memorizeTimeMs / 1000.0,
            remainingTimeSeconds = initialConfig.roundTimeMs / 1000.0,
            isTimerUrgent = false,
            transitionMessage = null,
            lastResult = null,
            isTimeout = false,
            wrongTileIndex = null,
            perfectRoundsCount = 0,
            totalSafeTappedInGame = 0
        )

        startMemorizePhase(initialConfig.memorizeTimeMs)
    }

    private fun startMemorizePhase(memorizeDurationMs: Long) {
        stopTimers()
        _uiState.update {
            it.copy(
                gameState = GameState.MEMORIZE,
                memorizeRemainingSeconds = memorizeDurationMs / 1000.0,
                // Reveal danger and safe tiles during memorize
                tiles = it.tiles.map { tile ->
                    tile.copy(
                        state = if (tile.isDangerous) TileState.DANGER_REVEALED else TileState.SAFE_REVEALED,
                        tapped = false,
                        isWronglyTapped = false
                    )
                }
            )
        }

        memorizeJob = viewModelScope.launch {
            val start = SystemClock.elapsedRealtime()
            val end = start + memorizeDurationMs

            while (true) {
                val now = SystemClock.elapsedRealtime()
                val remaining = max(0L, end - now)
                _uiState.update {
                    it.copy(memorizeRemainingSeconds = remaining / 1000.0)
                }

                if (remaining <= 0) break
                delay(50)
            }

            // Memorize phase complete -> start Playing phase!
            startPlayingPhase()
        }
    }

    private fun startPlayingPhase() {
        val config = _uiState.value.roundConfig
        roundDurationMs = config.roundTimeMs
        pausedRemainingMs = roundDurationMs

        // Hide tiles
        _uiState.update { state ->
            state.copy(
                gameState = GameState.PLAYING,
                memorizeRemainingSeconds = 0.0,
                remainingTimeSeconds = roundDurationMs / 1000.0,
                isTimerUrgent = false,
                tiles = state.tiles.map { tile ->
                    tile.copy(
                        state = if (tile.isDangerous) TileState.DANGER_HIDDEN else TileState.SAFE_HIDDEN,
                        tapped = false
                    )
                }
            )
        }

        startMonotonicRoundTimer(roundDurationMs)
    }

    private fun startMonotonicRoundTimer(durationMs: Long) {
        timerJob?.cancel()
        roundStartTime = SystemClock.elapsedRealtime()
        val targetEndTime = roundStartTime + durationMs

        timerJob = viewModelScope.launch {
            var lastTickSecond = -1

            while (true) {
                val now = SystemClock.elapsedRealtime()
                val remainingMs = max(0L, targetEndTime - now)
                pausedRemainingMs = remainingMs
                val remainingSec = remainingMs / 1000.0
                val isUrgent = remainingSec <= 5.0 && remainingSec > 0.0

                _uiState.update {
                    it.copy(
                        remainingTimeSeconds = remainingSec,
                        isTimerUrgent = isUrgent
                    )
                }

                val currentSecInt = remainingSec.toInt()
                if (isUrgent && currentSecInt != lastTickSecond) {
                    lastTickSecond = currentSecInt
                    soundPlayer.playTick()
                }

                if (remainingMs <= 0) {
                    onTimeout()
                    break
                }
                delay(33) // ~30 fps updates for smooth countdown
            }
        }
    }

    fun onTileClick(clickedTile: TileData) {
        val currentState = _uiState.value
        if (currentState.gameState != GameState.PLAYING) return
        if (clickedTile.tapped) return // Ignore already tapped tiles

        if (clickedTile.isDangerous) {
            // Hit danger!
            onDangerHit(clickedTile)
        } else {
            // Safe tile tapped!
            onSafeTileTapped(clickedTile)
        }
    }

    private fun onSafeTileTapped(tile: TileData) {
        hapticController.vibrateSafeTap()
        soundPlayer.playSafeTile()

        val updatedTiles = _uiState.value.tiles.map {
            if (it.index == tile.index) {
                it.copy(
                    state = TileState.TAPPED_SAFE,
                    tapped = true
                )
            } else it
        }

        val newSafeTappedCount = _uiState.value.safeTilesTappedInRound + 1
        val newTotalSafeInGame = _uiState.value.totalSafeTappedInGame + 1
        val config = _uiState.value.roundConfig

        _uiState.update {
            it.copy(
                tiles = updatedTiles,
                safeTilesTappedInRound = newSafeTappedCount,
                totalSafeTappedInGame = newTotalSafeInGame
            )
        }

        // Check if all safe tiles for this round have been found!
        if (newSafeTappedCount >= config.safeTilesCount) {
            onRoundCompleted()
        }
    }

    private fun onRoundCompleted() {
        stopTimers()
        val currentState = _uiState.value
        val config = currentState.roundConfig

        hapticController.vibrateRoundComplete()
        soundPlayer.playRoundComplete()

        val remainingSec = currentState.remainingTimeSeconds
        val currentStreak = currentState.currentStreak + 1
        val bestStreak = max(currentState.bestStreakInGame, currentStreak)
        val perfectRounds = currentState.perfectRoundsCount + 1

        val roundScore = ScoreCalculator.calculateRoundScore(
            safeTiles = config.safeTilesCount,
            remainingTimeSeconds = remainingSec,
            round = currentState.round,
            streak = currentStreak
        )

        val newTotalScore = currentState.score + roundScore

        _uiState.update {
            it.copy(
                score = newTotalScore,
                currentStreak = currentStreak,
                bestStreakInGame = bestStreak,
                perfectRoundsCount = perfectRounds,
                gameState = GameState.ROUND_COMPLETE,
                transitionMessage = "ROUND ${currentState.round} COMPLETE!\nDANGER MOVED"
            )
        }

        // Brief transition (600ms) before next round begins
        viewModelScope.launch {
            delay(650)
            prepareNextRound()
        }
    }

    private fun prepareNextRound() {
        val currentState = _uiState.value
        val nextRoundNumber = currentState.round + 1
        val nextConfig = RoundGenerator.createRoundConfig(currentState.difficulty, nextRoundNumber)

        val nextTiles = RoundGenerator.generateRound(
            config = nextConfig,
            previousDangerIndices = previousDangerIndices
        )
        previousDangerIndices = nextTiles.filter { it.isDangerous }.map { it.index }.toSet()

        _uiState.update {
            it.copy(
                round = nextRoundNumber,
                roundConfig = nextConfig,
                tiles = nextTiles,
                safeTilesTappedInRound = 0,
                transitionMessage = null,
                gameState = GameState.MEMORIZE,
                memorizeRemainingSeconds = nextConfig.memorizeTimeMs / 1000.0,
                remainingTimeSeconds = nextConfig.roundTimeMs / 1000.0,
                isTimerUrgent = false
            )
        }

        startMemorizePhase(nextConfig.memorizeTimeMs)
    }

    private fun onDangerHit(clickedTile: TileData) {
        stopTimers()
        hapticController.vibrateDangerTap()
        soundPlayer.playDangerTile()

        revealAllDanger(wrongIndex = clickedTile.index)
        triggerGameOver(dangerHit = true, wrongIndex = clickedTile.index, isTimeout = false)
    }

    private fun onTimeout() {
        stopTimers()
        hapticController.vibrateGameOver()
        soundPlayer.playGameOver()

        revealAllDanger(wrongIndex = null)
        triggerGameOver(dangerHit = false, wrongIndex = null, isTimeout = true)
    }

    private fun revealAllDanger(wrongIndex: Int?) {
        _uiState.update { state ->
            state.copy(
                tiles = state.tiles.map { tile ->
                    when {
                        tile.index == wrongIndex -> tile.copy(
                            state = TileState.DANGER_REVEALED,
                            isWronglyTapped = true
                        )
                        tile.isDangerous -> tile.copy(
                            state = TileState.DANGER_REVEALED
                        )
                        tile.tapped -> tile.copy(
                            state = TileState.TAPPED_SAFE
                        )
                        else -> tile.copy(
                            state = TileState.SAFE_REVEALED
                        )
                    }
                }
            )
        }
    }

    private fun triggerGameOver(dangerHit: Boolean, wrongIndex: Int?, isTimeout: Boolean) {
        val currentState = _uiState.value

        val roundsSurvived = max(0, currentState.round - 1)
        val dangerAvoided = currentState.roundConfig.dangerousCount * roundsSurvived

        viewModelScope.launch {
            val (isNewScore, isNewRound) = repository.recordGameResult(
                difficulty = currentState.difficulty,
                score = currentState.score,
                round = currentState.round,
                streak = currentState.bestStreakInGame,
                safeTilesTapped = currentState.totalSafeTappedInGame
            )

            val result = SafeTileGameResult(
                difficulty = currentState.difficulty,
                roundsSurvived = roundsSurvived,
                safeTilesTapped = currentState.totalSafeTappedInGame,
                dangerTilesAvoided = dangerAvoided,
                dangerHit = dangerHit,
                perfectRounds = currentState.perfectRoundsCount,
                bestStreak = currentState.bestStreakInGame,
                score = currentState.score,
                wrongTileIndex = wrongIndex,
                isNewBestScore = isNewScore,
                isNewBestRound = isNewRound
            )

            // Let the player see the revealed board for a brief instant before navigating
            delay(1200)

            _uiState.update {
                it.copy(
                    gameState = GameState.GAME_OVER,
                    lastResult = result,
                    wrongTileIndex = wrongIndex,
                    isTimeout = isTimeout,
                    currentStreak = 0
                )
            }
        }
    }

    fun pauseGame() {
        val currentState = _uiState.value
        if (currentState.gameState == GameState.PLAYING || currentState.gameState == GameState.MEMORIZE) {
            stateBeforePause = currentState.gameState
            stopTimers()
            _uiState.update { it.copy(gameState = GameState.PAUSED) }
        }
    }

    fun resumeGame() {
        val currentState = _uiState.value
        if (currentState.gameState == GameState.PAUSED) {
            when (stateBeforePause) {
                GameState.PLAYING -> {
                    _uiState.update { it.copy(gameState = GameState.PLAYING) }
                    startMonotonicRoundTimer(pausedRemainingMs)
                }
                GameState.MEMORIZE -> {
                    val remainingMs = (_uiState.value.memorizeRemainingSeconds * 1000).toLong()
                    startMemorizePhase(max(200L, remainingMs))
                }
                else -> {
                    startPlayingPhase()
                }
            }
        }
    }

    fun restartGame() {
        stopTimers()
        startGame()
    }

    fun exitToHome() {
        stopTimers()
        _uiState.update {
            it.copy(
                gameState = GameState.READY,
                transitionMessage = null,
                isTimeout = false,
                wrongTileIndex = null
            )
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSoundEnabled(enabled)
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setHapticsEnabled(enabled)
        }
    }

    fun setReducedMotion(enabled: Boolean) {
        viewModelScope.launch {
            repository.setReducedMotion(enabled)
        }
    }

    fun resetStatistics() {
        viewModelScope.launch {
            repository.resetStatistics()
            _statistics.value = GameStatistics()
        }
    }

    private fun stopTimers() {
        timerJob?.cancel()
        timerJob = null
        memorizeJob?.cancel()
        memorizeJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTimers()
    }
}
