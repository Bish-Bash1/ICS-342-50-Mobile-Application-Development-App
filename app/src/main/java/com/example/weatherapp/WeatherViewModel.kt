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

    private val weatherApi = WeatherApi.create()

    fun fetchWeather(location: String, apiKey: String, context: Context) {
        viewModelScope.launch {
            try {
                val response = weatherApi.getWeather(location, apiKey)
                _weatherData.value = response
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error fetching weather data: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun fetchForecast(location: String, apiKey: String, context: Context) {
        viewModelScope.launch {
            try {
                val response = weatherApi.getForecast(location, apiKey)
                _forecastData.value = response
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error fetching forecast data: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}