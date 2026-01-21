package io.github.yfujita.herenow.util

object AppConstants {
    const val LOCATION_UPDATE_INTERVAL_MS = 60_000L
    const val NETWORK_TIMEOUT_SECONDS = 30L
}

object GravityConstants {
    const val GRS80_BASE_GRAVITY = 9.780327
    const val GRS80_FIRST_CORRECTION = 0.0053024
    const val GRS80_SECOND_CORRECTION = 0.0000058
    const val FREE_AIR_CORRECTION = -3.086e-6
}

object UiConstants {
    const val GLASS_CARD_ALPHA = 0.08f
    const val GLASS_BUTTON_ALPHA = 0.15f
    const val BORDER_ALPHA_HIGH = 0.4f
    const val BORDER_ALPHA_MEDIUM = 0.1f
    const val BORDER_ALPHA_LOW = 0.05f

    const val CORNER_RADIUS_CARD = 24
    const val CORNER_RADIUS_BUTTON = 32

    const val ICON_SIZE_SMALL = 16
    const val ICON_SIZE_MEDIUM = 20

    const val ANIMATION_DURATION_AURORA_1 = 12000
    const val ANIMATION_DURATION_AURORA_2 = 18000
    const val ANIMATION_DURATION_AURORA_3 = 25000
}
