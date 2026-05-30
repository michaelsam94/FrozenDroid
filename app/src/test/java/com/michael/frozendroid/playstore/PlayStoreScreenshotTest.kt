package com.michael.frozendroid.playstore

import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val PHONE = "w360dp-h640dp-xxhdpi"
private const val TABLET = "w800dp-h1280dp-xhdpi"

@RunWith(RobolectricTestRunner::class)
@Category(PlayStoreScreenshotTests::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class PlayStoreScreenshotTest {
  @Test
  @Config(qualifiers = PHONE)
  fun phone_01_dashboard() {
    capturePlayStoreImage("phone/01_dashboard.png") {
      PlayStoreScreenshotFrame(PlayStoreScene.Dashboard)
    }
  }

  @Test
  @Config(qualifiers = PHONE)
  fun phone_02_directory() {
    capturePlayStoreImage("phone/02_directory.png") {
      PlayStoreScreenshotFrame(PlayStoreScene.Directory)
    }
  }

  @Test
  @Config(qualifiers = PHONE)
  fun phone_03_profiles() {
    capturePlayStoreImage("phone/03_profiles.png") {
      PlayStoreScreenshotFrame(PlayStoreScene.Profiles)
    }
  }

  @Test
  @Config(qualifiers = PHONE)
  fun phone_04_cpu_telemetry() {
    capturePlayStoreImage("phone/04_cpu_telemetry.png") {
      PlayStoreScreenshotFrame(PlayStoreScene.Telemetry)
    }
  }

  @Test
  @Config(qualifiers = TABLET)
  fun tablet_01_dashboard() {
    capturePlayStoreImage("tablet/01_dashboard.png") {
      PlayStoreScreenshotFrame(PlayStoreScene.Dashboard)
    }
  }

  @Test
  @Config(qualifiers = TABLET)
  fun tablet_02_directory() {
    capturePlayStoreImage("tablet/02_directory.png") {
      PlayStoreScreenshotFrame(PlayStoreScene.Directory)
    }
  }
}
