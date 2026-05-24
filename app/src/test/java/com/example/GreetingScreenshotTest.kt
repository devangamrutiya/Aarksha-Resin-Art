package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ActivityScenario
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.PinScreen
import com.example.ui.viewmodel.StudioViewModelFactory
import com.example.ui.theme.AarakshaTheme
import androidx.compose.material3.Text
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testMainActivityLaunch() {
    ActivityScenario.launch(MainActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assert(activity != null)
      }
    }
  }

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent { AarakshaTheme { Text("Aaraksha Resin Art") } }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }

  @Test
  fun pinScreen_renders_successfully() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val factory = StudioViewModelFactory(application)
    val viewModel = factory.create(com.example.ui.viewmodel.StudioViewModel::class.java)

    composeTestRule.setContent {
      AarakshaTheme {
        PinScreen(viewModel)
      }
    }
    // Verify that the logo and UI components render without causing any runtime exceptions
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/pinscreen.png")
  }
}
