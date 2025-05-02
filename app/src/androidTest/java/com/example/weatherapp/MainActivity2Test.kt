package com.example.weatherapp

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivity2Test {
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity2>()

    @Before
    fun setup() {
        // Launch activity with test ZIP code
        composeTestRule.activity.intent = Intent().apply {
            putExtra("ZIP_CODE", "12345")
        }
        
        // Wait for initial UI to be ready
        composeTestRule.waitForIdle()
    }

    @Test
    fun testForecastListDisplay() {
        // Verify forecast list title is displayed
        composeTestRule.onNodeWithText("16-Day Forecast")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun testForecastItemElements() {
        // Wait for data to load
        composeTestRule.waitForIdle()

        // Verify at least one forecast item exists
        composeTestRule.onAllNodesWithTag("ForecastCard")
            .fetchSemanticsNodes()
            .firstOrNull()?.let {
                composeTestRule.onNode(hasTestTag("ForecastDate"))
                    .assertExists()
                    .assertIsDisplayed()

                composeTestRule.onNode(hasTestTag("ForecastTemperature"))
                    .assertExists()
                    .assertIsDisplayed()

                composeTestRule.onNode(hasTestTag("ForecastDescription"))
                    .assertExists()
                    .assertIsDisplayed()
            }
    }

    @Test
    fun testBackButton() {
        // Verify back button exists
        composeTestRule.onNodeWithText("Back to Current Weather")
            .assertExists()
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testForecastDataLoading() {
        // Verify loading indicator
        composeTestRule.onNode(hasTestTag("LoadingIndicator"))
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun testForecastErrorHandling() {
        // Set up test content
        composeTestRule.setContent {
            val viewModel: WeatherViewModel = viewModel()
            viewModel.javaClass.getDeclaredField("_error").apply {
                isAccessible = true
                (get(viewModel) as MutableStateFlow<String?>).value = "Test error message"
            }
        }
        
        // Wait for UI to update
        composeTestRule.waitForIdle()


        composeTestRule.onNodeWithText("Test error message")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun testEmptyZipCodeHandling() {
        // Launch activity with empty ZIP code
        composeTestRule.activity.intent = Intent().apply {
            putExtra("ZIP_CODE", "")
        }

        // Verify empty
        composeTestRule.onNodeWithText("Please enter a ZIP code on the main screen")
            .assertExists()
            .assertIsDisplayed()
    }
} 