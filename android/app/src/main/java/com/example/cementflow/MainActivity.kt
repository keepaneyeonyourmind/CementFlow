package com.example.cementflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cementflow.calculator.ConcreteCalculatorScreen
import com.example.cementflow.orders.OrdersDeliveryScreen

private val Red = Color(0xFF96171A)
private val BlueGrey = Color(0xFF8F98A3)
private val DarkGrey = Color(0xFF454545)
private val LightGrey = Color(0xFFEEF0F1)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(
                primary = Red,
                secondary = BlueGrey,
                background = LightGrey,
                surface = Color.White,
                onSurface = DarkGrey,
            )) {
                var screen by rememberSaveable { mutableStateOf("home") }
                BackHandler(enabled = screen != "home") { screen = "home" }
                when (screen) {
                    "calculator" -> ConcreteCalculatorScreen(onBack = { screen = "home" })
                    "orders" -> OrdersDeliveryScreen(onBack = { screen = "home" })
                    else -> HomeScreen(onCalculator = { screen = "calculator" }, onOrders = { screen = "orders" })
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(onCalculator: () -> Unit, onOrders: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFFF8F8F8), Color(0xFFEEF0F1), Color(0xFFE9EAED)))
        )
    ) {
        // Цветные пятна под полупрозрачными панелями создают мягкий эффект стекла.
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(listOf(Red.copy(alpha = 0.12f), Color.Transparent), center = Offset(size.width, size.height * 0.17f), radius = size.width * 0.9f),
                radius = size.width * 0.9f,
                center = Offset(size.width, size.height * 0.17f),
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(BlueGrey.copy(alpha = 0.22f), Color.Transparent), center = Offset(0f, size.height * 0.69f), radius = size.width * 0.8f),
                radius = size.width * 0.8f,
                center = Offset(0f, size.height * 0.69f),
            )
        }
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.size(12.dp).clip(RoundedCornerShape(4.dp)).background(Red)
                )
                Text("CEMENTFLOW", color = Red, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(32.dp))
            Text("Всё для учебного заказа", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = DarkGrey)
            Spacer(Modifier.height(10.dp))
            Text("Расчёт бетона, заказ цемента и доставка — в одном приложении.", style = MaterialTheme.typography.bodyLarge, color = DarkGrey)
            Spacer(Modifier.height(25.dp))
            val infoShape = RoundedCornerShape(24.dp)
            Surface(
                shape = infoShape,
                color = Red.copy(alpha = 0.93f),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.35f), infoShape),
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("УЧЕБНЫЙ ПРОТОТИП", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Данные и цены демонстрационные. Расчёт не заменяет консультацию технолога.", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(30.dp))
            Text("Разделы", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DarkGrey)
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureCard("01", "Расчёт бетона", "Ориентировочная стоимость смеси", onCalculator)
                FeatureCard("02", "Заказ цемента", "Каталог и оформление заказа", onOrders)
                FeatureCard("03", "Доставка", "Назначение и изменение статуса", onOrders)
            }
            Spacer(Modifier.height(24.dp))
            Text("Для заказов нужен учебный сервер и база данных.", style = MaterialTheme.typography.bodyMedium, color = DarkGrey)
        }
    }
}

@Composable
private fun FeatureCard(number: String, title: String, description: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, animationSpec = tween(150), label = "card press")
    val shape = RoundedCornerShape(24.dp)
    Surface(
        shape = shape,
        color = Color.White.copy(alpha = 0.74f),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().scale(scale).border(1.dp, Color.White.copy(alpha = 0.9f), shape)
            .clickable(interactionSource = interaction, indication = LocalIndication.current, onClick = onClick),
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(15.dp))
                    .background(Brush.linearGradient(listOf(Color.White, Color(0xFFE9EAED)))),
                contentAlignment = Alignment.Center,
            ) { Text(number, color = Red, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge) }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DarkGrey)
                Spacer(Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodyMedium, color = DarkGrey)
            }
            Text("›", color = Red, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

