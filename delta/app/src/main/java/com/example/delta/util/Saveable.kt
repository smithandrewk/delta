package com.example.delta.util

import android.os.Bundle
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.autoSaver
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle

// Interop between SavedStateHandle and Compose Saver/SaveableStateRegistry
// Can be removed after https://issuetracker.google.com/195689777
fun <T : Any> SavedStateHandle.saveable(
    key: String,
    saver: Saver<T, out Any> = autoSaver(),
    init: () -> T
): T {
    @Suppress("UNCHECKED_CAST")
    saver as Saver<T, Any>
    // value is restored using the SavedStateHandle or created via [init] lambda
    val value = get<Bundle?>(key)?.get("value")?.let(saver::restore) ?: init()

    // Hook up saving the state to the SavedStateHandle
    setSavedStateProvider(key) {
        bundleOf("value" to with(saver) { SaverScope { true }.save(value) })
    }
    return value
}
