package com.example.weatherapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        // Wait for initial UI
        composeTestRule.waitForIdle()
    }

    @Test
    fun testSearchButtonClick() {
        // Check if search button is displayed
        composeTestRule.onNodeWithContentDescription("Search Button")
            .assertExists()
            .assertIsEnabled()
            .assertHasClickAction()
    }

    @Test
    fun testLocationButtonClick() {
        // Show if location button is displayed
        composeTestRule.onNodeWithContentDescription("Location Button")
            .assertExists()
            .assertIsEnabled()
            .assertHasClickAction()
    }

    @Test
    fun testSearchInputField() {
        // Show if search input field is displayed
        composeTestRule.onNodeWithTag("SearchInput")
            .assertExists()
            .assertIsEnabled()
    }

    @Test
    fun testWeatherDataDisplay() {
        // Enter a city name and click search
        composeTestRule.onNodeWithTag("SearchInput")
            .performTextInput("55408")
        
        composeTestRule.onNodeWithContentDescription("Search Button")
            .performClick()

        // Wait for the API call
        composeTestRule.waitForIdle()

        // Verify weather data elements
        composeTestRule.onNodeWithTag("TemperatureText")
            .assertExists()
        composeTestRule.onNodeWithTag("WeatherDescription")
            .assertExists()
        composeTestRule.onNodeWithTag("HumidityText")
            .assertExists()
    }

    @Test
    fun testErrorHandling() {
        // Enter an invalid city name
        composeTestRule.onNodeWithTag("SearchInput")
            .performTextInput("00000")
        
        composeTestRule.onNodeWithContentDescription("Search Button")
            .performClick()

        // Wait for the API call
        composeTestRule.waitForIdle()

        // Verify error message is displayed
        composeTestRule.onNodeWithTag("ErrorText")
            .assertExists()
    }

    @Test
    fun testForecastButton() {
        // Verify forecast button is displayed
        composeTestRule.onNodeWithContentDescription("Forecast Button")
            .assertExists()
            .assertIsEnabled()
            .assertHasClickAction()
    }
} 