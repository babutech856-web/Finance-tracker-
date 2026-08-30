package com.example

import androidx.compose.material3.Surface
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.CurrencyConfig
import com.example.ui.components.CashFlowChart
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CashFlowPoint
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
    fun cash_flow_chart_screenshot() {
        val samplePoints = listOf(
            CashFlowPoint("Mon", "Mon, Oct 12", 5000.0, 1200.0, 1000L),
            CashFlowPoint("Tue", "Tue, Oct 13", 0.0, 450.0, 2000L),
            CashFlowPoint("Wed", "Wed, Oct 14", 115000.0, 4800.0, 3000L),
            CashFlowPoint("Thu", "Thu, Oct 15", 0.0, 2000.0, 4000L),
            CashFlowPoint("Fri", "Fri, Oct 16", 0.0, 5000.0, 5000L)
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                Surface {
                    CashFlowChart(
                        points = samplePoints,
                        currency = CurrencyConfig.NPR
                    )
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/cash_flow_chart.png")
    }
}

