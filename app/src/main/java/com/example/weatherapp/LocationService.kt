package com.example.weatherapp

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener

data class LocationData(
    val latitude: Double,
    val longitude: Double
)

class LocationService : Service() {
    private val notificationId = 1
    private val channelId = "weather_notification_channel"
    private val tag = "LocationService"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Weather Updates"
            val descriptionText = "Weather updates based on your location"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                enableLights(false)
                enableVibration(false)
                setShowBadge(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(notificationId, notification)
        getCurrentLocation()
        return START_NOT_STICKY
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                object : CancellationToken() {
                    override fun onCanceledRequested(listener: OnTokenCanceledListener) = CancellationTokenSource().token
                    override fun isCancellationRequested() = false
                }
            ).addOnSuccessListener { location ->
                location?.let {
                    LocationData(
                        latitude = it.latitude,
                        longitude = it.longitude
                    )

                    stopSelf()
                } ?: run {
                    Log.e(tag, "Location is null")
                    stopSelf()
                }
            }.addOnFailureListener { e ->
                Log.e(tag, "Error getting location: ${e.message}")
                stopSelf()
            }
        } catch (e: SecurityException) {
            Log.e(tag, "SecurityException: ${e.message}")
            stopSelf()
        } catch (e: Exception) {
            Log.e(tag, "Error requesting location: ${e.message}")
            stopSelf()
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Getting current location...")
            .setSmallIcon(R.drawable.ic_weather_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

}