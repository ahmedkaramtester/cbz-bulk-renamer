package com.example.cbzbulkrenamer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val PREFERENCES_NAME = "cbz_bulk_renamer_preferences"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

object PreferencesKeys {
    val FOLDER_URI = stringPreferencesKey("folder_uri")
    val UNDO_STATE = stringPreferencesKey("undo_state")
}

class PreferenceRepository(private val context: Context) {

    val folderUri: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FOLDER_URI]
    }

    val undoState: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.UNDO_STATE]
    }

    suspend fun setFolderUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FOLDER_URI] = uri
        }
    }

    suspend fun setUndoState(state: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.UNDO_STATE] = state
        }
    }

    suspend fun clearUndoState() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.UNDO_STATE)
        }
    }
}
