package com.example.cementflow.orders

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private val background = Color(0xFFEEF0F1)
private val ink = Color(0xFF454545)
private val accent = Color(0xFF96171A)
private val screenBackground = Brush.verticalGradient(listOf(Color(0xFFE9EAED), background, Color(0xFFF8F5F5)))

/** Демонстрация заказов и доставки без авторизации; это не персональные данные. */
@Composable
fun OrdersDeliveryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var allowed by remember { mutableStateOf(Build.VERSION.SDK_INT < 37 || context.checkSelfPermission(Manifest.permission.ACCESS_LOCAL_NETWORK) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> allowed = granted }
    if (allowed) {
        OrdersDeliveryContent(onBack)
    } else {
        Column(Modifier.fillMaxSize().background(screenBackground).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TextButton(onClick = onBack) { Text("← Назад") }
            Text("Доступ к учебному серверу", style = MaterialTheme.typography.headlineSmall, color = ink)
            Text("Чтобы открыть заказы и доставку, разрешите приложению подключаться к локальной сети. Данные используются только для учебной демонстрации.", color = ink)
            Button(onClick = { launcher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK) }) { Text("Разрешить доступ") }
        }
    }
}

@Composable
private fun OrdersDeliveryContent(onBack: () -> Unit) {
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

    Column(
        modifier = Modifier.fillMaxSize().background(screenBackground).verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack, enabled = !busy) { Text("← Назад", color = accent) }
        Text("Заказы и доставка", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = ink)
        Text("Только учебные данные. Авторизации нет: обе роли видят общий список заказов.", style = MaterialTheme.typography.bodyMedium, color = ink)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { isStaff = false; showForm = false; notice = null }, enabled = !busy && isStaff) { Text("Клиент") }
            OutlinedButton(onClick = { isStaff = true; showForm = false; notice = null }, enabled = !busy && !isStaff) { Text("Сотрудник") }
        }
        Text(if (isStaff) "Режим сотрудника" else "Режим клиента", color = ink, style = MaterialTheme.typography.titleMedium)
        if (busy) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator()
                Text("Загрузка…", color = ink)
            }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
        notice?.let { Text(it, color = accent, style = MaterialTheme.typography.bodyMedium) }

        if (!isStaff && showForm) {
            Text("Учебный каталог", style = MaterialTheme.typography.titleLarge, color = ink)
            if (productsLoaded && products.isEmpty()) Text("В каталоге пока нет товаров.", color = ink)
            products.forEach { item ->
                val selectionColor by animateColorAsState(
                    targetValue = if (selectedProduct == item.id) Color.White.copy(alpha = 0.86f) else Color.White.copy(alpha = 0.48f),
                    animationSpec = tween(150),
                    label = "product-selection",
                )
                Row(
                    Modifier.fillMaxWidth()
                        .background(selectionColor, RoundedCornerShape(20.dp))
                        .selectable(selected = selectedProduct == item.id, role = Role.RadioButton, enabled = !busy) { selectedProduct = item.id }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selectedProduct == item.id, onClick = null, enabled = !busy)
                    Text("${item.name} · ${item.price} ₽ / ${item.unit}", color = ink)
                }
            }
            OutlinedTextField(quantity, { quantity = it }, label = { Text("Количество") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), enabled = !busy)
            OutlinedTextField(customer, { customer = it }, label = { Text("Имя (учебное)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), enabled = !busy)
            OutlinedTextField(phone, { phone = it }, label = { Text("Телефон (учебный)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), enabled = !busy)
            OutlinedTextField(address, { address = it }, label = { Text("Адрес (учебный)") }, modifier = Modifier.fillMaxWidth(), enabled = !busy)
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
            }, modifier = Modifier.fillMaxWidth()) { Text("Оформить учебный заказ") }
            OutlinedButton(enabled = !busy, onClick = { showForm = false; error = null }, modifier = Modifier.fillMaxWidth()) { Text("К списку заказов") }
            OutlinedButton(enabled = !busy, onClick = { refreshProducts() }, modifier = Modifier.fillMaxWidth()) { Text("Обновить каталог") }
        } else {
            if (!isStaff) Button(enabled = !busy, onClick = { showForm = true; notice = null; error = null }, modifier = Modifier.fillMaxWidth()) { Text("Новый учебный заказ") }
            OutlinedButton(enabled = !busy, onClick = { refreshOrders() }, modifier = Modifier.fillMaxWidth()) { Text("Обновить заказы") }
            if (ordersLoaded && orders.isEmpty()) Text("Заказов пока нет.", color = ink)
            orders.forEach { order ->
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.76f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.9f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Заказ №${order.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ink)
                        Text("${order.productName}: ${order.quantity} ${order.unit}", color = ink)
                        Text("Учебная сумма: ${order.total} ₽", color = ink)
                        Text("Доставка: ${statusLabel(order.status)}", color = ink)
                        Text("Клиент: ${order.customerName}\nТелефон: ${order.phone}\nАдрес: ${order.address}", color = ink)
                        order.driverName?.let { Text("Водитель: $it", color = ink) }
                        if (isStaff && order.status == "WAITING") {
                            val driver = driverNames[order.id].orEmpty()
                            OutlinedTextField(driver, { driverNames = driverNames + (order.id to it) }, label = { Text("Имя водителя (учебное)") }, singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth())
                            Button(enabled = !busy, onClick = {
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
                            Button(enabled = !busy, onClick = {
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
        Spacer(Modifier.height(12.dp))
    }
}

private fun statusLabel(status: String): String = when (status) {
    "WAITING" -> "Ожидает"
    "ASSIGNED" -> "Назначена"
    "IN_TRANSIT" -> "В пути"
    "DELIVERED" -> "Доставлена"
    else -> "Неизвестный статус"
}



