package com.example.weatherapp

import android.content.Intent
import android.os.Bundle
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
    val context = LocalContext.current

    val apiKey = "6af5deed7a5f157df39fc6d53bd67781"

    LaunchedEffect(zipCode) {
        if (zipCode.isNotEmpty()) {
            viewModel.fetchForecast("$zipCode,us", apiKey, context)
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

        if (zipCode.isEmpty()) {
            Text(
                text = "Please enter a ZIP code on the main screen",
                color = Color.Gray,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            forecastData?.let { forecast ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(forecast.list) { item ->
                        ForecastItemCard(item)
                    }
                }
            } ?: run {
                Text(
                    text = "Loading forecast data...",
                    color = Color.Gray,
                    modifier = Modifier.padding(8.dp)
                )
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
            Text(text = "Back to Current Weather")
        }
    }
}

@Composable
fun ForecastItemCard(item: ForecastItem) {
    val date = Date(item.dt * 1000)
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    
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
                    text = item.weather[0].description.capitalize(),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "H: ${String.format("%.0f°F", (item.temp.max * 9 / 5) + 32)}",
                    fontSize = 14.sp,
                    color = Color.Red
                )
                Text(
                    text = "L: ${String.format("%.0f°F", (item.temp.min * 9 / 5) + 32)}",
                    fontSize = 14.sp,
                    color = Color.Blue
                )
                Text(
                    text = "${item.humidity}% • ${String.format("%.0f mph", item.speed * 2.237)}",
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