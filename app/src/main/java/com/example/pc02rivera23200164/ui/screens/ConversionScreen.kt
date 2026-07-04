package com.example.pc02rivera23200164.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pc02rivera23200164.model.ConversionRecord
import com.example.pc02rivera23200164.network.RetrofitClient
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionScreen(onLogout: () -> Unit, onGoToHistory: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var fromCurrency by remember { mutableStateOf("USD") }
    var toCurrency by remember { mutableStateOf("PEN") }
    var resultText by remember { mutableStateOf("") }
    var expandedFrom by remember { mutableStateOf(false) }
    var expandedTo by remember { mutableStateOf(false) }
    
    val currencies = listOf("USD", "EUR", "PEN", "GBP", "JPY")
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Cumplir con el requisito de dos colecciones: Inicializar 'rates'
    LaunchedEffect(Unit) {
        val ratesData = mapOf("supported" to currencies)
        db.collection("rates").document("config").set(ratesData)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onLogout) { Text("Cerrar Sesión") }
            Button(onClick = onGoToHistory) { Text("Ver Historial") }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Conversor de Moneda", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Monto") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // From Currency
        ExposedDropdownMenuBox(
            expanded = expandedFrom,
            onExpandedChange = { expandedFrom = !expandedFrom }
        ) {
            OutlinedTextField(
                value = fromCurrency,
                onValueChange = {},
                readOnly = true,
                label = { Text("De") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrom) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedFrom,
                onDismissRequest = { expandedFrom = false }
            ) {
                currencies.forEach { currency ->
                    DropdownMenuItem(
                        text = { Text(currency) },
                        onClick = {
                            fromCurrency = currency
                            expandedFrom = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // To Currency
        ExposedDropdownMenuBox(
            expanded = expandedTo,
            onExpandedChange = { expandedTo = !expandedTo }
        ) {
            OutlinedTextField(
                value = toCurrency,
                onValueChange = {},
                readOnly = true,
                label = { Text("A") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTo) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedTo,
                onDismissRequest = { expandedTo = false }
            ) {
                currencies.forEach { currency ->
                    DropdownMenuItem(
                        text = { Text(currency) },
                        onClick = {
                            toCurrency = currency
                            expandedTo = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val value = amount.toDoubleOrNull()
                if (value != null) {
                    scope.launch {
                        try {
                            val response = RetrofitClient.apiService.getLatestRates(base = fromCurrency)
                            if (response.isSuccessful) {
                                val rates = response.body()?.conversion_rates
                                val rate = rates?.get(toCurrency)
                                if (rate != null) {
                                    val result = value * rate
                                    resultText = String.format(Locale.US, "%.2f %s equivalen a %.2f %s", value, fromCurrency, result, toCurrency)
                                    
                                    // Guardar en Firestore
                                    val record = ConversionRecord(
                                        userId = auth.currentUser?.uid ?: "anon",
                                        timestamp = Timestamp.now(),
                                        amount = value,
                                        fromCurrency = fromCurrency,
                                        toCurrency = toCurrency,
                                        result = result
                                    )
                                    db.collection("conversions").add(record)
                                }
                            }
                        } catch (e: Exception) {
                            resultText = "Error: ${e.message}"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Convertir")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(resultText, style = MaterialTheme.typography.bodyLarge)
    }
}
