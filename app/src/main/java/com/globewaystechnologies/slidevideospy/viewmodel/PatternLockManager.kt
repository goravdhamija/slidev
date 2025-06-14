package com.globewaystechnologies.slidevideospy.viewmodel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

// DataStore instance (can be defined at the top level of a file)
val Context.patternLockDataStore: DataStore<Preferences> by preferencesDataStore(name = "pattern_lock_settings")

object PatternLockKeys {
    val PATTERN_LOCK_ENABLED = booleanPreferencesKey("pattern_lock_enabled")
    val SAVED_PATTERN_HASH = stringPreferencesKey("saved_pattern_hash")
}

class PatternLockManager(private val context: Context) {

    val isPatternLockEnabledFlow: Flow<Boolean> = context.patternLockDataStore.data
        .map { preferences ->
            preferences[PatternLockKeys.PATTERN_LOCK_ENABLED] ?: false
        }

    val savedPatternHashFlow: Flow<String?> = context.patternLockDataStore.data
        .map { preferences ->
            preferences[PatternLockKeys.SAVED_PATTERN_HASH]
        }

    suspend fun setPatternLockEnabled(isEnabled: Boolean) {
        context.patternLockDataStore.edit { settings ->
            settings[PatternLockKeys.PATTERN_LOCK_ENABLED] = isEnabled
        }
    }

    suspend fun savePattern(pattern: List<Int>) {
        // IMPORTANT: Hash the pattern before saving. NEVER store the raw pattern.
        val patternString = pattern.joinToString("-")
        val hashedPattern = hashString(patternString) // Implement strong hashing
        context.patternLockDataStore.edit { settings ->
            settings[PatternLockKeys.SAVED_PATTERN_HASH] = hashedPattern
            settings[PatternLockKeys.PATTERN_LOCK_ENABLED] =
                true // Enable lock when pattern is saved
        }
    }

    suspend fun clearPattern() {
        context.patternLockDataStore.edit { settings ->
            settings.remove(PatternLockKeys.SAVED_PATTERN_HASH)
            settings[PatternLockKeys.PATTERN_LOCK_ENABLED] = false // Disable lock
        }
    }

    suspend fun verifyPattern(inputPattern: List<Int>): Boolean {
        val patternString = inputPattern.joinToString("-")
        val hashedInputPattern = hashString(patternString)
        val savedHash =
            context.patternLockDataStore.data.map { it[PatternLockKeys.SAVED_PATTERN_HASH] }
                .firstOrNull() // kotlinx.coroutines.flow.firstOrNull
        return savedHash != null && savedHash == hashedInputPattern
    }

    // --- Hashing (Use a strong, salted hashing algorithm for production) ---
    private fun hashString(input: String, algorithm: String = "SHA-256"): String {
        // WARNING: This is a basic hash. For production, use a library like BouncyCastle
        // for PBKDF2WithHmacSHA1 or Argon2, and include a salt.
        return MessageDigest
            .getInstance(algorithm)
            .digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}