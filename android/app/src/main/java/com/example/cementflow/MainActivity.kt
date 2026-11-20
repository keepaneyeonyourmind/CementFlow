package com.example.cementflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cementflow.calculator.ConcreteCalculatorScreen
import com.example.cementflow.orders.OrdersDeliveryScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val transparent = android.graphics.Color.TRANSPARENT
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(transparent, transparent),
            navigationBarStyle = SystemBarStyle.light(transparent, android.graphics.Color.BLACK),
        )
        setContent {
            MaterialTheme(colorScheme = CementColors) {
                var tab by rememberSaveable { mutableStateOf(CementTab.HOME) }
                val onTabSelected: (CementTab) -> Unit = { tab = it }
                BackHandler(enabled = tab != CementTab.HOME) { tab = CementTab.HOME }
                when (tab) {
                    CementTab.HOME -> HomeScreen(onTabSelected)
                    CementTab.CALCULATOR -> ConcreteCalculatorScreen(onTabSelected)
                    CementTab.ORDERS -> OrdersDeliveryScreen(onTabSelected)
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(onTabSelected: (CementTab) -> Unit) {
    GlassScreen(CementTab.HOME, onTabSelected, spacing = 22.dp) {
        ScreenHeading("Главная", "CementFlow")
        RedGlassPanel(
            label = "Учебный прототип",
            title = "Бетон и цемент",
            detail = "Данные и цены демонстрационные.",
            minHeight = 184.dp,
        )
        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Text("Разделы", color = CementInk, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HomeShortcut("Расчёт бетона", CalculatorIcon, Modifier.weight(1f)) {
                    onTabSelected(CementTab.CALCULATOR)
                }
                HomeShortcut("Заказ цемента", OrdersIcon, Modifier.weight(1f)) {
                    onTabSelected(CementTab.ORDERS)
                }
                HomeShortcut("Доставка", DeliveryIcon, Modifier.weight(1f)) {
                    onTabSelected(CementTab.ORDERS)
                }
            }
        }
        GlassPanel(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Учебный сервер", color = CementInk, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Для заказов и доставки нужен локальный сервер.",
                    color = CementMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun HomeShortcut(title: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    GlassPanel(modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(min = 106.dp)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 6.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.size(37.dp).background(
                    Brush.verticalGradient(listOf(Color.White, CementSelection)),
                    RoundedCornerShape(12.dp),
                ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(23.dp), tint = CementAccent)
            }
            Text(
                title,
                textAlign = TextAlign.Center,
                color = CementInk,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
