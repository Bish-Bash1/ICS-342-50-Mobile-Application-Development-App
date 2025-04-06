package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
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
    val context = LocalContext.current

    var zipCode by remember {
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
                value = zipCode,
                onValueChange = { newValue ->
                    if (newValue.length <= 5 && newValue.all { it.isDigit() }) {
                        zipCode = newValue
                    }
                },
                label = { Text("ZIP Code") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    unfocusedIndicatorColor = Color.Blue,
                    focusedIndicatorColor = Color.Blue,
                    focusedLabelColor = Color.Cyan
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { 
                    if (zipCode.length == 5) {
                        viewModel.fetchWeather("$zipCode,us", apiKey, context)
                    } else {
                        Toast.makeText(context, "Please enter a valid 5-digit ZIP code", Toast.LENGTH_SHORT).show()
                    }
                },
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
            ScreenButton(zipCode)
        }
    }
}

@Composable
fun ScreenButton(zipCode: String) {
    val context = LocalContext.current
    Spacer(modifier = Modifier.height(250.dp))

    Button(
        onClick = {
            val intent = Intent(context, MainActivity2::class.java)
            intent.putExtra("ZIP_CODE", zipCode)
            context.startActivity(intent)
        },
        colors = ButtonDefaults.buttonColors(Color.Blue)
    ) {
        Text(text = "Forecast")
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    WeatherScreen()
}