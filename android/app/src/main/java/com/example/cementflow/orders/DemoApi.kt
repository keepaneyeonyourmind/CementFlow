package com.example.cementflow.orders

import com.example.cementflow.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.HttpURLConnection
import java.net.URL

data class DemoProduct(val id: Long, val name: String, val unit: String, val price: String)
data class DemoOrder(
    val id: Long, val productName: String, val quantity: Int, val unit: String,
    val total: String, val customerName: String, val phone: String, val address: String,
    val status: String, val driverName: String?,
)

/** Клиент локального учебного сервера. */
class DemoApi(private val baseUrl: String = BuildConfig.DEMO_API_URL) {
    fun products(): List<DemoProduct> = JSONArray(request("GET", "/api/products")).let { array ->
        (0 until array.length()).map { index ->
            val json = array.getJSONObject(index)
            DemoProduct(json.getLong("id"), json.getString("name"), json.getString("unit"), formatMoney(json.getString("price")))
        }
    }

    fun orders(): List<DemoOrder> = JSONArray(request("GET", "/api/orders")).let { array ->
        (0 until array.length()).map { index -> parseOrder(array.getJSONObject(index)) }
    }

    fun createOrder(productId: Long, quantity: Int, name: String, phone: String, address: String): DemoOrder {
        val body = JSONObject().put("productId", productId).put("quantity", quantity)
            .put("customerName", name.trim()).put("phone", phone.trim()).put("address", address.trim())
        return parseOrder(JSONObject(request("POST", "/api/orders", body)))
    }

    fun assign(id: Long, driverName: String): DemoOrder = parseOrder(JSONObject(request(
        "POST", "/api/orders/$id/assign", JSONObject().put("driverName", driverName.trim()),
    )))

    fun advance(id: Long, status: String): DemoOrder = parseOrder(JSONObject(request(
        "POST", "/api/orders/$id/status", JSONObject().put("status", status),
    )))

    private fun formatMoney(value: String): String = BigDecimal(value).setScale(2, RoundingMode.HALF_UP).toPlainString()

    private fun parseOrder(json: JSONObject) = DemoOrder(
        id = json.getLong("id"), productName = json.getString("productName"),
        quantity = json.getInt("quantity"), unit = json.getString("unit"), total = formatMoney(json.getString("total")),
        customerName = json.getString("customerName"), phone = json.getString("phone"),
        address = json.getString("address"), status = json.getString("status"),
        driverName = if (json.isNull("driverName")) null else json.getString("driverName"),
    )

    private fun request(method: String, path: String, body: JSONObject? = null): String {
        val connection = (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 5000
            readTimeout = 5000
            setRequestProperty("Accept", "application/json")
        }
        try {
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            val status = connection.responseCode
            val response = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val detail = runCatching { JSONObject(response).optString("message") }.getOrNull().orEmpty()
                throw ApiException(detail.ifBlank { "Сервер вернул ошибку ($status)." }, uncertain = status >= 500)
            }
            return response
        } catch (e: ApiException) {
            throw e
        } catch (e: IOException) {
            throw ApiException("Нет связи с учебным сервером. Проверьте запуск сервера и подключение устройства.", e, uncertain = true)
        } finally {
            connection.disconnect()
        }
    }
}

class ApiException(message: String, cause: Throwable? = null, val uncertain: Boolean = false) : IOException(message, cause)




