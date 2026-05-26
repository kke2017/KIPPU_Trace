package com.kippu.trace.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.kippu.trace.R
import com.kippu.trace.model.DateEvent
import kotlinx.coroutines.launch
import com.kippu.trace.ui.components.NormalEventCard
import com.kippu.trace.ui.components.PinnedEventCard
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    events: List<DateEvent>,
    onAddClick: () -> Unit,
    onEventClick: (DateEvent) -> Unit,
    onDeleteEvent: (DateEvent) -> Unit,
    onUpdateOrder: (List<DateEvent>) -> Unit = {},
) {
    val pinnedEventsState = remember(events) { events.filter { it.isPinned }.toMutableStateList() }
    val otherEventsState = remember(events) { events.filter { !it.isPinned }.toMutableStateList() }

    var eventToDelete by remember { mutableStateOf<DateEvent?>(null) }

    val lazyListState = rememberLazyListState()

    if (eventToDelete != null) {
        AlertDialog(
            onDismissRequest = { eventToDelete = null },
            title = { Text(stringResource(R.string.confirm_delete_title)) },
            text = { Text(stringResource(R.string.delete_confirm_message, eventToDelete?.title ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        eventToDelete?.let { onDeleteEvent(it) }
                        eventToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.delete_button)) }
            },
            dismissButton = {
                TextButton(onClick = { eventToDelete = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "TimeTrace", style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.secondary))
                        Text(text = stringResource(R.string.timeline_title), style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
                    }
                },
                actions = {
                    IconButton(onClick = onAddClick) { Icon(Icons.Default.Add, contentDescription = "Add") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        if (events.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.empty_timeline_hint), style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.secondary))
            }
        } else {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(pinnedEventsState, key = { _, it -> "pinned_${it.id}" }) { _, event ->
                    SwipeToDeleteWrapper(isResetRequested = eventToDelete == null, isDeleting = eventToDelete?.id == event.id, onTrashClick = { eventToDelete = event }) {
                        PinnedEventCard(event = event, onClick = { onEventClick(event) })
                    }
                }

                itemsIndexed(otherEventsState, key = { _, it -> it.id }) { _, event ->
                    SwipeToDeleteWrapper(isResetRequested = eventToDelete == null, isDeleting = eventToDelete?.id == event.id, onTrashClick = { eventToDelete = event }) {
                        NormalEventCard(event = event, onClick = { onEventClick(event) })
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeToDeleteWrapper(
    isResetRequested: Boolean,
    isDeleting: Boolean,
    onTrashClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val revealWidth = with(density) { 100.dp.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var dragOffsetX by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isResetRequested) {
        if (isResetRequested && (offsetX.value != 0f)) {
            dragOffsetX = 0f
            offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
        }
    }

    val iconScale by animateFloatAsState(targetValue = if (isDeleting) 1.2f else 1f, animationSpec = spring(stiffness = Spring.StiffnessLow))
    val iconAlpha by animateFloatAsState(targetValue = if (offsetX.value < -20f) (if (isDeleting) 1f else 0.9f) else 0f)

    Box(
        modifier = Modifier.fillMaxWidth().pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragStart = { dragOffsetX = offsetX.value },
                onHorizontalDrag = { change, dragAmount ->
                    change.consume()
                    dragOffsetX += dragAmount
                    val effectiveX = when {
                        dragOffsetX < -revealWidth -> -revealWidth - (abs(dragOffsetX + revealWidth) * 150f / (abs(dragOffsetX + revealWidth) + 150f))
                        dragOffsetX > 0 -> dragOffsetX * 100f / (dragOffsetX + 100f)
                        else -> dragOffsetX
                    }
                    scope.launch { offsetX.snapTo(effectiveX) }
                },
                onDragEnd = {
                    val target = if (offsetX.value < -revealWidth * 0.45f) -revealWidth else 0f
                    dragOffsetX = target
                    scope.launch { offsetX.animateTo(target, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)) }
                }
            )
        }
    ) {
        Box(modifier = Modifier.matchParentSize().padding(end = 24.dp), contentAlignment = Alignment.CenterEnd) {
            IconButton(
                onClick = onTrashClick,
                modifier = Modifier
                    .size(48.dp)
                    .scale(iconScale)
                    .alpha(iconAlpha)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(42.dp)
                )
            }
        }
        Box(modifier = Modifier.fillMaxWidth().offset { IntOffset(offsetX.value.roundToInt(), 0) }) { content() }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F9FA)
@Composable
fun HomeScreenPreview() {
    com.kippu.trace.ui.theme.KIPPU_TraceTheme {
        HomeScreen(events = emptyList(), onAddClick = {}, onEventClick = {}, onDeleteEvent = {})
    }
}
