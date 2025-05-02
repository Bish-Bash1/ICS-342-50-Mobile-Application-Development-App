package com.example.weatherapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import java.util.*


class MainActivity : ComponentActivity() {
    private lateinit var locationViewModel: LocationViewModel

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                startLocationService()
                locationViewModel.getCurrentLocationWeather(this)
            }
            else -> {
                Toast.makeText(this, "Location permission is required for accurate weather data", Toast.LENGTH_LONG).show()
            }
        }
    }

    private val notificationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startLocationService()
        } else {
            Toast.makeText(this, "Notification permission is required for weather updates", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]

        setContent {
            WeatherAppTheme {
                LaunchedEffect(Unit) {
                    requestLocationPermission()
                }
                WeatherScreen(onLocationClick = { requestLocationPermission() })
            }
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationService()
            locationViewModel.getCurrentLocationWeather(this)
        } else {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    private fun startLocationService() {
        val serviceIntent = Intent(this, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}

@Composable
fun WeatherAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        content = content
    )
}

@Composable
fun WeatherScreen(onLocationClick: () -> Unit) {
    val viewModel: WeatherViewModel = viewModel()
    val locationViewModel: LocationViewModel = viewModel()
    val weatherData by viewModel.weatherData.collectAsState()
    val locationWeather by locationViewModel.locationWeather.collectAsState()
    val locationForecast by locationViewModel.locationForecast.collectAsState()
    val isLoading by locationViewModel.isLoading.collectAsState()
    val error by locationViewModel.error.collectAsState()
    val context = LocalContext.current

    var zipCode by remember { mutableStateOf("") }
    var showForecast by remember { mutableStateOf(false) }

    val apiKey = "6af5deed7a5f157df39fc6d53bd67781"

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            locationViewModel.clearError()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            WeatherInputSection(
                zipCode = zipCode,
                onZipCodeChange = { newValue ->
                    if (newValue.length <= 5 && newValue.all { it.isDigit() }) {
                        zipCode = newValue
                    }
                },
                onLocationClick = {
                    locationViewModel.getCurrentLocationWeather(context)
                    onLocationClick()
                },
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            WeatherCheckButton(
                onCheckWeather = {
                    if (zipCode.length == 5) {
                        viewModel.fetchWeather("$zipCode,us", apiKey, context)
                    } else {
                        Toast.makeText(context, "Please enter a valid 5-digit ZIP code", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    else -> {
                        WeatherContentSection(
                            weatherData = locationWeather ?: weatherData,
                            showForecast = showForecast,
                            locationForecast = locationForecast,
                            zipCode = zipCode,
                            context = context
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherContentSection(
    weatherData: WeatherResponse?,
    showForecast: Boolean,
    locationForecast: ForecastResponse?,
    zipCode: String,
    context: Context
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        weatherData?.let { weather ->
            WeatherDisplay(weather)
        }

        Spacer(modifier = Modifier.height(16.dp))

        ForecastSection(
            showForecast = showForecast,
            locationForecast = locationForecast,
            zipCode = zipCode,
            context = context
        )
    }
}

@Composable
fun WeatherInputSection(
    zipCode: String,
    onZipCodeChange: (String) -> Unit,
    onLocationClick: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = zipCode,
            onValueChange = onZipCodeChange,
            label = { Text("ZIP Code") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(30.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                unfocusedIndicatorColor = Color.Blue,
                focusedIndicatorColor = Color.Blue,
                focusedLabelColor = Color.Cyan
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.Blue
                )
            } else {
                IconButton(
                    onClick = onLocationClick,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Get current location",
                        tint = Color.Blue
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherCheckButton(
    onCheckWeather: () -> Unit
) {
    Button(
        onClick = onCheckWeather,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
    ) {
        Text(text = "Check Weather")
    }
}

@Composable
fun WeatherDisplay(weather: WeatherResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = weather.name,
                fontSize = 24.sp,
                color = Color.Blue,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${String.format(Locale.US, "%.0f°F", (weather.main.temp * 9 / 5) + 32)}",
                fontSize = 36.sp,
                color = Color.Blue
            )
            Text(
                text = weather.weather[0].description.capitalize(),
                fontSize = 18.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Humidity: ${weather.main.humidity}%",
                    fontSize = 16.sp,
                    color = Color.Cyan
                )
                Text(
                    text = "Pressure: ${weather.main.pressure} hPa",
                    fontSize = 16.sp,
                    color = Color.Blue
                )
            }
        }
    }
}

@Composable
fun ForecastSection(
    showForecast: Boolean,
    locationForecast: ForecastResponse?,
    zipCode: String,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                val intent = Intent(context, MainActivity2::class.java)
                intent.putExtra("ZIP_CODE", zipCode)
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("View 16-Day Forecast")
        }

        if (showForecast) {
            when {
                locationForecast == null -> {
                    Text(
                        text = "Loading forecast data...",
                        color = Color.Gray,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                locationForecast.list.isEmpty() -> {
                    Text(
                        text = "No forecast data available",
                        color = Color.Gray,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        items(locationForecast.list) { forecastItem ->
                            ForecastItem(forecastItem)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ForecastItem(forecastItem: ForecastItem) {
    val date = Date(forecastItem.dt * 1000)
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = dateFormat.format(date),
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Text(
                    text = forecastItem.weather.firstOrNull()?.description?.capitalize() ?: "N/A",
                    fontSize = 14.sp,
                    color = Color.Blue
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "H: ${String.format(Locale.US, "%.0f°F", (forecastItem.temp.max * 9/5) + 32)}",
                    fontSize = 14.sp,
                    color = Color.Red
                )
                Text(
                    text = "L: ${String.format(Locale.US, "%.0f°F", (forecastItem.temp.min * 9/5) + 32)}",
                    fontSize = 14.sp,
                    color = Color.Blue
                )
                Text(
                    text = "${forecastItem.humidity}%",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    WeatherAppTheme {
        WeatherScreen(onLocationClick = {})
    }
}

private fun String.capitalize(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
        }
    }
}