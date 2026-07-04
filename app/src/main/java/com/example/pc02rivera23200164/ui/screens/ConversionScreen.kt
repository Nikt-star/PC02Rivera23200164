package com.example.pc02rivera23200164.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    var isConverting by remember { mutableStateOf(false) }
    
    val currencies = listOf("USD", "EUR", "PEN", "GBP", "JPY")
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        val ratesData = mapOf("supported" to currencies)
        db.collection("rates").document("config").set(ratesData)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conversor Rivera", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onGoToHistory) {
                        Icon(Icons.Default.History, contentDescription = "Historial")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar Sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Nueva Conversión",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Monto a convertir") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expandedFrom,
                            onExpandedChange = { expandedFrom = !expandedFrom },
                            modifier = Modifier.weight(1f)
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

                        ExposedDropdownMenuBox(
                            expanded = expandedTo,
                            onExpandedChange = { expandedTo = !expandedTo },
                            modifier = Modifier.weight(1f)
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
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val value = amount.toDoubleOrNull()
                            if (value != null) {
                                isConverting = true
                                scope.launch {
                                    try {
                                        val response = RetrofitClient.apiService.getLatestRates(base = fromCurrency)
                                        if (response.isSuccessful) {
                                            val rates = response.body()?.conversion_rates
                                            val rate = rates?.get(toCurrency)
                                            if (rate != null) {
                                                val result = value * rate
                                                resultText = String.format(Locale.US, "%.2f %s = %.2f %s", value, fromCurrency, result, toCurrency)
                                                
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
                                        resultText = "Error de red"
                                    } finally {
                                        isConverting = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !isConverting
                    ) {
                        if (isConverting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text("CONVERTIR")
                        }
                    }
                }
            }

            if (resultText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        resultText,
                        modifier = Modifier.padding(24.dp).align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
