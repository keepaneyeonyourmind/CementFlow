package com.example.cementflow.orders

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cementflow.CementAccent
import com.example.cementflow.CementControlShape
import com.example.cementflow.CementFieldShape
import com.example.cementflow.CementInk
import com.example.cementflow.CementMuted
import com.example.cementflow.CementSelection
import com.example.cementflow.CementTab
import com.example.cementflow.GlassPanel
import com.example.cementflow.GlassScreen
import com.example.cementflow.OrdersIcon
import com.example.cementflow.RedGlassPanel
import com.example.cementflow.ScreenHeading
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/** Демонстрация заказов и доставки без авторизации; это не персональные данные. */
@Composable
internal fun OrdersDeliveryScreen(onTabSelected: (CementTab) -> Unit) {
    val context = LocalContext.current
    var allowed by remember { mutableStateOf(Build.VERSION.SDK_INT < 37 || context.checkSelfPermission(Manifest.permission.ACCESS_LOCAL_NETWORK) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> allowed = granted }
    if (allowed) {
        OrdersDeliveryContent(onTabSelected)
    } else {
        GlassScreen(CementTab.ORDERS, onTabSelected) {
            ScreenHeading("Заказы и доставка")
            GlassPanel(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Доступ к учебному серверу", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CementInk)
                    Text("Чтобы открыть заказы и доставку, разрешите приложению подключаться к локальной сети. Данные используются только для учебной демонстрации.", color = CementMuted)
                    Button(
                        onClick = { launcher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK) },
                        shape = CementControlShape,
                    ) { Text("Разрешить доступ") }
                }
            }
        }
    }
}

@Composable
private fun OrdersDeliveryContent(onTabSelected: (CementTab) -> Unit) {
    val api = remember { DemoApi() }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val alive = remember { AtomicBoolean(true) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    var isStaff by rememberSaveable { mutableStateOf(false) }
    var showForm by rememberSaveable { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    BackHandler(enabled = busy) { }
    var products by remember { mutableStateOf<List<DemoProduct>>(emptyList()) }
    var orders by remember { mutableStateOf<List<DemoOrder>>(emptyList()) }
    var productsLoaded by remember { mutableStateOf(false) }
    var ordersLoaded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    var selectedProduct by rememberSaveable { mutableStateOf<Long?>(null) }
    var quantity by rememberSaveable { mutableStateOf("1") }
    var customer by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var driverNames by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }

    DisposableEffect(Unit) {
        alive.set(true)
        onDispose { alive.set(false); executor.shutdownNow() }
    }

    fun <T> perform(mutating: Boolean = false, task: () -> T, success: (T) -> Unit) {
        if (busy) return
        busy = true
        error = null
        executor.execute {
            val outcome = runCatching(task)
            mainHandler.post {
                if (alive.get()) {
                    busy = false
                    outcome.onSuccess(success).onFailure { cause ->
                        error = if (mutating && (cause !is ApiException || cause.uncertain)) {
                            "Нет связи с сервером; результат операции неизвестен. Обновите список заказов перед повтором."
                        } else if (cause is ApiException) cause.message else "Не удалось обработать ответ сервера. Попробуйте обновить список."
                    }
                }
            }
        }
    }

    fun refreshOrders() {
        perform(task = { api.orders() }) { value -> orders = value; ordersLoaded = true }
    }
    fun refreshProducts() {
        perform(task = { api.products() }) { value ->
            products = value
            productsLoaded = true
            if (value.none { it.id == selectedProduct }) selectedProduct = value.firstOrNull()?.id
        }
    }
    LaunchedEffect(isStaff, showForm) {
        if (showForm && !isStaff) refreshProducts() else refreshOrders()
    }

    GlassScreen(CementTab.ORDERS, onTabSelected, navigationEnabled = !busy, spacing = 12.dp) {
        ScreenHeading("Заказы и доставка")
        RedGlassPanel(
            label = "Учебные заказы",
            title = if (isStaff) "Режим сотрудника" else "Режим клиента",
            detail = "Обе роли видят общий список заказов. Авторизации нет.",
            minHeight = 138.dp,
        )
        OrderRoleSelector(isStaff, enabled = !busy) { staff ->
            isStaff = staff
            showForm = false
            notice = null
        }
        if (busy) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                Text("Загрузка…", color = CementMuted)
            }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
        notice?.let { Text(it, color = CementAccent, style = MaterialTheme.typography.bodyMedium) }

        if (!isStaff && showForm) {
            Text("Учебный каталог", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CementInk)
            if (productsLoaded && products.isEmpty()) Text("В каталоге пока нет товаров.", color = CementMuted)
            if (products.isNotEmpty()) {
                GlassPanel(Modifier.fillMaxWidth()) {
                    Column(Modifier.selectableGroup()) {
                        products.forEachIndexed { index, item ->
                            val selected = selectedProduct == item.id
                            if (index > 0) HorizontalDivider(Modifier.padding(horizontal = 14.dp))
                            Row(
                                Modifier.fillMaxWidth()
                                    .background(if (selected) CementSelection else Color.Transparent)
                                    .selectable(selected = selected, role = Role.RadioButton, enabled = !busy) { selectedProduct = item.id }
                                    .heightIn(min = 58.dp).padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = selected, onClick = null, enabled = !busy)
                                Column {
                                    Text(item.name, style = MaterialTheme.typography.titleMedium, color = CementInk)
                                    Text("${item.price} ₽ / ${item.unit}", style = MaterialTheme.typography.bodyMedium, color = CementMuted)
                                }
                            }
                        }
                    }
                }
            }
            OutlinedTextField(quantity, { quantity = it }, label = { Text("Количество") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), enabled = !busy, shape = CementFieldShape)
            OutlinedTextField(customer, { customer = it }, label = { Text("Имя (учебное)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), enabled = !busy, shape = CementFieldShape)
            OutlinedTextField(phone, { phone = it }, label = { Text("Телефон (учебный)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), enabled = !busy, shape = CementFieldShape)
            OutlinedTextField(address, { address = it }, label = { Text("Адрес (учебный)") }, modifier = Modifier.fillMaxWidth(), enabled = !busy, shape = CementFieldShape)
            Button(enabled = !busy, onClick = {
                val itemId = selectedProduct
                val count = quantity.toIntOrNull()
                if (itemId == null || count == null || count !in 1..10000 || customer.trim().length !in 1..100 || phone.trim().length !in 1..100 || address.trim().length !in 1..300) {
                    error = "Выберите товар, укажите количество от 1 до 10000, имя, телефон и адрес. Имя и телефон — до 100 символов, адрес — до 300."
                } else {
                    perform(mutating = true, task = { api.createOrder(itemId, count, customer, phone, address) }) { order ->
                        notice = "Учебный заказ №${order.id} создан."
                        customer = ""; phone = ""; address = ""; quantity = "1"; showForm = false
                    }
                }
            }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = CementControlShape) { Text("Оформить учебный заказ") }
            OutlinedButton(enabled = !busy, onClick = { showForm = false; error = null }, modifier = Modifier.fillMaxWidth(), shape = CementControlShape) { Text("К списку заказов") }
            OutlinedButton(enabled = !busy, onClick = { refreshProducts() }, modifier = Modifier.fillMaxWidth(), shape = CementControlShape) { Text("Обновить каталог") }
        } else {
            if (!isStaff) Button(
                enabled = !busy,
                onClick = { showForm = true; notice = null; error = null },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = CementControlShape,
            ) { Text("Новый учебный заказ") }
            OutlinedButton(
                enabled = !busy,
                onClick = { refreshOrders() },
                modifier = Modifier.fillMaxWidth(),
                shape = CementControlShape,
            ) { Text("Обновить заказы") }
            if (ordersLoaded && orders.isEmpty()) {
                GlassPanel(Modifier.fillMaxWidth()) {
                    Text("Заказов пока нет.", Modifier.padding(18.dp), color = CementMuted)
                }
            }
            orders.forEach { order ->
                GlassPanel(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(OrdersIcon, contentDescription = null, modifier = Modifier.size(23.dp), tint = CementAccent)
                            Text("Заказ №${order.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CementInk)
                        }
                        Text("${order.productName}: ${order.quantity} ${order.unit}", color = CementInk)
                        Text("Учебная сумма: ${order.total} ₽", color = CementInk, fontWeight = FontWeight.SemiBold)
                        Surface(shape = CementFieldShape, color = CementSelection) {
                            Text(
                                "Доставка: ${statusLabel(order.status)}",
                                Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                        HorizontalDivider()
                        Text("Клиент: ${order.customerName}\nТелефон: ${order.phone}\nАдрес: ${order.address}", color = CementMuted)
                        order.driverName?.let { Text("Водитель: $it", color = CementMuted) }
                        if (isStaff && order.status == "WAITING") {
                            val driver = driverNames[order.id].orEmpty()
                            OutlinedTextField(driver, { driverNames = driverNames + (order.id to it) }, label = { Text("Имя водителя (учебное)") }, singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth(), shape = CementFieldShape)
                            Button(enabled = !busy, shape = CementControlShape, onClick = {
                                if (driver.trim().length !in 1..100) error = "Введите имя водителя длиной от 1 до 100 символов."
                                else perform(mutating = true, task = { api.assign(order.id, driver) }) { changed ->
                                    orders = orders.map { if (it.id == changed.id) changed else it }
                                    driverNames = driverNames - order.id
                                    notice = "Доставка назначена."
                                }
                            }) { Text("Назначить доставку") }
                        }
                        val next = when (order.status) { "ASSIGNED" -> "IN_TRANSIT"; "IN_TRANSIT" -> "DELIVERED"; else -> null }
                        if (isStaff && next != null) {
                            Button(enabled = !busy, shape = CementControlShape, onClick = {
                                perform(mutating = true, task = { api.advance(order.id, next) }) { changed ->
                                    orders = orders.map { if (it.id == changed.id) changed else it }
                                    notice = "Статус доставки изменён."
                                }
                            }) { Text(if (next == "IN_TRANSIT") "Отправить в путь" else "Отметить доставленной") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderRoleSelector(isStaff: Boolean, enabled: Boolean, onSelect: (Boolean) -> Unit) {
    GlassPanel(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(5.dp).selectableGroup(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(false to "Клиент", true to "Сотрудник").forEach { (staff, title) ->
                val selected = isStaff == staff
                Text(
                    title,
                    modifier = Modifier.weight(1f).clip(CementControlShape)
                        .background(if (selected) CementSelection else Color.Transparent)
                        .selectable(selected = selected, enabled = enabled, role = Role.RadioButton) {
                            if (!selected) onSelect(staff)
                        }
                        .padding(horizontal = 4.dp, vertical = 14.dp),
                    color = if (selected) CementAccent else CementMuted,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private fun statusLabel(status: String): String = when (status) {
    "WAITING" -> "Ожидает"
    "ASSIGNED" -> "Назначена"
    "IN_TRANSIT" -> "В пути"
    "DELIVERED" -> "Доставлена"
    else -> "Неизвестный статус"
}



