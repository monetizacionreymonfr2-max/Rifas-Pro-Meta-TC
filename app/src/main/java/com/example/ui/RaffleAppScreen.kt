package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Raffle
import com.example.data.TicketSold
import com.example.ui.theme.*
import com.example.ui.viewmodel.RaffleViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CarbonFiberBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBg)
            .drawBehind {
                val width = size.width
                val height = size.height

                // 1. Futuristic Radial Ambient Vignette
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF28154D), // Dark cosmic purple center
                            DeepSpaceBg        // Pitch black edges
                        ),
                        center = Offset(width / 2f, height / 2f),
                        radius = size.maxDimension * 0.7f
                    )
                )

                // 2. High-Tech Carbon Fiber Weave Pattern (optimised geometry)
                val step = 20.dp.toPx()
                val stroke = 1.2f.dp.toPx()

                // Translucent grid threads mimicking woven micro-filaments
                for (x in (-height.toInt()..width.toInt() step step.toInt())) {
                    drawLine(
                        color = Color(0x1F9C27B0), // Soft violet
                        start = Offset(x.toFloat(), 0f),
                        end = Offset(x.toFloat() + height, height),
                        strokeWidth = stroke
                    )
                }

                for (x in (0..(width.toInt() + height.toInt()) step step.toInt())) {
                    drawLine(
                        color = Color(0x1F9C27B0), // Soft violet
                        start = Offset(x.toFloat(), 0f),
                        end = Offset(x.toFloat() - height, height),
                        strokeWidth = stroke
                    )
                }

                // Horizontal laser alignment micro-dots / lines
                val dotsStep = 6.dp.toPx()
                for (y in (0..height.toInt() step dotsStep.toInt())) {
                    val alpha = if (y % (dotsStep * 8).toInt() == 0) 0.08f else 0.03f
                    drawLine(
                        color = Color(0xFFE2D6FF).copy(alpha = alpha),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(width, y.toFloat()),
                        strokeWidth = 1f
                    )
                }
            }
    ) {
        content()
    }
}

// Global text formatter to resemble sleek digital meters
fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CL")) // Spanish Chilean/Colombian style standard
    format.maximumFractionDigits = 0
    return format.format(amount)
}

fun formatTicketNumber(number: Int, totalNumbers: Int, startFromZero: Boolean): String {
    val maxVal = if (startFromZero) totalNumbers - 1 else totalNumbers
    val digits = maxOf(2, maxVal.toString().length)
    return number.toString().padStart(digits, '0')
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RaffleAppScreen(viewModel: RaffleViewModel) {
    val raffles by viewModel.allRaffles.collectAsStateWithLifecycle()
    val activeRaffle by viewModel.activeRaffle.collectAsStateWithLifecycle()
    val activeTickets by viewModel.activeRaffleTickets.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var clientPortalSimActive by remember { mutableStateOf(false) }

    CarbonFiberBackground(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Holographic visual container
        AnimatedContent(
            targetState = when {
                clientPortalSimActive && activeRaffle != null -> "portal"
                activeRaffle != null -> "detail"
                else -> "dashboard"
            },
            transitionSpec = {
                slideInVertically(initialOffsetY = { it }) + fadeIn() with
                        slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            },
            label = "ScreenTransition"
        ) { targetState ->
            when (targetState) {
                "dashboard" -> {
                    RaffleDashboard(
                        raffles = raffles,
                        onRaffleSelected = { viewModel.selectRaffle(it) },
                        onCreateRequested = { showCreateDialog = true }
                    )
                }
                "detail" -> {
                    RaffleDetailView(
                        raffle = activeRaffle!!,
                        soldTickets = activeTickets,
                        onBack = { viewModel.selectRaffle(null) },
                        onSellTicket = { number, name, phone, paid, notes ->
                            viewModel.sellTicket(
                                number = number,
                                clientName = name,
                                clientPhone = phone,
                                status = if (paid) "PAID" else "PENDING",
                                notes = notes
                            )
                        },
                        onReleaseTicket = { number -> viewModel.releaseTicketByNumber(number) },
                        onToggleStatus = { ticket -> viewModel.toggleTicketPaymentStatus(ticket) },
                        onDeleteRaffle = { viewModel.deleteActiveRaffle() },
                        onOpenPortalSimulator = { clientPortalSimActive = true }
                    )
                }
                "portal" -> {
                    ClientPortalSimulator(
                        raffle = activeRaffle!!,
                        soldTickets = activeTickets,
                        onBack = { clientPortalSimActive = false },
                        onClaimTicket = { number, name, phone ->
                            viewModel.sellTicket(
                                number = number,
                                clientName = name,
                                clientPhone = phone,
                                status = "PENDING",
                                notes = "Reservado por cliente vía simulador quantum"
                            )
                        }
                    )
                }
            }
        }

        // Futuristic neon modal to create new raffles
        if (showCreateDialog) {
            CreateRaffleDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { title, totalNumbers, desc, price, prize, startZero ->
                    viewModel.createRaffle(
                        title = title,
                        totalNumbers = totalNumbers,
                        description = desc,
                        ticketPrice = price,
                        prize = prize,
                        startFromZero = startZero
                    )
                    showCreateDialog = false
                }
            )
        }
    }
}

// ==========================================
// 1. DASHBOARD VIEW (List of Raffles)
// ==========================================
@Composable
fun RaffleDashboard(
    raffles: List<Raffle>,
    onRaffleSelected: (Int) -> Unit,
    onCreateRequested: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateRequested,
                containerColor = CyberCyan,
                contentColor = DeepSpaceBg,
                shape = CircleShape,
                modifier = Modifier
                    .testTag("create_raffle_fab")
                    .border(2.dp, Color.White, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear Rifa",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Quantum Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "QUANTUM",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        fontSize = 11.sp,
                        letterSpacing = 4.sp
                    )
                    Text(
                        text = "Rifas Conectadas",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 28.sp,
                        letterSpacing = (-0.5).sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(CardCyber, RoundedCornerShape(12.dp))
                        .border(1.dp, BorderCyber, RoundedCornerShape(12.dp))
                        .wrapContentSize(Alignment.Center)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ajustes",
                        tint = CyberPurple,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pulse Status Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x1100FFCC))
                    .border(1.dp, CyberCyan.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(CyberCyan.copy(alpha = pulseAlpha), CircleShape)
                            .border(1.dp, CyberCyan, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Red Sorteo Activa • Local SQLite Encrypt",
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (raffles.isEmpty()) {
                // Futuristic empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(CardCyber.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .border(1.dp, BorderCyber, RoundedCornerShape(24.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Sin Rifas",
                            tint = CyberPurple,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "NÚCLEO SIN SISTEMAS",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = CyberPurple,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No has creado ninguna rifa aún. Toca el botón '+' abajo para iniciar tu primer portal electromecánico de sorteo.",
                            textAlign = TextAlign.Center,
                            color = Color.LightGray.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(raffles, key = { it.id }) { raffle ->
                        RaffleListItem(
                            raffle = raffle,
                            onSelect = { onRaffleSelected(raffle.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RaffleListItem(
    raffle: Raffle,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        colors = CardDefaults.cardColors(containerColor = CardCyber.copy(alpha = 0.85f)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderCyber, RoundedCornerShape(20.dp))
            .testTag("raffle_item_card_${raffle.id}")
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Title
                    Text(
                        text = raffle.title,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Prize description
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Premio",
                            tint = CyberYellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = raffle.prize,
                            color = CyberYellow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Futuristic digital ID ticket tag
                Box(
                    modifier = Modifier
                        .background(Color(0xFF23163A), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "#${raffle.id}",
                        fontFamily = FontFamily.Monospace,
                        color = CyberPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details/Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "CAPACIDAD",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.LightGray.copy(0.6f)
                    )
                    Text(
                        text = "${raffle.totalNumbers} Números",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "VALOR UNITARIO",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.LightGray.copy(0.6f)
                    )
                    Text(
                        text = formatCurrency(raffle.ticketPrice),
                        color = CyberCyan,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Micro divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BorderCyber.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sorteo metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val formattedDate = remember(raffle.createdAt) {
                    val date = Date(raffle.createdAt)
                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
                }

                Text(
                    text = "Generado: $formattedDate",
                    fontSize = 12.sp,
                    color = Color.LightGray.copy(alpha = 0.5f)
                )

                Text(
                    text = "ENTRAR A MATRIZ →",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = CyberPurple,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ==========================================
// 2. CREATE RAFFLE COMPOSABLE SHEET/DIALOG
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateRaffleDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Int, String, Double, String, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var prize by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var totalNumbers by remember { mutableStateOf(100) }
    var desc by remember { mutableStateOf("") }
    var startFromZero by remember { mutableStateOf(false) }

    var customInputTotal by remember { mutableStateOf("") }
    var showCustomTotalInput by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(2.dp, CyberPurple, RoundedCornerShape(28.dp))
            .background(CardCyber, RoundedCornerShape(28.dp)),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Text(
                    text = "NUEVO INDUCTOR DE SORTEO",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Configurar Parámetros",
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Title Input
                Text(
                    text = "NOMBRE DEL SORTEO / PRODUCTO",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CyberPurple,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Ej: Gran Sorteo PS5", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("raffle_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0x3328154D),
                        unfocusedContainerColor = Color(0x1A28154D),
                        focusedIndicatorColor = CyberCyan,
                        unfocusedIndicatorColor = BorderCyber,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Prize Input
                Text(
                    text = "PREMIO PRINCIPAL",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CyberPurple,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                TextField(
                    value = prize,
                    onValueChange = { prize = it },
                    placeholder = { Text("Ej: PlayStation 5 Slim 1TB", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0x3328154D),
                        unfocusedContainerColor = Color(0x1A28154D),
                        focusedIndicatorColor = CyberCyan,
                        unfocusedIndicatorColor = BorderCyber,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Price Input
                Text(
                    text = "VALOR POR NÚMERO ($)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CyberPurple,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                TextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    placeholder = { Text("Ej: 5000 (Solo números)", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0x3328154D),
                        unfocusedContainerColor = Color(0x1A28154D),
                        focusedIndicatorColor = CyberCyan,
                        unfocusedIndicatorColor = BorderCyber,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Total numbers presets
                Text(
                    text = "DIMENSIÓN DE LA RIFA (NÚMEROS)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CyberPurple,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val presets = listOf(50, 100, 200, 500, 1000, 1500, 2000)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { size ->
                        val isSelected = totalNumbers == size && !showCustomTotalInput
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CyberCyan else Color(0xFF25173A))
                                .border(
                                    1.dp,
                                    if (isSelected) Color.White else BorderCyber,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    totalNumbers = size
                                    showCustomTotalInput = false
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "$size",
                                color = if (isSelected) DeepSpaceBg else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Custom input selector button
                    val isCustomSelected = showCustomTotalInput
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCustomSelected) CyberCyan else Color(0xFF25173A))
                            .border(
                                1.dp,
                                if (isCustomSelected) Color.White else BorderCyber,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                showCustomTotalInput = true
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Otro...",
                            color = if (isCustomSelected) DeepSpaceBg else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                if (showCustomTotalInput) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextField(
                        value = customInputTotal,
                        onValueChange = {
                            customInputTotal = it
                            val parsed = it.toIntOrNull()
                            if (parsed != null && parsed > 0) {
                                totalNumbers = parsed
                            }
                        },
                        placeholder = { Text("Escribe ej: 75 o 5000", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0x3300FFCC),
                            unfocusedContainerColor = Color(0x1100FFCC),
                            focusedIndicatorColor = CyberCyan,
                            unfocusedIndicatorColor = CyberCyan.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Start indexing choice (00-99 vs 01-100)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF160E23))
                        .border(1.dp, BorderCyber, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Iniciar Serie en 0",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (startFromZero) "Muestra ej: 00 a ${totalNumbers - 1}" else "Muestra ej: 01 a $totalNumbers",
                            color = Color.LightGray.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = startFromZero,
                        onCheckedChange = { startFromZero = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberCyan,
                            checkedTrackColor = CyberCyan.copy(alpha = 0.4f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color(0xFF2D1E4E)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description Input
                Text(
                    text = "REGLAS / DESCRIPCIÓN (OPCIONAL)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CyberPurple,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                TextField(
                    value = desc,
                    onValueChange = { desc = it },
                    placeholder = { Text("Ej: Sorteo juega cuando se vendan todos los números...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0x3328154D),
                        unfocusedContainerColor = Color(0x1A28154D),
                        focusedIndicatorColor = CyberCyan,
                        unfocusedIndicatorColor = BorderCyber,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceVal = priceStr.toDoubleOrNull() ?: 0.0
                    onCreate(title, totalNumbers, desc, priceVal, prize, startFromZero)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("submit_raffle_button")
            ) {
                Text(
                    text = "AUTORIZAR Y COMPILAR RIFA",
                    color = DeepSpaceBg,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Abortar",
                    color = Color.LightGray,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    )
}

// ==========================================
// 3. DETAILED VIEW OF AN ACTIVE RAFFLE
// ==========================================
@Composable
fun RaffleDetailView(
    raffle: Raffle,
    soldTickets: List<TicketSold>,
    onBack: () -> Unit,
    onSellTicket: (Int, String, String, Boolean, String) -> Unit,
    onReleaseTicket: (Int) -> Unit,
    onToggleStatus: (TicketSold) -> Unit,
    onDeleteRaffle: () -> Unit,
    onOpenPortalSimulator: () -> Unit
) {
    val context = LocalContext.current

    // Quick filter states
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, FREE, SOLD, PAID, PENDING
    var searchQuery by remember { mutableStateOf("") }

    // Clicked ticket for actions
    var activeTicketClickNumber by remember { mutableStateOf<Int?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Setup sharing template link
    var showShareSheet by remember { mutableStateOf(false) }

    // Calculations
    val soldCount = soldTickets.size
    val freeCount = raffle.totalNumbers - soldCount
    val totalEstimatedEarnings = soldCount * raffle.ticketPrice
    val paidCount = soldTickets.count { it.status == "PAID" }
    val receivedEarnings = paidCount * raffle.ticketPrice
    val pendingEarnings = (soldCount - paidCount) * raffle.ticketPrice

    val startNumIndex = if (raffle.startFromZero) 0 else 1
    val endNumIndex = if (raffle.startFromZero) raffle.totalNumbers - 1 else raffle.totalNumbers

    val ticketList = remember(raffle.totalNumbers, startNumIndex) {
        (startNumIndex..endNumIndex).toList()
    }

    // Filter tickets to display
    val filteredTickets = remember(ticketList, soldTickets, selectedFilter, searchQuery, raffle.totalNumbers, raffle.startFromZero) {
        ticketList.filter { number ->
            val soldInfo = soldTickets.find { it.number == number }
            val matchesFilter = when (selectedFilter) {
                "FREE" -> soldInfo == null
                "SOLD" -> soldInfo != null
                "PAID" -> soldInfo?.status == "PAID"
                "PENDING" -> soldInfo?.status == "PENDING"
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                soldInfo != null && (
                    soldInfo.clientName.contains(searchQuery, ignoreCase = true) ||
                    soldInfo.clientPhone.contains(searchQuery, ignoreCase = true) ||
                    number.toString().contains(searchQuery)
                )
            }
            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xE60D0A14))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(CardCyber, CircleShape)
                            .border(1.dp, BorderCyber, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }

                    Text(
                        text = raffle.title,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                    )

                    IconButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier
                            .background(Color(0xFF2C131F), CircleShape)
                            .border(1.dp, CyberPink.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = CyberPink)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Stats Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .background(CardCyber.copy(alpha = 0.9f), RoundedCornerShape(24.dp))
                    .border(1.dp, BorderCyber, RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PREMIO DE SORTEO",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CyberYellow,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = raffle.prize,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0x3300FFCC), RoundedCornerShape(8.dp))
                            .border(1.dp, CyberCyan, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Ticket: ${formatCurrency(raffle.ticketPrice)}",
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatCounter(title = "Vendidos", count = "$soldCount", color = CyberPink)
                    StatCounter(title = "Disponibles", count = "$freeCount", color = CyberCyan)
                    StatCounter(title = "Recaudado", count = formatCurrency(receivedEarnings), color = CyberYellow)
                    StatCounter(title = "Pendiente", count = formatCurrency(pendingEarnings), color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons for sharing or simulation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showShareSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .border(1.dp, Color.White.copy(0.2f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Compartir Link",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = onOpenPortalSimulator,
                        colors = ButtonDefaults.buttonColors(containerColor = CardCyber),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.7f)
                            .border(1.5.dp, CyberCyan, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Terminal",
                            modifier = Modifier.size(16.dp),
                            tint = CyberCyan
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Portal Cliente",
                            color = CyberCyan,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }
                }
            }

            // Quick Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "ALL" to "Todos (${raffle.totalNumbers})",
                    "FREE" to "Libres ($freeCount)",
                    "SOLD" to "Vendidos ($soldCount)",
                    "PAID" to "Pagados ($paidCount)",
                    "PENDING" to "Pendientes (${soldCount - paidCount})"
                )

                filters.forEach { (key, label) ->
                    val isSelected = selectedFilter == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) CyberCyan else Color(0x331B112B))
                            .border(
                                1.dp,
                                if (isSelected) Color.White else BorderCyber,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedFilter = key }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) DeepSpaceBg else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar número, cliente, o teléfono...", color = Color.Gray, fontSize = 13.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF130A21),
                    unfocusedContainerColor = Color(0xFF130A21),
                    focusedIndicatorColor = CyberCyan,
                    unfocusedIndicatorColor = BorderCyber,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Main Grid of Tickets
            if (filteredTickets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin resultados para el filtro actual.",
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 64.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredTickets) { number ->
                        val soldInfo = soldTickets.find { it.number == number }
                        TicketCell(
                            number = number,
                            totalNumbers = raffle.totalNumbers,
                            startFromZero = raffle.startFromZero,
                            soldTicket = soldInfo,
                            onClick = { activeTicketClickNumber = number }
                        )
                    }
                }
            }
        }
    }

    // Modal Confirmation for Deletion
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(text = "Destruir Matriz", color = CyberPink) },
            text = { Text(text = "¿Confirmas la desintegración cuántica de la rifa '${raffle.title}'? Esta acción es irreversible y borrará todos los registros asociados.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteRaffle()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPink)
                ) {
                    Text(text = "ELIMINAR")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(text = "Cancelar", color = Color.White)
                }
            },
            containerColor = CardCyber,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(1.dp, CyberPink, RoundedCornerShape(20.dp))
        )
    }

    // Modal / Bottom Drawers for selling of tickets or detailed ticket specs
    if (activeTicketClickNumber != null) {
        val selectedNum = activeTicketClickNumber!!
        val existingTicket = soldTickets.find { it.number == selectedNum }

        if (existingTicket == null) {
            SellTicketDialog(
                ticketNumber = selectedNum,
                totalNumbers = raffle.totalNumbers,
                startFromZero = raffle.startFromZero,
                onDismiss = { activeTicketClickNumber = null },
                onConfirm = { name, phone, paid, notes ->
                    onSellTicket(selectedNum, name, phone, paid, notes)
                    activeTicketClickNumber = null
                }
            )
        } else {
            TicketDetailsDialog(
                ticket = existingTicket,
                totalNumbers = raffle.totalNumbers,
                startFromZero = raffle.startFromZero,
                ticketPrice = raffle.ticketPrice,
                onDismiss = { activeTicketClickNumber = null },
                onToggleStatus = {
                    onToggleStatus(existingTicket)
                },
                onRelease = {
                    onReleaseTicket(selectedNum)
                    activeTicketClickNumber = null
                }
            )
        }
    }

    // Share link Dialog generator
    if (showShareSheet) {
        ShareLinkDialog(
            raffle = raffle,
            soldTickets = soldTickets,
            onDismiss = { showShareSheet = false }
        )
    }
}

@Composable
fun StatCounter(title: String, count: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title.uppercase(),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.LightGray.copy(alpha = 0.5f),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = count,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

// Cell component representing each lottery ticket in the matrix grid
@Composable
fun TicketCell(
    number: Int,
    totalNumbers: Int,
    startFromZero: Boolean,
    soldTicket: TicketSold?,
    onClick: () -> Unit
) {
    val formattedNum = remember(number, totalNumbers, startFromZero) {
        formatTicketNumber(number, totalNumbers, startFromZero)
    }

    val isPaid = soldTicket?.status == "PAID"
    val isPending = soldTicket?.status == "PENDING"

    val cellColor = when {
        isPaid -> CyberPink
        isPending -> Color(0xFF321946)
        else -> Color.Transparent
    }

    val borderColor = when {
        isPaid -> CyberPink
        isPending -> CyberCyan
        else -> BorderCyber
    }

    val strokeWidth = if (soldTicket != null) 1.5.dp else 1.dp

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(cellColor)
            .border(strokeWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("ticket_cell_$number"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formattedNum,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                color = when {
                    isPaid -> DeepSpaceBg
                    isPending -> CyberCyan
                    else -> Color.LightGray
                }
            )
            // Client name preview for high density screens (initials)
            if (soldTicket != null) {
                val initials = remember(soldTicket.clientName) {
                    soldTicket.clientName.split(" ")
                        .filter { it.isNotEmpty() }
                        .take(2)
                        .map { it.first().uppercase() }
                        .joinToString("")
                }
                Text(
                    text = initials,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPaid) DeepSpaceBg.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }
        }
    }
}

// ==========================================
// 4. ACTION SHEETS & TICKETS SELLING POPUPS
// ==========================================
@Composable
fun SellTicketDialog(
    ticketNumber: Int,
    totalNumbers: Int,
    startFromZero: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Boolean, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var paid by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    val formattedNum = remember(ticketNumber, totalNumbers, startFromZero) {
        formatTicketNumber(ticketNumber, totalNumbers, startFromZero)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "REGISTRAR TICKET #$formattedNum",
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = CyberCyan,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "CLIENTE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CyberPurple,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Nombre Completo") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("client_name_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF130A21),
                        unfocusedContainerColor = Color(0xFF130A21),
                        focusedIndicatorColor = CyberCyan,
                        unfocusedIndicatorColor = BorderCyber,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "DISPOSITIVO MÓVIL / TELÉFONO",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CyberPurple,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = { Text("Ej: +56 9 1234 5678") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF130A21),
                        unfocusedContainerColor = Color(0xFF130A21),
                        focusedIndicatorColor = CyberCyan,
                        unfocusedIndicatorColor = BorderCyber,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Toggle row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF160E23))
                        .border(1.dp, BorderCyber, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Estado de Pago", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = if (paid) "COMPLETAMENTE PAGADO" else "RESERVA PENDIENTE",
                            color = if (paid) CyberCyan else CyberPink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Switch(
                        checked = paid,
                        onCheckedChange = { paid = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberCyan,
                            checkedTrackColor = CyberCyan.copy(alpha = 0.4f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color(0xFF2D1E4E)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "NOTAS DIGITALES (OPCIONAL)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CyberPurple,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Ej: Entregó seña de la mitad...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF130A21),
                        unfocusedContainerColor = Color(0xFF130A21),
                        focusedIndicatorColor = CyberCyan,
                        unfocusedIndicatorColor = BorderCyber,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, phone, paid, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                modifier = Modifier.testTag("confirm_ticket_button")
            ) {
                Text(text = "Confirmar", color = DeepSpaceBg)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar", color = Color.LightGray)
            }
        },
        containerColor = CardCyber,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.border(1.dp, BorderCyber, RoundedCornerShape(20.dp))
    )
}

@Composable
fun TicketDetailsDialog(
    ticket: TicketSold,
    totalNumbers: Int,
    startFromZero: Boolean,
    ticketPrice: Double,
    onDismiss: () -> Unit,
    onToggleStatus: () -> Unit,
    onRelease: () -> Unit
) {
    val formattedNum = remember(ticket.number, totalNumbers, startFromZero) {
        formatTicketNumber(ticket.number, totalNumbers, startFromZero)
    }

    var showReleaseConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TICKET #$formattedNum",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 20.sp
                )
                Box(
                    modifier = Modifier
                        .background(
                            if (ticket.status == "PAID") CyberPink.copy(0.15f) else Color(0x3300FFCC),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (ticket.status == "PAID") CyberPink else CyberCyan,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (ticket.status == "PAID") "PAGADO" else "PENDIENTE",
                        color = if (ticket.status == "PAID") CyberPink else CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Info block
                DetailLine(label = "Comprador", value = ticket.clientName)
                DetailLine(label = "Teléfono", value = ticket.clientPhone.ifEmpty { "Sin registro" })
                DetailLine(label = "A pagar", value = formatCurrency(ticketPrice))
                DetailLine(label = "Registrado el", value = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(ticket.soldAt)))
                if (ticket.notes.isNotEmpty()) {
                    DetailLine(label = "Anotaciones", value = ticket.notes)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Toggle payment button
                Button(
                    onClick = onToggleStatus,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (ticket.status == "PAID") Color(0xFF2A1530) else CyberCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (ticket.status == "PAID") CyberPink else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Text(
                        text = if (ticket.status == "PAID") "Revertir Pago a Pendiente" else "Marcar como PAGADO ✔",
                        color = if (ticket.status == "PAID") Color.White else DeepSpaceBg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Release number button
                Button(
                    onClick = { showReleaseConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberPink.copy(0.7f), RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = "Liberar / Hacer Disponible",
                        color = CyberPink,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cerrar Panel", color = Color.LightGray)
            }
        },
        containerColor = CardCyber,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.border(1.dp, BorderCyber, RoundedCornerShape(22.dp))
    )

    if (showReleaseConfirm) {
        AlertDialog(
            onDismissRequest = { showReleaseConfirm = false },
            title = { Text(text = "Liberar Número", color = CyberPink) },
            text = { Text(text = "¿Estás seguro de que quieres liberar el número $formattedNum? Se borrarán todos los datos del cliente asignado.") },
            confirmButton = {
                Button(
                    onClick = {
                        showReleaseConfirm = false
                        onRelease()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPink)
                ) {
                    Text(text = "CONFIRMAR LIBERACIÓN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReleaseConfirm = false }) {
                    Text(text = "Cancelar", color = Color.White)
                }
            },
            containerColor = CardCyber,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.border(1.dp, CyberPink, RoundedCornerShape(16.dp))
        )
    }
}

@Composable
fun DetailLine(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label.uppercase(),
            fontSize = 9.sp,
            color = CyberPurple,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

// ==========================================
// 5. SHARE DIALOG GENERATING LINKS & TEMPLATE
// ==========================================
@Composable
fun ShareLinkDialog(
    raffle: Raffle,
    soldTickets: List<TicketSold>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Simulate generating a unique URL with encrypted/parsed parameters containing the raffle state
    val totalSold = soldTickets.size
    val occupiedString = soldTickets.take(30).map { it.number }.joinToString(",")
    val overflow = if (soldTickets.size > 30) "..." else ""
    val clientAppUrl = "https://rifaspro.quantum/seleccionar?id=${raffle.id}&tot=${raffle.totalNumbers}&sz=${if (raffle.startFromZero) 1 else 0}&occ=$occupiedString$overflow"

    // Construct beautiful messaging script
    val sharingMessageText = """
🚀 *¡RECLAMA TU NÚMERO GANADOR!* 🚀

Participa en la rifa interactiva: *"${raffle.title}"*
🎁 *PREMIO:* ${raffle.prize}
🎫 *VALOR NÚMERO:* ${formatCurrency(raffle.ticketPrice)}

Elige tu número de la suerte directamente desde el *Portal de Selección Inteligente* ingresando aquí:
👉 $clientAppUrl

_Sorteo gestionado en el ecosistema cuántico de Rifas Pro._
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "ENLACE ELECTRÓNICO COPIADO",
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = CyberCyan,
                fontSize = 16.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Se ha computado una URL dinámica que incluye el estado actual de tus números ocupados. El cliente podrá abrirla para pre-reservar directamente:",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF130A21))
                        .border(1.dp, BorderCyber, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = clientAppUrl,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyberCyan,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Mensaje a enviar (Modo portapapeles):",
                    fontSize = 11.sp,
                    color = CyberPurple,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 120.dp)
                        .verticalScroll(rememberScrollState())
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1C132C))
                        .padding(10.dp)
                ) {
                    Text(
                        text = sharingMessageText,
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Mensaje Rifa", sharingMessageText)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "¡Mensaje copiado con éxito!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Text(text = "COPIAR MENSAJE ✔", color = DeepSpaceBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cerrar", color = Color.White)
            }
        },
        containerColor = CardCyber,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.border(1.dp, CyberPurple, RoundedCornerShape(20.dp))
    )
}

// ==========================================
// 6. CLIENT PORTAL SIMULATOR (100% WORKING!)
// ==========================================
@Composable
fun ClientPortalSimulator(
    raffle: Raffle,
    soldTickets: List<TicketSold>,
    onBack: () -> Unit,
    onClaimTicket: (Int, String, String) -> Unit
) {
    val context = LocalContext.current

    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var selectedNumberForClaim by remember { mutableStateOf<Int?>(null) }

    val startNumIndex = if (raffle.startFromZero) 0 else 1
    val endNumIndex = if (raffle.startFromZero) raffle.totalNumbers - 1 else raffle.totalNumbers

    val ticketList = remember(raffle.totalNumbers, startNumIndex) {
        (startNumIndex..endNumIndex).toList()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B0F2B))
                    .padding(vertical = 12.dp, horizontal = 16.dp)
                    .drawBehind {
                        // Drawing futuristic network indicators
                        drawLine(
                            color = CyberCyan,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 2f
                        )
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(CyberCyan, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PORTAL_CLIENTE_SIMULADOR.SYS",
                            color = CyberCyan,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFF351F4B), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Simulated Phone Frame Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F071C))
                    .border(1.5.dp, CyberPurple, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "📱 PORTAL WEB RECLAMADOR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberPurple,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Elige tu número de la suerte:",
                        fontSize = 14.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = raffle.title,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = raffle.prize,
                        color = CyberYellow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Valor: ${formatCurrency(raffle.ticketPrice)}",
                        color = CyberCyan,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Information details input
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardCyber.copy(0.7f), RoundedCornerShape(16.dp))
                    .border(1.dp, BorderCyber, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "1. INGRESA TUS DATOS DE IDENTIDAD",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    placeholder = { Text("Escribe tu nombre...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0E0716),
                        unfocusedContainerColor = Color(0xFF0E0716),
                        focusedIndicatorColor = CyberCyan,
                        unfocusedIndicatorColor = BorderCyber,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                TextField(
                    value = clientPhone,
                    onValueChange = { clientPhone = it },
                    placeholder = { Text("Número de teléfono (ej. 987654321)", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0E0716),
                        unfocusedContainerColor = Color(0xFF0E0716),
                        focusedIndicatorColor = CyberCyan,
                        unfocusedIndicatorColor = BorderCyber,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Numbers Matrix grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardCyber.copy(0.7f), RoundedCornerShape(16.dp))
                    .border(1.dp, BorderCyber, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "2. SELECCIONA TU TICKET DE LA LISTA",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Los números bloqueados en color rosa ya pertenecen a otro participante.",
                    fontSize = 12.sp,
                    color = Color.LightGray.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Create visual compact grid list inside column scroll (lazy grid fails inside vertical scrolls)
                val rowSize = 4
                val chunkedNumbers = ticketList.chunked(rowSize)

                chunkedNumbers.forEach { numberRow ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        numberRow.forEach { number ->
                            val soldInfo = soldTickets.find { it.number == number }
                            val isSelected = selectedNumberForClaim == number
                            val isSold = soldInfo != null

                            val bkg = when {
                                isSold -> Color(0xFF2C131F)
                                isSelected -> CyberCyan
                                else -> Color(0xFF130922)
                            }
                            val bdr = when {
                                isSold -> CyberPink.copy(0.3f)
                                isSelected -> Color.White
                                else -> BorderCyber
                            }
                            val textCol = when {
                                isSold -> CyberPink
                                isSelected -> DeepSpaceBg
                                else -> Color.LightGray
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.2f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bkg)
                                    .border(1.dp, bdr, RoundedCornerShape(8.dp))
                                    .clickable(enabled = !isSold) {
                                        selectedNumberForClaim = number
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = formatTicketNumber(number, raffle.totalNumbers, raffle.startFromZero),
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = textCol
                                )
                            }
                        }
                        // Pad row if not full
                        if (numberRow.size < rowSize) {
                            repeat(rowSize - numberRow.size) {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Proceed Reservation Button
            Button(
                onClick = {
                    if (clientName.isBlank() || clientPhone.isBlank()) {
                        Toast.makeText(context, "Por favor completa tu nombre y contacto.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedNumberForClaim == null) {
                        Toast.makeText(context, "Por favor selecciona un número disponible.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    onClaimTicket(selectedNumberForClaim!!, clientName, clientPhone)
                    Toast.makeText(context, "¡Número reservado exitosamente!", Toast.LENGTH_LONG).show()

                    // Reset choices & close
                    clientName = ""
                    clientPhone = ""
                    selectedNumberForClaim = null
                    onBack()
                },
                enabled = clientName.isNotBlank() && clientPhone.isNotBlank() && selectedNumberForClaim != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(
                        1.5.dp,
                        if (selectedNumberForClaim != null) Color.White else Color.Transparent,
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Text(
                    text = "RESERVAR NÚMERO " + (selectedNumberForClaim?.let {
                        "#" + formatTicketNumber(it, raffle.totalNumbers, raffle.startFromZero)
                    } ?: ""),
                    color = DeepSpaceBg,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "✓ Sus datos se almacenan de forma local en la matriz del propietario sincronizando su dispositivo.",
                color = Color.LightGray.copy(alpha = 0.4f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
