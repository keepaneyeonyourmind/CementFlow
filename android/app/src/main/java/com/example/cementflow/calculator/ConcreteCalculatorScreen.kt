package com.example.cementflow.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cementflow.CementControlShape
import com.example.cementflow.CementFieldShape
import com.example.cementflow.CementInk
import com.example.cementflow.CementMuted
import com.example.cementflow.CementSelection
import com.example.cementflow.CementTab
import com.example.cementflow.GlassPanel
import com.example.cementflow.GlassScreen
import com.example.cementflow.RedGlassPanel
import com.example.cementflow.ScreenHeading
import java.math.BigDecimal

private data class ConcreteOption(val name: String, val price: BigDecimal)

private val concreteOptions = listOf(
    ConcreteOption("М200", BigDecimal("4000")),
    ConcreteOption("М300", BigDecimal("5000")),
    ConcreteOption("М400", BigDecimal("6000")),
)

/** Учебный калькулятор не отправляет данные в заказ и не обращается к серверу. */
@Composable
internal fun ConcreteCalculatorScreen(onTabSelected: (CementTab) -> Unit) {
    var optionIndex by rememberSaveable { mutableStateOf(0) }
    var volume by rememberSaveable { mutableStateOf("") }
    var result by rememberSaveable { mutableStateOf<String?>(null) }
    var error by rememberSaveable { mutableStateOf(false) }

    GlassScreen(CementTab.CALCULATOR, onTabSelected) {
        ScreenHeading(
            "Расчёт бетона",
            "Учебный расчёт. Цены условные и не являются предложением о продаже. Рецептуру определяет технолог.",
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Выберите марку", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CementInk)
            GlassPanel(Modifier.fillMaxWidth()) {
                Column(Modifier.selectableGroup()) {
                    concreteOptions.forEachIndexed { index, option ->
                        val selected = optionIndex == index
                        if (index > 0) HorizontalDivider(Modifier.padding(horizontal = 14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .background(if (selected) CementSelection else Color.Transparent)
                                .selectable(selected = selected, role = Role.RadioButton) {
                                    optionIndex = index
                                    result = null
                                }
                                .heightIn(min = 61.dp)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = selected, onClick = null)
                            Column {
                                Text(option.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = CementInk)
                                Text("${option.price.toPlainString()} ₽/м³ · учебная цена", style = MaterialTheme.typography.bodyMedium, color = CementMuted)
                            }
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
            shape = CementFieldShape,
        )
        Button(
            onClick = {
                val cost = calculateConcreteCost(volume, concreteOptions[optionIndex].price)
                error = cost == null
                result = cost?.toPlainString()?.replace('.', ',')
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = CementControlShape,
        ) { Text("Рассчитать") }
        result?.let { amount ->
            RedGlassPanel("Ориентировочная стоимость", "$amount ₽", "Расчёт учебный, без стоимости доставки.")
        }
    }
}

