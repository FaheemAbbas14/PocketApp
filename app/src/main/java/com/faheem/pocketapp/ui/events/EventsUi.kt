package com.faheem.pocketapp.ui.events

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.faheem.pocketapp.EventItem
import com.faheem.pocketapp.MainViewModel
import com.faheem.pocketapp.ui.common.formatDate
import com.faheem.pocketapp.ui.common.formatDateTime
import com.faheem.pocketapp.ui.common.mergeDateAndTimeMillis
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.gms.common.api.ApiException
import androidx.compose.ui.res.stringResource
import com.faheem.pocketapp.R
import com.faheem.pocketapp.ui.components.DateRange
import com.faheem.pocketapp.ui.components.DateRangeFilterButton
import com.faheem.pocketapp.ui.components.FilterPeriod
import com.faheem.pocketapp.ui.components.filterByDateRange

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EventsScreen(
    events: List<EventItem>,
    onEdit: (EventItem) -> Unit = {},
    onDelete: (EventItem) -> Unit = {}
) {
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf(FilterPeriod.ALL) }
    var activeDateRange by remember { mutableStateOf<DateRange?>(null) }

    val refreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            isRefreshing = false
        }
    )

    val filteredEvents = remember(events, activeDateRange) {
        filterByDateRange(events, activeDateRange) { it.eventDateMillis }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(refreshState)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                DateRangeFilterButton(
                    selectedPeriod = selectedPeriod,
                    customDateRange = if (selectedPeriod == FilterPeriod.CUSTOM) activeDateRange else null,
                    onFilterSelected = { period, range ->
                        selectedPeriod = period
                        activeDateRange = range
                    }
                )
            }

            if (filteredEvents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (selectedPeriod == FilterPeriod.ALL) {
                            stringResource(R.string.no_events_yet)
                        } else {
                            "No events in ${selectedPeriod.displayName.lowercase()}"
                        },
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                    items(filteredEvents) { event ->
                        EventCard(event = event, onEdit = onEdit, onDelete = onDelete)
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = refreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun EventCard(
    event: EventItem,
    onEdit: (EventItem) -> Unit = {},
    onDelete: (EventItem) -> Unit = {}
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit(event) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(event.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.cd_delete), tint = MaterialTheme.colorScheme.error)
                }
            }
            if (event.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(event.description, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.scheduled_value, formatDateTime(event.eventDateMillis)),
                style = MaterialTheme.typography.labelMedium
            )
            if (event.locationName.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${stringResource(R.string.location)}: ${event.locationName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_event_title)) },
            text = { Text(stringResource(R.string.delete_event_message)) },
            confirmButton = {
                Button(onClick = {
                    onDelete(event)
                    showDeleteConfirm = false
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis() + 86400000) }
    var selectedHour by remember { mutableStateOf(10) }
    var selectedMinute by remember { mutableStateOf(0) }
    var alarmEnabled by remember { mutableStateOf(true) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(selectedHour, selectedMinute, false)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.select_time)) },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showMapPicker) {
        EventLocationPickerDialog(
            initialLatitude = latitude,
            initialLongitude = longitude,
            initialLocationName = locationName,
            onDismiss = { showMapPicker = false },
            onLocationPicked = { name, lat, lng ->
                locationName = name
                latitude = lat
                longitude = lng
                showMapPicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_event)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.event_title)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(stringResource(R.string.description)) }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                Button(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors()) {
                    Text(stringResource(R.string.date_value, formatDate(selectedDate)))
                }
                Button(onClick = { showTimePicker = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors()) {
                    Text(stringResource(R.string.time_value, String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute)))
                }
                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = { Text(stringResource(R.string.location)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.location_placeholder)) }
                )
                Button(onClick = { showMapPicker = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors()) {
                    Text(
                        if (latitude != null && longitude != null) {
                            stringResource(R.string.map_change_location)
                        } else {
                            stringResource(R.string.map_select_location)
                        }
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.set_reminder))
                    Checkbox(checked = alarmEnabled, onCheckedChange = { alarmEnabled = it })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) {
                    val eventDateMillis = mergeDateAndTimeMillis(selectedDate, selectedHour, selectedMinute)
                    viewModel.addEvent(
                        title = title,
                        description = description,
                        eventDateMillis = eventDateMillis,
                        locationName = locationName,
                        latitude = latitude,
                        longitude = longitude,
                        alarmEnabled = alarmEnabled
                    )
                    onDismiss()
                }
            }) { Text(stringResource(R.string.add)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventDialog(event: EventItem, viewModel: MainViewModel, onDismiss: () -> Unit) {
    val initialCal = remember(event.eventDateMillis) { Calendar.getInstance().apply { timeInMillis = event.eventDateMillis } }
    var title by remember { mutableStateOf(event.title) }
    var description by remember { mutableStateOf(event.description) }
    var locationName by remember { mutableStateOf(event.locationName) }
    var latitude by remember { mutableStateOf(event.latitude) }
    var longitude by remember { mutableStateOf(event.longitude) }
    var selectedDate by remember { mutableStateOf(event.eventDateMillis) }
    var selectedHour by remember { mutableStateOf(initialCal.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(initialCal.get(Calendar.MINUTE)) }
    var alarmEnabled by remember { mutableStateOf(event.alarmEnabled) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(selectedHour, selectedMinute, false)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.select_time)) },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showMapPicker) {
        EventLocationPickerDialog(
            initialLatitude = latitude,
            initialLongitude = longitude,
            initialLocationName = locationName,
            onDismiss = { showMapPicker = false },
            onLocationPicked = { name, lat, lng ->
                locationName = name
                latitude = lat
                longitude = lng
                showMapPicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_event)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.event_title)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(stringResource(R.string.description)) }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                Button(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors()) {
                    Text(stringResource(R.string.date_value, formatDate(selectedDate)))
                }
                Button(onClick = { showTimePicker = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors()) {
                    Text(stringResource(R.string.time_value, String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute)))
                }
                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = { Text(stringResource(R.string.location)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.location_placeholder)) }
                )
                Button(onClick = { showMapPicker = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors()) {
                    Text(
                        if (latitude != null && longitude != null) {
                            stringResource(R.string.map_change_location)
                        } else {
                            stringResource(R.string.map_select_location)
                        }
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.set_reminder))
                    Checkbox(checked = alarmEnabled, onCheckedChange = { alarmEnabled = it })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val eventDateMillis = mergeDateAndTimeMillis(selectedDate, selectedHour, selectedMinute)
                viewModel.updateEvent(
                    event.copy(
                        title = title,
                        description = description,
                        eventDateMillis = eventDateMillis,
                        locationName = locationName,
                        latitude = latitude,
                        longitude = longitude,
                        alarmEnabled = alarmEnabled
                    )
                )
                onDismiss()
            }) { Text(stringResource(R.string.update)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun EventLocationPickerDialog(
    initialLatitude: Double?,
    initialLongitude: Double?,
    initialLocationName: String,
    onDismiss: () -> Unit,
    onLocationPicked: (String, Double, Double) -> Unit
) {
    val context = LocalContext.current
    val fallback = LatLng(24.8607, 67.0011)
    val startLatLng = if (initialLatitude != null && initialLongitude != null) {
        LatLng(initialLatitude, initialLongitude)
    } else {
        fallback
    }

    val initError = remember { ensurePlacesInitialized(context) }
    val placesClient = remember { Places.createClient(context) }
    val autocompleteSession = remember { AutocompleteSessionToken.newInstance() }
    val coroutineScope = rememberCoroutineScope()

    var pickedLocation by remember { mutableStateOf(startLatLng) }
    val markerState = remember { MarkerState(position = startLatLng) }
    LaunchedEffect(pickedLocation) {
        markerState.position = pickedLocation
    }
    var pickedName by remember {
        mutableStateOf(
            initialLocationName.ifBlank {
                "Lat ${"%.5f".format(Locale.US, startLatLng.latitude)}, Lng ${"%.5f".format(Locale.US, startLatLng.longitude)}"
            }
        )
    }
    var searchQuery by remember { mutableStateOf(initialLocationName) }
    var suggestions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(initError) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startLatLng, if (initialLatitude == null) 4f else 14f)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_event_location)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        searchError = null
                        searchJob?.cancel()
                        searchJob = coroutineScope.launch {
                            delay(250)
                            if (query.length < 2) {
                                suggestions = emptyList()
                                isSearching = false
                                return@launch
                            }
                            isSearching = true
                            val request = FindAutocompletePredictionsRequest.builder()
                                .setSessionToken(autocompleteSession)
                                .setQuery(query)
                                .build()
                            placesClient.findAutocompletePredictions(request)
                                .addOnSuccessListener { response ->
                                    suggestions = response.autocompletePredictions.take(5)
                                    if (suggestions.isEmpty()) {
                                        searchError = context.getString(R.string.search_no_suggestions, query)
                                    }
                                    isSearching = false
                                }
                                .addOnFailureListener { err ->
                                    suggestions = emptyList()
                                    searchError = mapPlacesError(context, err)
                                    isSearching = false
                                }
                        }
                    },
                    label = { Text(stringResource(R.string.search_location)) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        }
                    }
                )

                if (searchError != null) {
                    Text(
                        text = searchError.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (suggestions.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        itemsIndexed(suggestions) { index, prediction ->
                            TextButton(
                                onClick = {
                                    val placeId = prediction.placeId
                                    val req = FetchPlaceRequest.builder(
                                        placeId,
                                        listOf(
                                            Place.Field.LOCATION,
                                            Place.Field.DISPLAY_NAME,
                                            Place.Field.FORMATTED_ADDRESS
                                        )
                                    ).build()
                                    placesClient.fetchPlace(req)
                                        .addOnSuccessListener { result ->
                                            val place = result.place
                                            place.location?.let { latLng ->
                                                pickedLocation = latLng
                                                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                                            }
                                            pickedName = place.displayName
                                                ?: place.formattedAddress
                                                ?: prediction.getFullText(null).toString()
                                            searchQuery = pickedName
                                            suggestions = emptyList()
                                        }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = prediction.getFullText(null).toString(),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (index < suggestions.lastIndex) {
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = false),
                        onMapClick = { latLng ->
                            pickedLocation = latLng
                            pickedName = "Lat ${"%.5f".format(Locale.US, latLng.latitude)}, Lng ${"%.5f".format(Locale.US, latLng.longitude)}"
                            searchQuery = pickedName
                            suggestions = emptyList()
                        }
                    ) {
                        Marker(
                            state = markerState,
                            title = stringResource(R.string.selected_location_title)
                        )
                    }
                }

                OutlinedTextField(
                    value = pickedName,
                    onValueChange = { pickedName = it },
                    label = { Text(stringResource(R.string.location_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(R.string.location_picker_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onLocationPicked(
                    pickedName.ifBlank {
                        "Lat ${"%.5f".format(Locale.US, pickedLocation.latitude)}, Lng ${"%.5f".format(Locale.US, pickedLocation.longitude)}"
                    },
                    pickedLocation.latitude,
                    pickedLocation.longitude
                )
            }) {
                Text(stringResource(R.string.use_location))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

private fun mapPlacesError(context: Context, error: Exception): String {
    val statusCode = (error as? ApiException)?.statusCode
    return when (statusCode) {
        9011 -> context.getString(R.string.places_access_denied)
        else -> error.localizedMessage ?: context.getString(R.string.places_fetch_failed)
    }
}

private fun ensurePlacesInitialized(context: Context): String? {
    if (Places.isInitialized()) return null
    val appContext = context.applicationContext
    val metaKey = runCatching {
        val appInfo = appContext.packageManager.getApplicationInfo(
            appContext.packageName,
            PackageManager.GET_META_DATA
        )
        appInfo.metaData?.getString("com.google.android.geo.API_KEY").orEmpty()
    }.getOrDefault("")

    if (metaKey.isBlank()) {
        return context.getString(R.string.maps_key_missing)
    }

    return runCatching {
        Places.initializeWithNewPlacesApiEnabled(appContext, metaKey)
        null
    }.getOrElse { it.localizedMessage ?: context.getString(R.string.places_init_failed) }
}
