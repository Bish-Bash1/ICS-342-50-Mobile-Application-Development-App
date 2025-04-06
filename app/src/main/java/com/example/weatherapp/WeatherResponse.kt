package com.example.weatherapp

data class WeatherResponse(
    val name: String,
    val main: Main,
    val weather: List<Weather>
)

data class Main(
    val temp: Double,
    val feelsLike: Double,
    val tempMin: Double,
    val tempMax: Double,
    val humidity: Int,
    val pressure: Int
)

data class Weather(
    val description: String
)

data class ForecastResponse(
    val city: ForecastCity,
    val list: List<ForecastItem>
)

data class ForecastCity(
    val name: String,
    val country: String,
    val coord: Coordinates
)

data class Coordinates(
    val lat: Double,
    val lon: Double
)

data class ForecastItem(
    val dt: Long,
    val temp: Temperature,
    val humidity: Int,
    val weather: List<ForecastWeather>,
    val speed: Double,
    val deg: Int,
    val clouds: Int
)

data class Temperature(
    val day: Double,
    val min: Double,
    val max: Double,
    val night: Double,
    val eve: Double,
    val morn: Double
)

data class ForecastWeather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Clouds(
    val all: Int
)

data class Wind(
    val speed: Double,
    val deg: Int,
    val gust: Double
)

data class ForecastSys(
    val pod: String
)