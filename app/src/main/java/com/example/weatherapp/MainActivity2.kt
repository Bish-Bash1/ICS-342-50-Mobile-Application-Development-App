package com.example.weatherapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val zipCode = intent.getStringExtra("ZIP_CODE") ?: ""
        setContent {
            ForecastScreen(zipCode)
        }
    }
}

@Composable
fun ForecastScreen(zipCode: String) {
    val viewModel: WeatherViewModel = viewModel()
    val forecastData by viewModel.forecastData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    val apiKey = "6af5deed7a5f157df39fc6d53bd67781"

    LaunchedEffect(zipCode) {
        if (zipCode.isNotEmpty()) {
            viewModel.fetchForecast("$zipCode,us", apiKey, context)
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "16-Day Forecast",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Blue,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            zipCode.isEmpty() -> {
                Text(
                    text = "Please enter a ZIP code on the main screen",
                    color = Color.Gray,
                    modifier = Modifier.padding(8.dp)
                )
            }
            error != null -> {
                Text(
                    text = error ?: "An unknown error occurred",
                    color = Color.Red,
                    modifier = Modifier.padding(8.dp)
                )
            }
            forecastData == null -> {
                Text(
                    text = "No forecast data available",
                    color = Color.Gray,
                    modifier = Modifier.padding(8.dp)
                )
            }
            forecastData?.list?.isEmpty() == true -> {
                Text(
                    text = "No forecast data available for this location",
                    color = Color.Gray,
                    modifier = Modifier.padding(8.dp)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = forecastData?.list ?: emptyList(),
                        key = { it.dt }
                    ) { item ->
                        DailyForecastCard(
                            DailyForecastSummary(
                                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    .format(Date(item.dt * 1000)),
                                high = item.temp.max,
                                low = item.temp.min,
                                description = item.weather.firstOrNull()?.description ?: "",
                                humidity = item.humidity
                            )
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(Color.Blue),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Back to Current Weather")
        }
    }
}

data class DailyForecastSummary(
    val date: String,
    val high: Double,
    val low: Double,
    val description: String,
    val humidity: Int
)

@Composable
fun DailyForecastCard(item: DailyForecastSummary) {
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(item.date)
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    val highTempF = (item.high * 9/5) + 32
    val lowTempF = (item.low * 9/5) + 32
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = dateFormat.format(date),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Blue
                )
                Text(
                    text = item.description.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "H: ${String.format(Locale.US, "%.0f°F", highTempF)}",
                    fontSize = 14.sp,
                    color = Color.Red
                )
                Text(
                    text = "L: ${String.format(Locale.US, "%.0f°F", lowTempF)}",
                    fontSize = 14.sp,
                    color = Color.Blue
                )
                Text(
                    text = "${item.humidity}%",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForecastScreenPreview() {
    ForecastScreen("")
}