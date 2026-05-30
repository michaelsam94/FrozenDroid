package com.michael.frozendroid

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.michael.frozendroid.data.local.AppDatabase
import com.michael.frozendroid.data.repository.AppRepositoryImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FrozenDroid", appName)
  }

  @Test
  fun testDatabaseAndRepoInstantiation() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    val dao = db.appPackageDao()
    assertNotNull(dao)
    
    val repo = AppRepositoryImpl(context, dao)
    assertNotNull(repo)
    db.close()
  }
}
