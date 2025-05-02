package com.example.weatherapp

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationViewModel : ViewModel() {
    private val _locationWeather = MutableStateFlow<WeatherResponse?>(null)
    val locationWeather: StateFlow<WeatherResponse?> = _locationWeather

    private val _locationForecast = MutableStateFlow<ForecastResponse?>(null)
    val locationForecast: StateFlow<ForecastResponse?> = _locationForecast

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val weatherApi = WeatherApi.create()
    private val apiKey = "6af5deed7a5f157df39fc6d53bd67781"

    fun getCurrentLocationWeather(context: Context) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val location = getLastLocation(fusedLocationClient)
                
                if (location != null) {
                    saveLocation(context, location)
                    updateWeatherData(location)
                    updateForecastData(location)
                } else {
                    _error.value = "Unable to get current location. Please try again."
                }
            } catch (e: SecurityException) {
                _error.value = "Location permission is required to get weather for your current location."
            } catch (e: Exception) {
                _error.value = "Error getting location: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun saveLocation(context: Context, location: Location) {
        val sharedPreferences = context.getSharedPreferences("WeatherAppPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putFloat("last_latitude", location.latitude.toFloat())
            putFloat("last_longitude", location.longitude.toFloat())
            apply()
        }
    }

    private suspend fun getLastLocation(fusedLocationClient: FusedLocationProviderClient): Location? =
        suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        continuation.resume(location)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            } catch (e: SecurityException) {
                continuation.resumeWithException(e)
            }
        }

    private suspend fun updateWeatherData(location: Location) {
        try {
            val response = weatherApi.getWeather("${location.latitude},${location.longitude}", apiKey)
            _locationWeather.value = response
        } catch (e: Exception) {
            _error.value = "Error fetching weather data: ${e.message}"
        }
    }

    private suspend fun updateForecastData(location: Location) {
        try {
            val response = weatherApi.getForecast("${location.latitude},${location.longitude}", apiKey)
            _locationForecast.value = response
        } catch (e: Exception) {
            _error.value = "Error fetching forecast data: ${e.message}"
        }
    }

    fun clearError() {
        _error.value = null
    }
} 