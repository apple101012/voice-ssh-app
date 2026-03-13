package com.apple101012.voicessh

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

data class SavedTerminalSession(
    val name: String,
    val profile: ConnectionProfile,
)

interface SavedSessionRepository {
    val sessions: Flow<List<SavedTerminalSession>>

    suspend fun saveSession(session: SavedTerminalSession)

    suspend fun deleteSession(name: String)
}

private val Context.savedSessionsDataStore by preferencesDataStore(name = "saved_terminal_sessions")

class DataStoreSavedSessionRepository(
    private val context: Context,
) : SavedSessionRepository {
    override val sessions: Flow<List<SavedTerminalSession>> =
        context.savedSessionsDataStore.data.map { preferences ->
            decodeSessions(preferences[SAVED_SESSIONS_KEY])
        }

    override suspend fun saveSession(session: SavedTerminalSession) {
        context.savedSessionsDataStore.edit { preferences ->
            val current = decodeSessions(preferences[SAVED_SESSIONS_KEY])
            val updated = buildList {
                add(session)
                addAll(current.filterNot { it.name.equals(session.name, ignoreCase = true) })
            }
            preferences[SAVED_SESSIONS_KEY] = encodeSessions(updated)
        }
    }

    override suspend fun deleteSession(name: String) {
        context.savedSessionsDataStore.edit { preferences ->
            val current = decodeSessions(preferences[SAVED_SESSIONS_KEY])
            val updated = current.filterNot { it.name.equals(name, ignoreCase = true) }
            preferences[SAVED_SESSIONS_KEY] = encodeSessions(updated)
        }
    }

    private fun encodeSessions(sessions: List<SavedTerminalSession>): String {
        return JSONArray().apply {
            sessions.forEach { session ->
                put(
                    JSONObject().apply {
                        put("name", session.name)
                        put("host", session.profile.host)
                        put("port", session.profile.port)
                        put("username", session.profile.username)
                        put("authMode", session.profile.authMode.name)
                        put("password", session.profile.password)
                        put("privateKey", session.profile.privateKey)
                    },
                )
            }
        }.toString()
    }

    private fun decodeSessions(raw: String?): List<SavedTerminalSession> {
        if (raw.isNullOrBlank()) {
            return emptyList()
        }

        return runCatching {
            val jsonArray = JSONArray(raw)
            buildList {
                for (index in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(index)
                    add(
                        SavedTerminalSession(
                            name = item.optString("name"),
                            profile = ConnectionProfile(
                                host = item.optString("host"),
                                port = item.optString("port", "22"),
                                username = item.optString("username"),
                                authMode = item.optString("authMode")
                                    .let { value -> AuthMode.entries.firstOrNull { it.name == value } }
                                    ?: AuthMode.Password,
                                password = item.optString("password"),
                                privateKey = item.optString("privateKey"),
                            ),
                        ),
                    )
                }
            }.filter { it.name.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    private companion object {
        val SAVED_SESSIONS_KEY = stringPreferencesKey("saved_sessions_json")
    }
}
