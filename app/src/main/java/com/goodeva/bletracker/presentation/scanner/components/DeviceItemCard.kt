package com.goodeva.bletracker.presentation.scanner.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goodeva.bletracker.domain.model.BleDevice
import com.goodeva.bletracker.presentation.theme.CardBackground
import com.goodeva.bletracker.presentation.theme.PrimaryNeon
import com.goodeva.bletracker.presentation.theme.TextMuted
import com.goodeva.bletracker.presentation.theme.TextPrimary
import com.goodeva.bletracker.presentation.theme.TextSecondary

@Composable
fun DeviceItemCard(
    device: BleDevice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val zone = device.proximityZone
    val zoneColor = zone.color

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        CardBackground,
                        Color(0xFF16223B)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        zoneColor.copy(alpha = 0.45f),
                        Color(0xFF22304D)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Icon Container dengan efek glowing badge
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(zoneColor.copy(alpha = 0.14f))
                    .border(1.5.dp, zoneColor.copy(alpha = 0.55f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = zoneColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // 2. Info Perangkat (Nama, MAC, Status Sinyal)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = device.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = device.macAddress,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Badge status sinyal pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Mini signal bar indicator
                    SignalStrengthBars(rssi = device.rssi, activeColor = zoneColor)

                    Text(
                        text = "${zone.categoryName} (${device.rssi} dBm)",
                        style = MaterialTheme.typography.labelSmall,
                        color = zoneColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // 3. Estimasi Jarak & Arrow
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF090E1A))
                        .border(1.dp, PrimaryNeon.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 11.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = device.formattedDistance,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryNeon,
                        letterSpacing = 0.2.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Radar View",
                    tint = TextMuted,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun SignalStrengthBars(
    rssi: Int,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    // Menghitung bar aktif (1 - 4)
    val activeBars = when {
        rssi >= -50 -> 4
        rssi >= -70 -> 3
        rssi >= -80 -> 2
        else -> 1
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val barHeights = listOf(4.dp, 7.dp, 10.dp, 13.dp)
        barHeights.forEachIndexed { index, height ->
            val isActive = (index + 1) <= activeBars
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (isActive) activeColor else Color(0xFF263352))
            )
        }
    }
}
