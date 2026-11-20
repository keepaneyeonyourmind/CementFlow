package com.example.cementflow

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val CementAccent = Color(0xFFB71B22)
internal val CementInk = Color(0xFF221B1D)
internal val CementMuted = Color(0xFF63555A)
internal val CementFieldShape = RoundedCornerShape(16.dp)
internal val CementControlShape = RoundedCornerShape(18.dp)
internal val CementSelection = Color(0xFFFFE6E7)

internal val CementColors = lightColorScheme(
    primary = Color(0xFF96171A),
    onPrimary = Color.White,
    primaryContainer = CementSelection,
    onPrimaryContainer = Color(0xFF751017),
    secondary = CementMuted,
    background = Color(0xFFF0ECEF),
    onBackground = CementInk,
    surface = Color(0xFFF9F5F6),
    onSurface = CementInk,
    onSurfaceVariant = CementMuted,
    outline = Color(0xFF8E7B82),
    outlineVariant = Color(0xFFD9C9CF),
)

internal enum class CementTab(val label: String) {
    HOME("Главная"),
    CALCULATOR("Расчёт"),
    ORDERS("Заказы"),
}

@Composable
internal fun GlassScreen(
    tab: CementTab,
    onTabSelected: (CementTab) -> Unit,
    navigationEnabled: Boolean = true,
    spacing: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFFF9F5F6), Color(0xFFEFEAEC), Color(0xFFEAE4E8)))
        ),
        contentAlignment = Alignment.TopCenter,
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawRect(Brush.radialGradient(
                listOf(Color(0x32F38A92), Color.Transparent),
                center = Offset(size.width, size.height * 0.26f),
                radius = size.width * 0.78f,
            ))
            drawRect(Brush.radialGradient(
                listOf(Color(0x30E3A4AF), Color.Transparent),
                center = Offset(0f, size.height * 0.93f),
                radius = size.width * 0.88f,
            ))
        }
        Column(Modifier.safeDrawingPadding().widthIn(max = 560.dp).fillMaxSize()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(spacing),
                content = content,
            )
            if (!keyboardVisible) {
                GlassTabBar(tab, onTabSelected, navigationEnabled, Modifier.padding(horizontal = 18.dp, vertical = 8.dp))
            }
        }
    }
}

@Composable
internal fun ScreenHeading(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.8).sp,
            color = CementInk,
        )
        subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = CementMuted) }
    }
}

@Composable
internal fun GlassPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)
    Surface(
        modifier = modifier.shadow(5.dp, shape, clip = false, ambientColor = CementMuted, spotColor = CementMuted),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.9f)),
    ) {
        Box(Modifier.background(Brush.verticalGradient(listOf(Color.White, Color(0xFFF4EEF1))))) {
            content()
        }
    }
}

@Composable
internal fun RedGlassPanel(
    label: String,
    title: String,
    detail: String,
    modifier: Modifier = Modifier,
    minHeight: Dp = 148.dp,
) {
    val shape = RoundedCornerShape(26.dp)
    Surface(
        modifier = modifier.fillMaxWidth().shadow(12.dp, shape, clip = false, ambientColor = CementAccent, spotColor = CementAccent),
        shape = shape,
        color = Color(0xFF78181F),
        border = BorderStroke(1.dp, Color(0xFFF9BBC1)),
    ) {
        Box(Modifier.fillMaxWidth().heightIn(min = minHeight), contentAlignment = Alignment.CenterStart) {
            GlassWaves(Modifier.matchParentSize())
            Column(Modifier.padding(horizontal = 21.dp, vertical = 22.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(label, color = Color(0xFFFFE5E7), style = MaterialTheme.typography.labelLarge)
                Text(title, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(detail, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun GlassWaves(modifier: Modifier) {
    Canvas(modifier) {
        scale(size.width / 360f, size.height / 180f, pivot = Offset.Zero) {
            val canvasSize = Size(360f, 180f)
            val crest = Path().apply {
                moveTo(0f, 161f)
                cubicTo(74f, 180f, 142f, 175f, 194f, 116f)
                cubicTo(246f, 56f, 257f, 23f, 306f, 29f)
                cubicTo(328f, 32f, 343f, 42f, 360f, 49f)
            }
            val redWave = Path().apply {
                addPath(crest)
                lineTo(360f, 180f)
                lineTo(0f, 180f)
                close()
            }
            val lowerCrest = Path().apply {
                moveTo(0f, 175f)
                cubicTo(49f, 139f, 76f, 147f, 127f, 158f)
                cubicTo(202f, 172f, 229f, 106f, 276f, 99f)
                cubicTo(311f, 93f, 339f, 117f, 360f, 138f)
            }
            val pinkWave = Path().apply {
                addPath(lowerCrest)
                lineTo(360f, 180f)
                lineTo(0f, 180f)
                close()
            }
            drawRect(Brush.linearGradient(
                listOf(Color(0xFF893A42), Color(0xFF58191E), Color(0xFF7C141E)),
                end = Offset(360f, 158f),
            ), size = canvasSize)
            drawRect(Brush.radialGradient(
                listOf(Color(0xBFFFE0E4), Color(0x44F1969D), Color.Transparent),
                center = Offset(339f, -12f), radius = 178f,
            ), size = canvasSize)
            drawPath(redWave, Brush.linearGradient(
                listOf(Color(0xFF891822), Color(0xFFF4313D), Color(0xFF7D0714), Color(0xFFD43A49)),
                start = Offset(151f, 177f), end = Offset(335f, 23f),
            ))
            drawPath(crest, Color(0x49FF5860), style = Stroke(6f))
            drawPath(crest, Color(0xCFFFF0F2), style = Stroke(0.9f))
            drawPath(pinkWave, Brush.linearGradient(
                listOf(Color(0x53FFD6DD), Color(0x45EF7B87), Color(0xBBFFB7BF)),
                start = Offset(0f, 134f), end = Offset(346f, 180f),
            ))
            drawPath(lowerCrest, Color(0xA6FFD5DD), style = Stroke(0.9f))
            drawRect(Brush.horizontalGradient(
                0f to Color(0xA352111A),
                0.55f to Color(0x7849141A),
                1f to Color.Transparent,
                endX = 360f,
            ), size = canvasSize)
            drawRect(Brush.verticalGradient(
                0f to Color(0x55FFFFFF),
                0.17f to Color.Transparent,
                0.88f to Color.Transparent,
                1f to Color(0x38FFC5CA),
                endY = 180f,
            ), size = canvasSize)
        }
    }
}

@Composable
private fun GlassTabBar(
    selectedTab: CementTab,
    onTabSelected: (CementTab) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val capsule = RoundedCornerShape(34.dp)
    val itemShape = RoundedCornerShape(28.dp)
    Surface(
        modifier = modifier.fillMaxWidth().shadow(10.dp, capsule, clip = false, ambientColor = CementMuted, spotColor = CementMuted),
        shape = capsule,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Color.White),
    ) {
        Row(
            Modifier.background(Brush.verticalGradient(listOf(Color.White, Color(0xFFF3ECEF))))
                .padding(5.dp).selectableGroup(),
        ) {
            CementTab.entries.forEach { tab ->
                val selected = selectedTab == tab
                val tint = when {
                    selected -> CementAccent
                    enabled -> CementInk
                    else -> CementMuted.copy(alpha = 0.62f)
                }
                val selection = if (selected) Modifier.background(
                    Brush.radialGradient(listOf(Color(0xFFFFDADC), Color.Transparent))
                ) else Modifier
                Column(
                    modifier = Modifier.weight(1f).clip(itemShape).then(selection)
                        .selectable(selected = selected, enabled = enabled, role = Role.Tab) {
                            if (!selected) onTabSelected(tab)
                        }
                        .heightIn(min = 54.dp).padding(horizontal = 2.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(tab.icon, contentDescription = null, modifier = Modifier.size(23.dp), tint = tint)
                    Text(
                        tab.label,
                        color = tint,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
