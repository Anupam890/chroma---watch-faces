package com.chrona.shared

import kotlinx.serialization.Serializable

/** Catalog category shown as a tab/chip in the phone app. */
@Serializable
enum class FaceCategory(val displayName: String, val emoji: String) {
    CLASSIC("Classic", "🕰️"),
    MINIMAL("Minimal", "◻️"),
    SPORT("Sport", "🏃"),
    PHOTO("Photo", "📷"),
    WEATHER("Weather", "🌤️"),
    INFO("Information", "📊");
}

/** Minimum Watch Face Format version a face requires (maps to Wear OS version). */
@Serializable
enum class WffVersion(val version: Int, val minWearOs: String, val minApi: Int) {
    V1(1, "Wear OS 4", 33),
    V2(2, "Wear OS 5", 34),
    V3(3, "Wear OS 5.1", 35),
    V4(4, "Wear OS 6", 36);
}

/** A customization option exposed by a face (mirrors WFF UserConfigurations). */
@Serializable
data class CustomizationFeature(
    val id: String,
    val label: String,
    val type: Type,
    val options: List<String> = emptyList()
) {
    @Serializable
    enum class Type { COLOR_THEME, LIST, BOOLEAN, COMPLICATION, PHOTO, FLAVOR }
}

/** One watch face in the catalog. */
@Serializable
data class WatchFaceItem(
    val id: String,                    // e.g. "classic_aurora"
    val name: String,
    val tagline: String,
    val description: String,
    val category: FaceCategory,
    val wffVersion: WffVersion,
    val previewAsset: String,          // drawable name or URL
    val accentColorHex: String,
    val isPremium: Boolean = false,
    val sizeKb: Int,
    val features: List<CustomizationFeature>,
    val flavors: List<String> = emptyList(),
    val complicationSlots: Int = 0,
    /** Relative path of the pre-built, validated WFF APK inside the phone app assets or remote CDN. */
    val apkAsset: String,
    /** Token produced by the Watch Face Push validator for this exact APK build. */
    val validationToken: String = ""
) {
    /** Package name convention enforced by Watch Face Push. */
    fun pushPackageName(appPackage: String) = "$appPackage.watchfacepush.$id"
}

/** Phone -> watch header sent before the APK bytes are streamed. */
@Serializable
data class TransferManifest(
    val faceId: String,
    val packageName: String,
    val sizeBytes: Long,
    val sha256: String,
    val validationToken: String,
    val setActiveAfterInstall: Boolean = true,
    val replaceSlotId: String? = null
)

@Serializable
data class InstallResult(
    val faceId: String,
    val success: Boolean,
    val slotId: String? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null
)

/** Snapshot of the watch, synced back to the phone via DataClient. */
@Serializable
data class WatchState(
    val nodeId: String,
    val deviceName: String,
    val apiLevel: Int,
    val wearOsLabel: String,
    val supportsWatchFacePush: Boolean,
    val hasPushPermission: Boolean,
    val installedFaces: List<InstalledFace>,
    val activeSlotId: String? = null,
    val remainingSlots: Int = 0,
    val batteryPercent: Int = -1,
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class InstalledFace(
    val slotId: String,
    val faceId: String,
    val packageName: String,
    val versionCode: Long,
    val isActive: Boolean
)

@Serializable
data class UserPrefs(
    val favouriteIds: Set<String> = emptySet(),
    val autoSetActive: Boolean = true,
    val wifiOnlyLargeFaces: Boolean = false,
    val themeMode: String = "system"
)

/** Human-friendly label for an API level. */
fun apiToWearOsLabel(api: Int): String = when {
    api >= 36 -> "Wear OS 6"
    api == 35 -> "Wear OS 5.1"
    api == 34 -> "Wear OS 5"
    api == 33 -> "Wear OS 4"
    api == 30 -> "Wear OS 3"
    else -> "Wear OS (API $api)"
}
