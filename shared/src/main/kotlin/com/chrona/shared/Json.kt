package com.chrona.shared

import kotlinx.serialization.json.Json

/** Shared lenient JSON configuration so phone and watch always agree on wire format. */
val ChronaJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
    prettyPrint = false
}
