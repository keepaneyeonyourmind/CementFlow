package ru.cementflow.server

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.*
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.math.BigDecimal

class CreateOrderRequest {
    var productId: Long? = null
    var quantity: BigDecimal? = null
    var customerName: String? = null
    var phone: String? = null
    var address: String? = null
}
class AssignRequest { var driverName: String? = null }
class StatusRequest { var status: String? = null }
data class ErrorResponse(val message: String)

@RestController
@RequestMapping("/api")
class OrderController(private val orders: OrderService) {
    @GetMapping("/products")
    fun products(): List<Product> = orders.products()

    @GetMapping("/orders")
    fun orders(): List<OrderView> = orders.orders()

    @GetMapping("/orders/{id}")
    fun order(@PathVariable("id") id: Long): OrderView = orders.get(id)

    @PostMapping("/orders")
    fun create(@RequestBody request: CreateOrderRequest): ResponseEntity<OrderView> =
        ResponseEntity.status(HttpStatus.CREATED).body(orders.create(OrderRules.validate(request.productId, request.quantity, request.customerName, request.phone, request.address)))

    @PostMapping("/orders/{id}/assign")
    fun assign(@PathVariable("id") id: Long, @RequestBody request: AssignRequest): OrderView = orders.assign(id, request.driverName)

    @PostMapping("/orders/{id}/status")
    fun status(@PathVariable("id") id: Long, @RequestBody request: StatusRequest): OrderView = orders.changeStatus(id, request.status)
}

@RestControllerAdvice
class ApiErrors {
    @ExceptionHandler(BadRequest::class)
    fun badRequest(e: BadRequest): ResponseEntity<ErrorResponse> = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(e.message ?: "Некорректные данные"))
    @ExceptionHandler(NotFound::class)
    fun notFound(e: NotFound): ResponseEntity<ErrorResponse> = ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse(e.message ?: "Не найдено"))
    @ExceptionHandler(Conflict::class)
    fun conflict(e: Conflict): ResponseEntity<ErrorResponse> = ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse(e.message ?: "Недопустимое действие"))
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun invalidParameter(): ResponseEntity<ErrorResponse> = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse("Некорректный параметр запроса"))
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun invalidJson(): ResponseEntity<ErrorResponse> = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse("Некорректный формат запроса"))
}


