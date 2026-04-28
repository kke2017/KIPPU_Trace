package com.kippu.trace.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kippu.trace.model.DateEvent
import com.kippu.trace.model.DisplayMode
import com.kippu.trace.utils.TimeUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
fun NormalEventCard(
    event: DateEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetLocalDate = Instant.ofEpochMilli(event.targetDate)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val today = LocalDate.now()
    val daysTotal = ChronoUnit.DAYS.between(today, targetLocalDate).let { if (it < 0) -it else it }
    
    val relativeTime = TimeUtils.getRelativeTime(event.targetDate)
    val timeDescription = TimeUtils.formatRelativeTime(relativeTime)
    
    // Semantic prefix
    val prefix = if (event.isFuture) "还有" else "已经"

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "$prefix $timeDescription",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = daysTotal.toString(),
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 36.sp
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "天",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}
