package com.chrona.shared

/**
 * Built-in catalog. In production this would be fetched from a backend
 * (see docs/FUTURE_ENHANCEMENTS.md) but shipping a bundled catalog keeps the
 * app fully functional offline and on first launch.
 */
object Catalog {

    private val colorThemes = CustomizationFeature(
        id = "theme", label = "Colour theme", type = CustomizationFeature.Type.COLOR_THEME,
        options = listOf("Aurora", "Ember", "Ocean", "Graphite", "Mint")
    )

    val faces: List<WatchFaceItem> = listOf(
        WatchFaceItem(
            id = "classic_aurora",
            name = "Aurora Classic",
            tagline = "Timeless hands, living colour",
            description = "A refined analog face with sweeping hands, a subtle sunburst dial and five curated colour themes. Two complication slots keep your day in view without clutter.",
            category = FaceCategory.CLASSIC,
            wffVersion = WffVersion.V1,
            previewAsset = "preview_classic_aurora",
            accentColorHex = "#7C5CFF",
            sizeKb = 420,
            features = listOf(
                colorThemes,
                CustomizationFeature("hands", "Hand style", CustomizationFeature.Type.LIST, listOf("Dauphine", "Baton", "Skeleton")),
                CustomizationFeature("seconds", "Seconds hand", CustomizationFeature.Type.BOOLEAN),
                CustomizationFeature("date", "Date window", CustomizationFeature.Type.BOOLEAN),
                CustomizationFeature("comp_left", "Left complication", CustomizationFeature.Type.COMPLICATION),
                CustomizationFeature("comp_right", "Right complication", CustomizationFeature.Type.COMPLICATION)
            ),
            flavors = listOf("Evening", "Daylight", "Monochrome"),
            complicationSlots = 2,
            apkAsset = "faces/classic_aurora.apk"
        ),
        WatchFaceItem(
            id = "mono_digital",
            name = "Mono",
            tagline = "Nothing but the time",
            description = "A bold typographic digital face with auto-sizing numerals, optional date line and battery hairline. Designed for maximum legibility in ambient mode.",
            category = FaceCategory.MINIMAL,
            wffVersion = WffVersion.V3,
            previewAsset = "preview_mono_digital",
            accentColorHex = "#F5F5F5",
            sizeKb = 180,
            features = listOf(
                colorThemes,
                CustomizationFeature("weight", "Type weight", CustomizationFeature.Type.LIST, listOf("Thin", "Regular", "Black")),
                CustomizationFeature("date", "Show date", CustomizationFeature.Type.BOOLEAN),
                CustomizationFeature("battery", "Battery hairline", CustomizationFeature.Type.BOOLEAN),
                CustomizationFeature("comp_bottom", "Bottom complication", CustomizationFeature.Type.COMPLICATION)
            ),
            flavors = listOf("Pure", "Night", "Loud"),
            complicationSlots = 1,
            apkAsset = "faces/mono_digital.apk"
        ),
        WatchFaceItem(
            id = "pulse_sport",
            name = "Pulse",
            tagline = "Your goals, in rings",
            description = "Three concentric goal rings for steps, active minutes and heart rate, plus a weather chip and four complication slots. Rings animate as you progress.",
            category = FaceCategory.SPORT,
            wffVersion = WffVersion.V2,
            previewAsset = "preview_pulse_sport",
            accentColorHex = "#FF5E5B",
            isPremium = true,
            sizeKb = 610,
            features = listOf(
                colorThemes,
                CustomizationFeature("ring_steps", "Steps ring", CustomizationFeature.Type.BOOLEAN),
                CustomizationFeature("ring_hr", "Heart-rate ring", CustomizationFeature.Type.BOOLEAN),
                CustomizationFeature("weather", "Weather chip", CustomizationFeature.Type.BOOLEAN),
                CustomizationFeature("goal", "Step goal", CustomizationFeature.Type.LIST, listOf("6,000", "8,000", "10,000", "12,000")),
                CustomizationFeature("comp_1", "Complication 1", CustomizationFeature.Type.COMPLICATION),
                CustomizationFeature("comp_2", "Complication 2", CustomizationFeature.Type.COMPLICATION),
                CustomizationFeature("comp_3", "Complication 3", CustomizationFeature.Type.COMPLICATION),
                CustomizationFeature("comp_4", "Complication 4", CustomizationFeature.Type.COMPLICATION)
            ),
            flavors = listOf("Run", "Gym", "Recovery"),
            complicationSlots = 4,
            apkAsset = "faces/pulse_sport.apk"
        ),
        WatchFaceItem(
            id = "memories_photo",
            name = "Memories",
            tagline = "Your photos, your time",
            description = "Pick a single photo or a gallery that rotates on tap. Smart text contrast keeps the time readable over any image, with soft ambient transitions.",
            category = FaceCategory.PHOTO,
            wffVersion = WffVersion.V4,
            previewAsset = "preview_memories_photo",
            accentColorHex = "#FFC857",
            sizeKb = 260,
            features = listOf(
                CustomizationFeature("photos", "Photos", CustomizationFeature.Type.PHOTO),
                CustomizationFeature("advance", "Gallery advance", CustomizationFeature.Type.LIST, listOf("On tap", "Hourly", "Daily")),
                CustomizationFeature("clock_pos", "Clock position", CustomizationFeature.Type.LIST, listOf("Top", "Centre", "Bottom")),
                CustomizationFeature("scrim", "Readability scrim", CustomizationFeature.Type.BOOLEAN),
                CustomizationFeature("comp_bottom", "Bottom complication", CustomizationFeature.Type.COMPLICATION)
            ),
            flavors = listOf("Gallery", "Portrait", "Minimal"),
            complicationSlots = 1,
            apkAsset = "faces/memories_photo.apk"
        ),
        WatchFaceItem(
            id = "skyline_weather",
            name = "Skyline",
            tagline = "Today, at a glance",
            description = "Current conditions, a six-hour forecast strip and sunrise/sunset arc wrapped around a clean digital clock. Colours shift with the weather.",
            category = FaceCategory.WEATHER,
            wffVersion = WffVersion.V2,
            previewAsset = "preview_skyline_weather",
            accentColorHex = "#4CC9F0",
            sizeKb = 540,
            features = listOf(
                colorThemes,
                CustomizationFeature("units", "Temperature units", CustomizationFeature.Type.LIST, listOf("°C", "°F")),
                CustomizationFeature("forecast", "Forecast strip", CustomizationFeature.Type.BOOLEAN),
                CustomizationFeature("sun_arc", "Sun arc", CustomizationFeature.Type.BOOLEAN),
                CustomizationFeature("comp_left", "Left complication", CustomizationFeature.Type.COMPLICATION),
                CustomizationFeature("comp_right", "Right complication", CustomizationFeature.Type.COMPLICATION)
            ),
            flavors = listOf("Clear", "Storm", "Dusk"),
            complicationSlots = 2,
            apkAsset = "faces/skyline_weather.apk"
        )
    )

    fun byId(id: String): WatchFaceItem? = faces.firstOrNull { it.id == id }
    fun byCategory(category: FaceCategory?): List<WatchFaceItem> =
        if (category == null) faces else faces.filter { it.category == category }
    fun compatibleWith(apiLevel: Int): List<WatchFaceItem> =
        faces.filter { it.wffVersion.minApi <= apiLevel }
}
