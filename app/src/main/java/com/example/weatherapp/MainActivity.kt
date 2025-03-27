package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherScreen()
        }
    }
}

@Composable
fun WeatherScreen() {
    val viewModel: WeatherViewModel = viewModel()
    val weatherData by viewModel.weatherData.collectAsState()

    var city by remember {
        mutableStateOf("")
    }

    val apiKey = "6af5deed7a5f157df39fc6d53bd67781"

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(180.dp))
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    unfocusedIndicatorColor = Color.Blue,
                    focusedIndicatorColor = Color.Blue,
                    focusedLabelColor = Color.Cyan
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.featchWeather(city, apiKey) },
                colors = ButtonDefaults.buttonColors(Color.Blue)
            ) {
                Text(text = "Check Weather")
            }
            Spacer(modifier = Modifier.height(16.dp))
            weatherData?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(text = "City: ${it.name}", fontSize = 18.sp, color = Color.Cyan)
                    Text(
                        text = "Temp: ${String.format("%.0f°F", (it.main.temp * 9 / 5) + 32)}",
                        fontSize = 18.sp,
                        color = Color.Blue
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(text = "Humidity: ${it.main.humidity}%", fontSize = 18.sp, color = Color.Cyan)
                    Text(text = "Description: ${it.weather[0].description}", fontSize = 18.sp, color = Color.Blue)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    WeatherScreen()
}