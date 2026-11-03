package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusDegraded
import com.example.ui.theme.StatusDegradedBg
import com.example.ui.theme.StatusDown
import com.example.ui.theme.StatusDownBg
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusPendingBg
import com.example.ui.theme.StatusUp
import com.example.ui.theme.StatusUpBg

@Composable
fun StatusBadge(
    status: String?,
    modifier: Modifier = Modifier,
    isChecking: Boolean = false,
    showLabel: Boolean = true
) {
    val visual = when {
        isChecking -> StatusVisual(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            "Checking...",
            Icons.Default.HourglassEmpty
        )
        status == "UP" -> StatusVisual(StatusUp, StatusUpBg, "UP", Icons.Default.CheckCircle)
        status == "DEGRADED" -> StatusVisual(StatusDegraded, StatusDegradedBg, "DEGRADED", Icons.Default.Warning)
        status == "DOWN" -> StatusVisual(StatusDown, StatusDownBg, "DOWN", Icons.Default.Error)
        else -> StatusVisual(StatusPending, StatusPendingBg, "PENDING", Icons.Default.HourglassEmpty)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = if (isChecking || status == "UP" || status == "DOWN") 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(visual.bgColor)
            .border(1.dp, visual.color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(visual.color.copy(alpha = 0.4f))
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(visual.color)
            )
        }

        if (showLabel) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = visual.text,
                color = visual.color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun LatencyPill(
    latencyMs: Long?,
    isDown: Boolean,
    modifier: Modifier = Modifier
) {
    if (latencyMs == null || latencyMs == 0L) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = if (isDown) "Offline" else "-- ms",
                color = if (isDown) StatusDown else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        return
    }

    val (color, bgColor) = when {
        isDown -> Pair(StatusDown, StatusDownBg)
        latencyMs < 250 -> Pair(StatusUp, StatusUpBg)
        latencyMs < 800 -> Pair(StatusDegraded, StatusDegradedBg)
        else -> Pair(StatusDown, StatusDownBg)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = "$latencyMs ms",
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class StatusVisual(
    val color: Color,
    val bgColor: Color,
    val text: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
