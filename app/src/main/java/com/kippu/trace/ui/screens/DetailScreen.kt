package com.kippu.trace.ui.screens

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import com.kippu.trace.model.DateEvent
import com.kippu.trace.utils.TimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun DetailScreen(
    events: List<DateEvent>,
    initialEventId: Long,
    onBack: () -> Unit
) {
    val view = LocalView.current
    var showControls by remember { mutableStateOf(false) }

    if (!view.isInEditMode) {
        DisposableEffect(androidx.compose.foundation.isSystemInDarkTheme()) {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            
            // Initial state: Force light icons (appearance light = false) for dark immersive background
            val originalStatusBarLight = insetsController.isAppearanceLightStatusBars
            val originalNavBarLight = insetsController.isAppearanceLightNavigationBars
            
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
            
            onDispose {
                // Restore original state when leaving the screen
                insetsController.isAppearanceLightStatusBars = originalStatusBarLight
                insetsController.isAppearanceLightNavigationBars = originalNavBarLight
            }
        }
        
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }

    if (events.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无数据")
        }
        return
    }

    val initialIndex = events.indexOfFirst { it.id == initialEventId }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { events.size })

    androidx.activity.compose.BackHandler(onBack = onBack)

    Box(modifier = Modifier
        .fillMaxSize()
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            showControls = !showControls
        }
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                key = { events[it].id }
            ) { pageIndex ->
                EventDetailItem(event = events[pageIndex])
            }
        }

        // Dark Overlay when controls are shown
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
        }

        // Top Controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButtonWithPulse(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = { onBack() }
                )
                
                IconButtonWithPulse(
                    icon = Icons.Default.MoreVert,
                    onClick = { /* TODO: Functionality */ }
                )
            }
        }
    }
}

@Composable
fun IconButtonWithPulse(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isClicked by remember { mutableStateOf(false) }
    val pulseScale = remember { Animatable(1f) }
    val pulseAlpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isClicked) {
        if (isClicked) {
            pulseScale.snapTo(1f)
            pulseAlpha.snapTo(0.5f)
            scope.launch {
                pulseScale.animateTo(2f, tween(400, easing = LinearOutSlowInEasing))
            }
            scope.launch {
                pulseAlpha.animateTo(0f, tween(400))
            }
            isClicked = false
        }
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isClicked = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .scale(pulseScale.value)
                .alpha(pulseAlpha.value)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White
        )
    }
}

@Composable
fun EventDetailItem(event: DateEvent) {
    val targetLocalDate = Instant.ofEpochMilli(event.targetDate).atZone(ZoneId.systemDefault()).toLocalDate()
    val days = ChronoUnit.DAYS.between(LocalDate.now(), targetLocalDate).let { if (it < 0) -it else it }

    val animatedDays = remember { Animatable(0f) }
    var detailedTime by remember { mutableStateOf(TimeUtils.getDetailedTime(event.targetDate)) }

    LaunchedEffect(event.id) {
        animatedDays.snapTo(0f)
        animatedDays.animateTo(
            targetValue = days.toFloat(),
            animationSpec = tween(durationMillis = 800)
        )
    }

    // Ticker for H/M/S
    LaunchedEffect(event.id) {
        while (true) {
            detailedTime = TimeUtils.getDetailedTime(event.targetDate)
            delay(1000)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (event.backgroundUri != null) {
            AsyncImage(
                model = event.backgroundUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = event.maskOpacity)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() 
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val prefix = if (event.isFuture) "还有" else "已经"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = event.title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Light,
                        letterSpacing = 4.sp
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = prefix,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Light)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = animatedDays.value.toInt().toString(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 120.sp,
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = String.format(Locale.getDefault(), "%02d:%02d:%02d", detailedTime.hours, detailedTime.minutes, detailedTime.seconds),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White.copy(alpha = 0.8f),
                    fontFamily = com.kippu.trace.ui.theme.NumberFontFamily,
                    letterSpacing = 4.sp
                )
            )
        }
    }
}
