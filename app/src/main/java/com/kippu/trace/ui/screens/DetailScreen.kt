package com.kippu.trace.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kippu.trace.model.DateEvent
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
fun DetailScreen(
    events: List<DateEvent>,
    initialEventId: Long,
    onBack: () -> Unit
) {
    if (events.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无数据")
        }
        return
    }

    // Find the index of the clicked event
    val initialIndex = events.indexOfFirst { it.id == initialEventId }.coerceAtLeast(0)
    
    // Setup Pager State
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { events.size }
    )

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { events[it].id }
        ) { pageIndex ->
            EventDetailItem(event = events[pageIndex])
        }
    }
}

@Composable
fun EventDetailItem(event: DateEvent) {
    val targetLocalDate = Instant.ofEpochMilli(event.targetDate)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val days = ChronoUnit.DAYS.between(LocalDate.now(), targetLocalDate).let { if (it < 0) -it else it }

    // Number Increment Animation
    val animatedDays = remember { Animatable(0f) }
    LaunchedEffect(event.id) {
        animatedDays.snapTo(0f)
        animatedDays.animateTo(
            targetValue = days.toFloat(),
            animationSpec = tween(durationMillis = 800)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        if (event.backgroundUri != null) {
            AsyncImage(
                model = event.backgroundUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        // Dynamic Mask
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = event.maskOpacity)))

        // Content
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val prefix = if (event.isFuture) "还有" else "已经"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White.copy(alpha = 0.9f),
                        letterSpacing = 4.sp,
                        fontWeight = FontWeight.Light
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = prefix,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Light
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = animatedDays.value.toInt().toString(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 120.sp, // Slightly larger since "天" is removed
                        fontWeight = FontWeight.Bold,
                        fontFamily = com.kippu.trace.ui.theme.NumberFontFamily,
                        color = Color.White
                    )
                )
            }
            
            val datePrefix = if (event.isFuture) "距离" else "自从"
            Text(
                text = "$datePrefix $targetLocalDate",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 2.sp
                )
            )
        }
    }
}
