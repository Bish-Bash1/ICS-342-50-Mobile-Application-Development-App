package com.example.weatherapp

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class LocationViewModelTest {
    private lateinit var viewModel: LocationViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockFusedLocationClient: FusedLocationProviderClient

    @Mock
    private lateinit var mockWeatherApi: WeatherApi

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockSharedPreferencesEditor: SharedPreferences.Editor

    @Mock
    private lateinit var mockLocationTask: Task<Location>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)


        whenever(mockContext.getSharedPreferences("WeatherAppPrefs", Context.MODE_PRIVATE))
            .thenReturn(mockSharedPreferences)
        whenever(mockSharedPreferences.edit()).thenReturn(mockSharedPreferencesEditor)
        whenever(mockSharedPreferencesEditor.putFloat(any(), any())).thenReturn(mockSharedPreferencesEditor)
        whenever(mockSharedPreferencesEditor.apply()).then { }

        Mockito.mockStatic(Toast::class.java).use { mockedStatic ->
            val mockToast = Mockito.mock(Toast::class.java)
            mockedStatic.`when`<Toast> { 
                Toast.makeText(any(), any<String>(), any()) 
            }.thenReturn(mockToast)
        }

        Mockito.mockStatic(LocationServices::class.java).use { mockedStatic ->
            mockedStatic.`when`<FusedLocationProviderClient> { 
                LocationServices.getFusedLocationProviderClient(mockContext) 
            }.thenReturn(mockFusedLocationClient)
        }

        viewModel = LocationViewModel().apply {
            javaClass.getDeclaredField("weatherApi").apply {
                isAccessible = true
                set(this@apply, mockWeatherApi)
            }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getCurrentLocationWeather should update locationWeather when successful`() = runTest {
        val mockLocation = Location("test").apply {
            latitude = 37.7749
            longitude = -122.4194
        }
        val mockWeatherResponse = WeatherResponse(
            name = "San Francisco",
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

        whenever(mockFusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null))
            .thenReturn(mockLocationTask)
        whenever(mockLocationTask.isComplete).thenReturn(true)
        whenever(mockLocationTask.isSuccessful).thenReturn(true)
        whenever(mockLocationTask.result).thenReturn(mockLocation)

        whenever(mockWeatherApi.getWeather("${mockLocation.latitude},${mockLocation.longitude}", any()))
            .thenReturn(mockWeatherResponse)

        viewModel.getCurrentLocationWeather(mockContext)

        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.locationWeather.value)
        assertEquals("San Francisco", viewModel.locationWeather.value?.name)
        assertEquals(20.0, viewModel.locationWeather.value?.main?.temp)
    }

    @Test
    fun `getCurrentLocationWeather should update error when location is null`() = runTest {
        whenever(mockFusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null))
            .thenReturn(mockLocationTask)
        whenever(mockLocationTask.isComplete).thenReturn(true)
        whenever(mockLocationTask.isSuccessful).thenReturn(true)
        whenever(mockLocationTask.result).thenReturn(null)

        viewModel.getCurrentLocationWeather(mockContext)

        // Then
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.error.value)
        assertEquals("Unable to get current location. Please try again.", viewModel.error.value)
    }

    @Test
    fun `getCurrentLocationWeather should update error when location permission is denied`() = runTest {
        val securityException = SecurityException("Location permission denied")
        whenever(mockFusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null))
            .thenThrow(securityException)

        viewModel.getCurrentLocationWeather(mockContext)

        // Then
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.error.value)
        assertEquals("Location permission is required to get weather for your current location.", viewModel.error.value)
    }

    @Test
    fun `clearError should reset error state`() = runTest {
        viewModel.getCurrentLocationWeather(mockContext)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearError()

        assertNull(viewModel.error.value)
    }
} 