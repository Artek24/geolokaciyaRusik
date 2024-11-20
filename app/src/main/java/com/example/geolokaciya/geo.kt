package com.example.geolokaciya

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine

@Composable
fun GeolocationApp() {
    val context = LocalContext.current
    val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    var latitude by remember { mutableStateOf<String?>("Идет получение данных...") }
    var longitude by remember { mutableStateOf<String?>("Идет получение данных...") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                getLastKnownLocation(fusedLocationProviderClient) { lat, lng, error ->
                    latitude = lat?.toString()
                    longitude = lng?.toString()
                    errorMessage = error
                }
            } else {
                errorMessage = "Доступ к геолокации запрещен."
            }
        }
    )

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Широта: ${latitude ?: "Неизвестно"}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Долгота: ${longitude ?: "Неизвестно"}")
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun getLastKnownLocation(
    client: FusedLocationProviderClient,
    onResult: (latitude: Double?, longitude: Double?, error: String?) -> Unit
) {
    client.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                onResult(location.latitude, location.longitude, null)
            } else {
                onResult(null, null, "Не удалось получить текущую локацию.")
            }
        }
        .addOnFailureListener { exception ->
            onResult(null, null, "Ошибка: ${exception.localizedMessage}")
        }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                GeolocationApp()
            }
        }
    }
}
