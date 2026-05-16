package com.whitestone.app.data

enum class StoneRoot(val rawValue: String, val displayName: String) {
    SENSUAL("sensual", "sensual desire"),
    ILL_WILL("illWill", "ill will"),
    HARMING("harming", "harming"),
    RENUNCIATION("renunciation", "renunciation"),
    KINDNESS("kindness", "kindness"),
    HARMLESSNESS("harmlessness", "harmlessness");

    companion object {
        fun allowedFor(type: StoneType): List<StoneRoot> = when (type) {
            StoneType.WHITE -> listOf(RENUNCIATION, KINDNESS, HARMLESSNESS)
            StoneType.BLACK -> listOf(SENSUAL, ILL_WILL, HARMING)
        }

        fun fromRawValue(value: String): StoneRoot? = entries.firstOrNull { it.rawValue == value }
    }
}

enum class StoneIntensity(val rawValue: String, val displayName: String) {
    STRONG("strong", "strong"),
    WEAK("weak", "weak");

    companion object {
        fun fromRawValue(value: String?): StoneIntensity? = entries.firstOrNull { it.rawValue == value }
    }
}

val Stone.roots: List<StoneRoot>
    get() = rootTagsCsv
        ?.split(",")
        ?.mapNotNull { StoneRoot.fromRawValue(it.trim()) }
        .orEmpty()

val Stone.customRootDescriptors: List<String>
    get() = rootDescriptor
        ?.split("\n")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        .orEmpty()

val Stone.rootDisplayNames: List<String>
    get() = roots.map { it.displayName } + customRootDescriptors

val Stone.stoneIntensity: StoneIntensity?
    get() = StoneIntensity.fromRawValue(intensity)

val Stone.tagSummaryText: String?
    get() {
        val labels = rootDisplayNames + listOfNotNull(stoneIntensity?.displayName)
        return labels.takeIf { it.isNotEmpty() }?.joinToString(" · ")
    }

fun Collection<StoneRoot>.toRootTagsCsv(): String? =
    takeIf { it.isNotEmpty() }?.joinToString(",") { it.rawValue }

fun Collection<String>.toRootDescriptorString(): String? =
    map { it.trim() }
        .filter { it.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.joinToString("\n")
