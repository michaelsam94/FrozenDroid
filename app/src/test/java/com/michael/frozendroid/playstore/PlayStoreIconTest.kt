package com.michael.frozendroid.playstore

import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.test.core.app.ApplicationProvider
import com.michael.frozendroid.R
import java.io.File
import java.io.FileOutputStream
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Category(PlayStoreScreenshotTests::class)
@Config(sdk = [35], qualifiers = "w512dp-h512dp-mdpi")
class PlayStoreIconTest {
  @Test
  fun app_icon_512() {
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    val icon = context.getDrawable(R.mipmap.ic_launcher) ?: error("Launcher icon not found")
    val bitmap = icon.toBitmap(width = 512, height = 512)
    val output = File("../play-store/app-icon-512.png")
    output.parentFile?.mkdirs()
    FileOutputStream(output).use { stream ->
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    }
  }
}
