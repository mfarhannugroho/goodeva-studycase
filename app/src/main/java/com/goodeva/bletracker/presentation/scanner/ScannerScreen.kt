package com.goodeva.bletracker.presentation.scanner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goodeva.bletracker.presentation.scanner.components.DeviceItemCard
import com.goodeva.bletracker.presentation.theme.AccentPurple
import com.goodeva.bletracker.presentation.theme.AccentRed
import com.goodeva.bletracker.presentation.theme.BackgroundDark
import com.goodeva.bletracker.presentation.theme.CardBackground
import com.goodeva.bletracker.presentation.theme.PrimaryNeon
import com.goodeva.bletracker.presentation.theme.SecondaryNeon
import com.goodeva.bletracker.presentation.theme.TextMuted
import com.goodeva.bletracker.presentation.theme.TextPrimary
import com.goodeva.bletracker.presentation.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onNavigateToRadar: (String) -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "BLE Tracker",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        // Pill status hardware / simulasi
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (state.isSimulationMode) AccentPurple.copy(alpha = 0.2f)
                                    else SecondaryNeon.copy(alpha = 0.16f)
                                )
                                .border(
                                    1.dp,
                                    if (state.isSimulationMode) AccentPurple.copy(alpha = 0.6f)
                                    else SecondaryNeon.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (state.isSimulationMode) "SIMULASI" else "HARDWARE",
                                color = if (state.isSimulationMode) Color(0xFFC4B5FD) else SecondaryNeon,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Riwayat",
                            tint = PrimaryNeon,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark
                )
            )
        },
        containerColor = BackgroundDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {

            // 1. Hero Scanner Control Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF131D33),
                                Color(0xFF0F1728)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                if (state.isScanning) SecondaryNeon.copy(alpha = 0.6f) else Color(0xFF263352),
                                Color(0xFF1E293B)
                            )
                        ),
                        RoundedCornerShape(22.dp)
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pulse Indicator Sinyal Aktif
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (state.isScanning) SecondaryNeon.copy(alpha = pulseAlpha)
                                        else TextMuted
                                    )
                                    .border(
                                        2.dp,
                                        if (state.isScanning) SecondaryNeon else Color.Transparent,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (state.isScanning) "Memindai Sinyal BLE..." else "Pemindai Standby",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (state.isScanning) SecondaryNeon else TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Demo Mode Toggle Switch
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Demo",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = state.isSimulationMode,
                                onCheckedChange = { viewModel.toggleSimulationMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PrimaryNeon,
                                    checkedTrackColor = Color(0xFF1E293B)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Button: Start / Stop Scan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (state.isScanning) viewModel.stopScan()
                                else viewModel.startScan()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.isScanning) AccentRed else SecondaryNeon
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Icon(
                                imageVector = if (state.isScanning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF090D16),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.isScanning) "HENTIKAN PEMINDAIAN" else "MULAI PEMINDAIAN",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF090D16),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        // Filter Button
                        IconButton(
                            onClick = { showFilterSheet = !showFilterSheet },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1B243A))
                                .border(1.dp, Color(0xFF2E3E61), RoundedCornerShape(14.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Filter",
                                tint = if (showFilterSheet) PrimaryNeon else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = {
                    Text("Cari nama perangkat atau MAC...", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted)
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryNeon,
                    unfocusedBorderColor = Color(0xFF22304D),
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // 3. Filter Options Panel
            AnimatedVisibility(visible = showFilterSheet) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF151F36))
                        .border(1.dp, Color(0xFF2A3A5E), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ambang Batas Sinyal (RSSI)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (state.rssiThreshold <= -100) "Semua Sinyal" else "≥ ${state.rssiThreshold} dBm",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryNeon,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Slider(
                            value = state.rssiThreshold.toFloat(),
                            onValueChange = { viewModel.onRssiThresholdChanged(it.toInt()) },
                            valueRange = -100f..-50f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = PrimaryNeon,
                                activeTrackColor = PrimaryNeon,
                                inactiveTrackColor = Color(0xFF263352)
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = state.hideUnnamed,
                                onClick = { viewModel.onToggleHideUnnamed(!state.hideUnnamed) },
                                label = { Text("Sembunyikan Tanpa Nama", style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryNeon.copy(alpha = 0.2f),
                                    selectedLabelColor = PrimaryNeon
                                )
                            )

                            FilterChip(
                                selected = state.sortOrder == DeviceSortOrder.STRONGEST_FIRST,
                                onClick = { viewModel.onSortOrderChanged(DeviceSortOrder.STRONGEST_FIRST) },
                                label = { Text("Sinyal Terkuat", style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SecondaryNeon.copy(alpha = 0.2f),
                                    selectedLabelColor = SecondaryNeon
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subheader
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Perangkat Aktif (${state.devices.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                if (state.totalDiscoveredCount > state.devices.size) {
                    Text(
                        text = "Difilter dari ${state.totalDiscoveredCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // List Perangkat
            if (state.devices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (state.isScanning) "Sedang memindai sinyal di sekitar..."
                            else "Tekan 'MULAI PEMINDAIAN' untuk mendeteksi perangkat BLE.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Gunakan tombol toggle 'Demo' jika menjalankan di emulator.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = state.devices,
                        key = { it.macAddress }
                    ) { device ->
                        DeviceItemCard(
                            device = device,
                            onClick = { onNavigateToRadar(device.macAddress) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
