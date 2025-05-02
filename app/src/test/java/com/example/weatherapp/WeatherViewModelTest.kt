package com.example.weatherapp

import android.content.Context
import android.widget.Toast
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.anyString
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class WeatherViewModelTest {
    private lateinit var viewModel: WeatherViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockWeatherApi: WeatherApi

    private lateinit var mockToast: Toast
    private lateinit var closeable: AutoCloseable

    @Before
    fun setup() {
        closeable = MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        mockToast = Mockito.mock(Toast::class.java)
        whenever(Toast.makeText(any(), anyString(), anyInt())).thenReturn(mockToast)
        
        viewModel = WeatherViewModel()
        viewModel.javaClass.getDeclaredField("weatherApi").apply {
            isAccessible = true
            set(viewModel, mockWeatherApi)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        closeable.close()
    }

    @Test
    fun `fetchWeather should update weatherData when successful`() = runTest {
        val mockResponse = WeatherResponse(
            name = "Test City",
            main = Main(
                temp = 20.0,
                feelsLike = 22.0,
                tempMin = 18.0,
                tempMax = 23.0,
                humidity = 50,
                pressure = 1013
            ),
            weather = listOf(Weather(description = "Sunny"))
        )
        whenever(mockWeatherApi.getWeather("Test City", "test_api_key"))
            .thenReturn(mockResponse)

        viewModel.fetchWeather("Test City", "test_api_key", mockContext)

        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.weatherData.value)
        assertEquals("Test City", viewModel.weatherData.value?.name)
        assertEquals(20.0, viewModel.weatherData.value?.main?.temp)
    }

    @Test
    fun `fetchWeather should update error when API call fails`() = runTest {
        val exception = Exception("Network error")
        whenever(mockWeatherApi.getWeather("Test City", "test_api_key"))
            .thenThrow(exception)

        viewModel.fetchWeather("Test City", "test_api_key", mockContext)

        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.error.value)
        assertEquals("Error fetching weather data: Network error", viewModel.error.value)
    }

    @Test
    fun `fetchForecast should update forecastData when successful`() = runTest {
        val mockResponse = ForecastResponse(
            list = listOf(
                ForecastItem(
                    dt = 1234567890,
                    temp = Temperature(
                        day = 20.0,
                        min = 18.0,
                        max = 23.0,
                        night = 15.0,
                        eve = 19.0,
                        morn = 17.0
                    ),
                    humidity = 50,
                    weather = listOf(
                        ForecastWeather(
                            id = 800,
                            main = "Clear",
                            description = "Sunny",
                            icon = "01d"
                        )
                    ),
                    speed = 5.0,
                    deg = 180,
                    clouds = 0
                )
            ),
            city = ForecastCity(
                name = "Test City",
                country = "TS",
                coord = Coordinates(lat = 0.0, lon = 0.0)
            )
        )
        whenever(mockWeatherApi.getForecast("Test City", "test_api_key"))
            .thenReturn(mockResponse)

        viewModel.fetchForecast("Test City", "test_api_key", mockContext)

        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.forecastData.value)
        assertEquals(1, viewModel.forecastData.value?.list?.size)
        assertEquals(20.0, viewModel.forecastData.value?.list?.first()?.temp?.day)
    }

    @Test
    fun `fetchForecast should update error when API call fails`() = runTest {
        val exception = Exception("Network error")
        whenever(mockWeatherApi.getForecast("Test City", "test_api_key"))
            .thenThrow(exception)

        viewModel.fetchForecast("Test City", "test_api_key", mockContext)

        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.error.value)
        assertEquals("Error fetching forecast data: Network error", viewModel.error.value)
    }

    @Test
    fun `clearError should reset error state`() = runTest {

        viewModel.fetchWeather("Test City", "test_api_key", mockContext)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearError()

        assertNull(viewModel.error.value)
    }
} 