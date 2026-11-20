package ru.cementflow.server

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.sql.ResultSet

data class Product(val id: Long, val name: String, val unit: String, val price: BigDecimal)
data class OrderView(val id: Long, val productId: Long, val productName: String, val unit: String, val quantity: Int,
    val unitPrice: BigDecimal, val total: BigDecimal, val customerName: String, val phone: String, val address: String,
    val status: String, val driverName: String?)

@Service
class OrderService(private val jdbc: JdbcTemplate) {
    fun products(): List<Product> = jdbc.query("SELECT id, name, unit, price FROM products ORDER BY id") { rs, _ -> product(rs) }
    fun orders(): List<OrderView> = jdbc.query("SELECT * FROM orders ORDER BY id DESC") { rs, _ -> order(rs) }
    fun get(id: Long): OrderView = find(id, false)

    @Transactional
    fun create(details: ValidOrder): OrderView {
        val product = jdbc.query("SELECT id, name, unit, price FROM products WHERE id = ?", { rs, _ -> product(rs) }, details.productId)
            .firstOrNull() ?: throw NotFound("Товар не найден")
        val id = jdbc.queryForObject(
            """INSERT INTO orders (product_id, product_name, unit, quantity, unit_price, total, customer_name, phone, address)
               VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id""",
            Long::class.java, product.id, product.name, product.unit, details.quantity, product.price,
            OrderRules.total(product.price, details.quantity), details.customerName, details.phone, details.address
        ) ?: error("Заказ не создан")
        return find(id, false)
    }

    @Transactional
    fun assign(id: Long, name: String?): OrderView {
        val driver = DeliveryRules.driver(name)
        val existing = find(id, true)
        if (existing.status != DeliveryStatus.WAITING.name) throw Conflict("Доставка уже назначена или завершена")
        jdbc.update("UPDATE orders SET status = ?, driver_name = ? WHERE id = ?", DeliveryStatus.ASSIGNED.name, driver, id)
        return find(id, false)
    }

    @Transactional
    fun changeStatus(id: Long, requested: String?): OrderView {
        val target = try { DeliveryStatus.valueOf(requested ?: "") } catch (_: IllegalArgumentException) { throw BadRequest("Неизвестный статус доставки") }
        val existing = find(id, true)
        val next = DeliveryRules.next(DeliveryStatus.valueOf(existing.status), target)
        jdbc.update("UPDATE orders SET status = ? WHERE id = ?", next.name, id)
        return find(id, false)
    }

    private fun find(id: Long, lock: Boolean): OrderView =
        jdbc.query("SELECT * FROM orders WHERE id = ?" + if (lock) " FOR UPDATE" else "", { rs, _ -> order(rs) }, id)
            .firstOrNull() ?: throw NotFound("Заказ не найден")

    private fun product(rs: ResultSet) = Product(rs.getLong("id"), rs.getString("name"), rs.getString("unit"), rs.getBigDecimal("price"))
    private fun order(rs: ResultSet) = OrderView(rs.getLong("id"), rs.getLong("product_id"), rs.getString("product_name"),
        rs.getString("unit"), rs.getInt("quantity"), rs.getBigDecimal("unit_price"), rs.getBigDecimal("total"),
        rs.getString("customer_name"), rs.getString("phone"), rs.getString("address"), rs.getString("status"), rs.getString("driver_name"))
}
