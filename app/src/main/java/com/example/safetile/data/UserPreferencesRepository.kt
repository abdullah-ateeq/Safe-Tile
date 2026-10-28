package com.example.safetile.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.safetile.model.Difficulty
import com.example.safetile.model.GameStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "safe_tile_prefs")

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class UserSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val reducedMotion: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")

        // Stats per difficulty
        fun bestScore(d: Difficulty) = intPreferencesKey("best_score_${d.name}")
        fun bestRound(d: Difficulty) = intPreferencesKey("best_round_${d.name}")
        fun bestStreak(d: Difficulty) = intPreferencesKey("best_streak_${d.name}")

        val TOTAL_GAMES = intPreferencesKey("total_games")
        val TOTAL_SAFE_TILES = intPreferencesKey("total_safe_tiles")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        val themeString = prefs[Keys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
        val theme = runCatching { AppThemeMode.valueOf(themeString) }.getOrDefault(AppThemeMode.SYSTEM)
        UserSettings(
            themeMode = theme,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false
        )
    }

    fun getStatsFlow(difficulty: Difficulty): Flow<GameStatistics> = context.dataStore.data.map { prefs ->
        GameStatistics(
            bestScore = prefs[Keys.bestScore(difficulty)] ?: 0,
            bestRound = prefs[Keys.bestRound(difficulty)] ?: 0,
            bestStreak = prefs[Keys.bestStreak(difficulty)] ?: 0,
            totalGamesPlayed = prefs[Keys.TOTAL_GAMES] ?: 0,
            totalSafeTilesTapped = prefs[Keys.TOTAL_SAFE_TILES] ?: 0
        )
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun setReducedMotion(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.REDUCED_MOTION] = enabled
        }
    }

    suspend fun recordGameResult(
        difficulty: Difficulty,
        score: Int,
        round: Int,
        streak: Int,
        safeTilesTapped: Int
    ): Pair<Boolean, Boolean> {
        var isNewBestScore = false
        var isNewBestRound = false

        context.dataStore.edit { prefs ->
            val curBestScore = prefs[Keys.bestScore(difficulty)] ?: 0
            val curBestRound = prefs[Keys.bestRound(difficulty)] ?: 0
            val curBestStreak = prefs[Keys.bestStreak(difficulty)] ?: 0

            if (score > curBestScore) {
                prefs[Keys.bestScore(difficulty)] = score
                isNewBestScore = true
            }
            if (round > curBestRound) {
                prefs[Keys.bestRound(difficulty)] = round
                isNewBestRound = true
            }
            if (streak > curBestStreak) {
                prefs[Keys.bestStreak(difficulty)] = streak
            }

            prefs[Keys.TOTAL_GAMES] = (prefs[Keys.TOTAL_GAMES] ?: 0) + 1
            prefs[Keys.TOTAL_SAFE_TILES] = (prefs[Keys.TOTAL_SAFE_TILES] ?: 0) + safeTilesTapped
        }

        return Pair(isNewBestScore, isNewBestRound)
    }

    suspend fun resetStatistics() {
        context.dataStore.edit { prefs ->
            for (d in Difficulty.entries) {
                prefs[Keys.bestScore(d)] = 0
                prefs[Keys.bestRound(d)] = 0
                prefs[Keys.bestStreak(d)] = 0
            }
            prefs[Keys.TOTAL_GAMES] = 0
            prefs[Keys.TOTAL_SAFE_TILES] = 0
        }
    }
}
