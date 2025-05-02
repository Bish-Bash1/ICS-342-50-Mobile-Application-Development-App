package com.example.weatherapp

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {
    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    val weatherData: StateFlow<WeatherResponse?> = _weatherData

    private val _forecastData = MutableStateFlow<ForecastResponse?>(null)
    val forecastData: StateFlow<ForecastResponse?> = _forecastData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val weatherApi = WeatherApi.create()
    private var lastForecastLocation: String? = null
    private var lastForecastTime: Long = 0
    private val CACHE_DURATION = 15 * 60 * 1000

    fun fetchWeather(location: String, apiKey: String, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = weatherApi.getWeather(location, apiKey)
                _weatherData.value = response
            } catch (e: Exception) {
                _error.value = "Error fetching weather data: ${e.message}"
                Toast.makeText(context, _error.value, Toast.LENGTH_LONG).show()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchForecast(location: String, apiKey: String, context: Context) {
        if (location == lastForecastLocation && 
            System.currentTimeMillis() - lastForecastTime < CACHE_DURATION && 
            _forecastData.value != null) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = weatherApi.getForecast(location, apiKey)
                if (response.list.isEmpty()) {
                    _error.value = "No forecast data available for this location"
                } else {
                    _forecastData.value = response
                    lastForecastLocation = location
                    lastForecastTime = System.currentTimeMillis()
                }
            } catch (e: Exception) {
                _error.value = "Error fetching forecast data: ${e.message}"
                Toast.makeText(context, _error.value, Toast.LENGTH_LONG).show()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}