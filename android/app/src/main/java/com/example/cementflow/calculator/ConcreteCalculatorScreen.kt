package com.example.cementflow.calculator

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.math.BigDecimal

private data class ConcreteOption(val name: String, val price: BigDecimal)

private val concreteOptions = listOf(
    ConcreteOption("М200", BigDecimal("4000")),
    ConcreteOption("М300", BigDecimal("5000")),
    ConcreteOption("М400", BigDecimal("6000")),
)

private val red = Color(0xFF96171A)
private val darkGrey = Color(0xFF454545)
private val lightGrey = Color(0xFFEEF0F1)

/** Учебный калькулятор не отправляет данные в заказ и не обращается к серверу. */
@Composable
fun ConcreteCalculatorScreen(onBack: () -> Unit) {
    var optionIndex by rememberSaveable { mutableStateOf(0) }
    var volume by rememberSaveable { mutableStateOf("") }
    var result by rememberSaveable { mutableStateOf<String?>(null) }
    var error by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE9EAED), lightGrey, Color(0xFFF8F5F5))))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextButton(onClick = onBack) { Text("← Назад", color = red) }
        Text("Расчёт бетона", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = darkGrey)
        Text(
            "Учебный расчёт. Цены условные и не являются предложением о продаже. Рецептуру определяет технолог.",
            style = MaterialTheme.typography.bodyMedium,
            color = darkGrey,
        )
        Text("Выберите марку", style = MaterialTheme.typography.titleMedium, color = darkGrey)
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.72f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
        ) {
            Column {
                concreteOptions.forEachIndexed { index, option ->
                    val selectionColor by animateColorAsState(
                        targetValue = if (optionIndex == index) red.copy(alpha = 0.07f) else Color.Transparent,
                        animationSpec = tween(150),
                        label = "concrete-selection",
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(selectionColor)
                            .selectable(selected = optionIndex == index, role = Role.RadioButton) {
                                optionIndex = index
                                result = null
                            }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = optionIndex == index, onClick = null)
                        Column(Modifier.padding(start = 8.dp)) {
                            Text(option.name, style = MaterialTheme.typography.titleMedium, color = darkGrey)
                            Text("${option.price.toPlainString()} ₽/м³ · учебная цена", style = MaterialTheme.typography.bodyMedium, color = darkGrey)
                        }
                    }
                }
            }
        }
        OutlinedTextField(
            value = volume,
            onValueChange = {
                volume = it
                result = null
                error = false
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Объём, м³") },
            placeholder = { Text("Например, 2,5") },
            singleLine = true,
            isError = error,
            supportingText = { if (error) Text("Введите положительное число, например 2,5") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        Button(
            onClick = {
                val cost = calculateConcreteCost(volume, concreteOptions[optionIndex].price)
                error = cost == null
                result = cost?.toPlainString()?.replace('.', ',')
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = red),
        ) { Text("Рассчитать") }
        result?.let { amount ->
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.78f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.9f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Ориентировочная стоимость", style = MaterialTheme.typography.titleMedium, color = darkGrey)
                    Spacer(Modifier.height(8.dp))
                    Text("$amount ₽", style = MaterialTheme.typography.headlineMedium, color = red, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Расчёт учебный, без стоимости доставки.", style = MaterialTheme.typography.bodyMedium, color = darkGrey)
                }
            }
        }
    }
}

