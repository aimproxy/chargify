package com.aimproxy.chargify.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EvStation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aimproxy.chargify.firestore.RatingsAggregation
import com.aimproxy.chargify.viewmodels.EvStationsViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.math.roundToInt

@Composable
fun EvStationRateDialog(
    evStationsViewModel: EvStationsViewModel = viewModel(),
    openDialog: MutableState<Boolean>
) {
    val context = LocalContext.current
    val currentEvStation = evStationsViewModel.selectedEvStation.observeAsState()
    var sliderPosition by remember { mutableStateOf(0f) }

    val ratingsService = RatingsAggregation(Firebase.firestore)

    AlertDialog(
        onDismissRequest = { openDialog.value = false },
        icon = { Icon(Icons.Outlined.EvStation, contentDescription = null) },
        title = {
            Text(
                text = "Rate ${currentEvStation.value?.addressInfo ?: "this station!"}",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column {
                Text(
                    text = "Power up your ride and spread the joy of electric driving by rating your favorite EV charging stations!",
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = sliderPosition.roundToInt().toString(),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    valueRange = 0f..5f,
                    onValueChangeFinished = {
                        // launch some business logic update with the state you hold
                        // viewModel.updateSelectedSliderValue(sliderPosition)
                    },
                    steps = 4
                )
            }
        },
        confirmButton = {
            currentEvStation.value?.stationId?.let {
                TextButton(
                    onClick = {
                        openDialog.value = false
                        ratingsService.addRating(it, sliderPosition)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    "Thank you for your opinion!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d("Rate", "DocumentSnapshot successfully written!")
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    "I couldn't register your opinion!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.w("Rate", "Error writing document", e)
                            }
                    }
                ) {
                    Text(text = "Give my rate!", fontWeight = FontWeight.SemiBold)
                }
            }
        },
    )
}