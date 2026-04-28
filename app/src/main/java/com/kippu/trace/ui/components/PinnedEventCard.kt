package com.kippu.trace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kippu.trace.model.DateEvent
import com.kippu.trace.model.DisplayMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
fun PinnedEventCard(
    event: DateEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetLocalDate = Instant.ofEpochMilli(event.targetDate)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val today = LocalDate.now()
    val days = ChronoUnit.DAYS.between(today, targetLocalDate).let { if (it < 0) -it else it }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (event.backgroundUri != null) {
                AsyncImage(
                    model = event.backgroundUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer))
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = event.maskOpacity)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = Color.White,
                            shadow = Shadow(color = Color.Black.copy(alpha = 0.5f), blurRadius = 8f)
                        )
                    )
                    Text(
                        text = targetLocalDate.toString(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Normal
                        )
                    )
                }

                Row(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = days.toString(),
                        style = MaterialTheme.typography.displayMedium.copy(
                            color = Color.White,
                            shadow = Shadow(color = Color.Black.copy(alpha = 0.5f), blurRadius = 12f)
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "天",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }

            // Pinned Badge moved outside to allow finer padding control
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp) // Closer to edge to align with 24dp corners
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "▷ 置顶",
                    style = MaterialTheme.typography.labelMedium.copy(color = Color.White)
                )
            }
        }
    }
}

@Preview
@Composable
fun PinnedEventCardPreview() {
    val mockEvent = DateEvent(
        title = "我的生日",
        targetDate = System.currentTimeMillis() + 86400000 * 23,
        isFuture = true,
        mode = DisplayMode.COUNT_DOWN,
        isPinned = true,
        backgroundUri = "https://images.unsplash.com/photo-1490730141103-6cac27aaab94"
    )
    com.kippu.trace.ui.theme.KIPPU_TraceTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PinnedEventCard(event = mockEvent, onClick = {})
        }
    }
}
