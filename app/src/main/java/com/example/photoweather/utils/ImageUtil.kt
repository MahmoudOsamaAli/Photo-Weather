package com.example.photoweather.utils

import android.content.Context
import android.graphics.*
import android.os.Environment
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import androidx.palette.graphics.Palette
import com.example.photoweather.R
import com.example.photoweather.network.models.WeatherConditions
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


object ImageUtil {

    private const val TAG = "ImageUtil"

    fun createImageFile(context: Context): File? {
        val folderName = context.resources.getString(R.string.file_name)
        val storageDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DCIM),
            folderName
        )
        return if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) null
            else createFile(storageDir)
        } else createFile(storageDir)
    }

    private fun createFile(storageDir: File): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(Date())
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".png", /* suffix */
            storageDir /* directory */
        )
    }

    private fun overrideOlfImageFile(bitmap: Bitmap, currentPhotoPath: String) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray: ByteArray = stream.toByteArray()
        File(currentPhotoPath).writeBytes(byteArray)
        Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)
    }

    suspend fun drawTextToBitmap(
        weatherText: WeatherConditions?,
        filePath: String,
        context: Context
    ): Bitmap? {
        return GlobalScope.async(Dispatchers.Default) {
            val textBounds = Rect()
            val startX = 100F
            var startY = 200F
            // path for draw background
            val backgroundPath = Path()
            var bitmap = BitmapFactory.decodeFile(filePath)
            var bitmapConfig = bitmap.config
            // set default bitmap config if none
            if (bitmapConfig == null) {
                bitmapConfig = Bitmap.Config.ARGB_8888
            }
            //convert bitmap to mutable one
            bitmap = bitmap.copy(bitmapConfig, true)
            val canvas = Canvas(bitmap)
            // new Paint
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val backgroundPaint = Paint()

            // determine text and background color based on image brightness
            setTextProperties(bitmap, textPaint, backgroundPaint, context)

            // get text info from weather object
            val weather = weatherText?.condition!![0].condition + ", " +
                    weatherText.tempInfo.currentTemperature +
                    context.resources.getString(R.string.degree_simple)
            val location = weatherText.cityName + ", " + weatherText.country?.countryName

            textPaint.getTextBounds(weather, 0, weather.length, textBounds)
            // background bounds
            val mRectF = RectF(
                0F, 0F, bitmap.width.toFloat(),
                (startY + textBounds.height() + textPaint.textSize + 50)
            )
            // add bounds to draw path
            backgroundPath.addRect(mRectF, Path.Direction.CCW)
            //add background style
            backgroundPaint.style = Paint.Style.FILL_AND_STROKE
            // draw background
            canvas.drawPath(backgroundPath, backgroundPaint)
            // draw first line of text
            canvas.drawText(weather, startX, startY, textPaint)
            startY += textPaint.textSize + 50
            // draw second line of text
            canvas.drawText(location, startX, startY, textPaint)
            // override old file image with new one
            CoroutineScope(Dispatchers.Default).async { overrideOlfImageFile(bitmap, filePath) }
                .await()
            // return bitmap
            bitmap
        }.await()
    }

    private fun setTextProperties(
        bitmap: Bitmap,
        textPaint: Paint,
        backgroundPaint: Paint,
        context: Context
    ) {
        val p = getBitmapPalette(bitmap, textPaint)
        val lightness = ColorsUtils3.isDark(p)
        Log.i(TAG, "setTextColor: $lightness")
        val isDark = if (lightness == ColorsUtils3.LIGHTNESS_UNKNOWN) {
            ColorsUtils3.isDark(bitmap, 0, 0)
        } else {
            lightness == ColorsUtils3.IS_DARK
        }

        if (isDark) { // make back icon dark on light images
            textPaint.color = Color.BLACK
            textPaint.setShadowLayer(1f, 0f, 1f, Color.WHITE)
            backgroundPaint.color = Color.argb(155, 255, 255, 255)
        } else {
            textPaint.color = Color.WHITE
            textPaint.setShadowLayer(1f, 0f, 1f, Color.BLACK)
            backgroundPaint.color = Color.argb(155, 0, 0, 0)
        }
        // set text font
        textPaint.typeface = ResourcesCompat.getFont(context, R.font.sansation_regular)
        // set text size
        textPaint.textSize = (48 * context.resources.displayMetrics.density)
    }

    private fun getBitmapPalette(bitmap: Bitmap, paint: Paint): Palette {
        val p = Palette.from(bitmap)
            .maximumColorCount(3)
            .clearFilters()
            .setRegion(0, 0, bitmap.width, paint.textSize.toInt())
        return p.generate()
    }

}