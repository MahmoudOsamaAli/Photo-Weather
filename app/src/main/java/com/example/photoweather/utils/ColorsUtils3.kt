package com.example.photoweather.utils

import android.graphics.*
import androidx.annotation.*
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette

object ColorsUtils3 {

    const val IS_LIGHT = 0
    const val IS_DARK = 1
    const val LIGHTNESS_UNKNOWN = 2

    /**
     * Checks if the most populous color in the given palette is dark
     *
     *
     * Annoyingly we have to return this Lightness 'enum' rather than a boolean as palette isn't
     * guaranteed to find the most populous color.
     */
    @Lightness
    fun isDark(palette: Palette?): Int {
        val mostPopulous = getMostPopulousSwatch(palette) ?: return LIGHTNESS_UNKNOWN
        return if (isDark(mostPopulous.rgb)) IS_DARK else IS_LIGHT
    }

    private fun getMostPopulousSwatch(palette: Palette?): Palette.Swatch? {
        var mostPopulous: Palette.Swatch? = null
        if (palette != null) {
            for (swatch in palette.swatches) {
                if (mostPopulous == null || swatch.population > mostPopulous.population) {
                    mostPopulous = swatch
                }
            }
        }
        return mostPopulous
    }

    /**
     * Determines if a given bitmap is dark. This extracts a palette inline so should not be called
     * with a large image!! If palette fails then check the color of the specified pixel
     */
    fun isDark(bitmap: Bitmap, backupPixelX: Int, backupPixelY: Int): Boolean {
        // first try palette with a small color quant size
        val palette:Palette = Palette.from(bitmap).maximumColorCount(3).generate()
        return if (palette != null && palette.swatches.size > 0) {
            isDark(palette) == IS_DARK
        } else {
            // if palette failed, then check the color of the specified pixel
            isDark(bitmap.getPixel(backupPixelX, backupPixelY))
        }
    }

    /**
     * Check if a color is dark (convert to XYZ & check Y component)
     */
    private fun isDark(@ColorInt color: Int): Boolean {
        return ColorUtils.calculateLuminance(color) < 0.5
    }

    @IntDef(IS_LIGHT, IS_DARK, LIGHTNESS_UNKNOWN)
    annotation class Lightness
}
