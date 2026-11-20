package com.example.cementflow

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val CementTab.icon: ImageVector
    get() = when (this) {
        CementTab.HOME -> HomeIcon
        CementTab.CALCULATOR -> CalculatorIcon
        CementTab.ORDERS -> OrdersIcon
    }

private val HomeIcon = cementIcon("Home") {
    moveTo(3.5f, 10.5f)
    lineTo(12f, 3.5f)
    lineTo(20.5f, 10.5f)
    verticalLineTo(20f)
    horizontalLineTo(14.5f)
    verticalLineTo(14.5f)
    horizontalLineTo(9.5f)
    verticalLineTo(20f)
    horizontalLineTo(3.5f)
    close()
}

internal val CalculatorIcon = cementIcon("Calculator") {
    moveTo(6f, 3f)
    horizontalLineTo(18f)
    quadTo(20f, 3f, 20f, 5f)
    verticalLineTo(19f)
    quadTo(20f, 21f, 18f, 21f)
    horizontalLineTo(6f)
    quadTo(4f, 21f, 4f, 19f)
    verticalLineTo(5f)
    quadTo(4f, 3f, 6f, 3f)
    close()
    moveTo(8f, 8f)
    horizontalLineTo(16f)
    moveTo(8f, 12.5f)
    horizontalLineTo(9f)
    moveTo(15f, 12.5f)
    horizontalLineTo(16f)
    moveTo(8f, 16.5f)
    horizontalLineTo(9f)
    moveTo(15f, 16.5f)
    horizontalLineTo(16f)
}

internal val OrdersIcon = cementIcon("Orders") {
    moveTo(4f, 7f)
    lineTo(7f, 3.5f)
    horizontalLineTo(17f)
    lineTo(20f, 7f)
    verticalLineTo(18.5f)
    quadTo(20f, 20.5f, 18f, 20.5f)
    horizontalLineTo(6f)
    quadTo(4f, 20.5f, 4f, 18.5f)
    close()
    moveTo(4f, 7f)
    horizontalLineTo(20f)
    moveTo(9f, 12f)
    quadTo(9f, 15f, 12f, 15f)
    quadTo(15f, 15f, 15f, 12f)
}

internal val DeliveryIcon = cementIcon("Delivery") {
    moveTo(2.5f, 16.5f)
    verticalLineTo(5.5f)
    horizontalLineTo(14.5f)
    verticalLineTo(16.5f)
    horizontalLineTo(9f)
    moveTo(2.5f, 16.5f)
    horizontalLineTo(5f)
    moveTo(14.5f, 9f)
    horizontalLineTo(18.5f)
    lineTo(21.5f, 12.5f)
    verticalLineTo(16.5f)
    horizontalLineTo(20f)
    moveTo(14.5f, 16.5f)
    horizontalLineTo(16f)
    moveTo(9f, 17f)
    arcTo(2f, 2f, 0f, true, true, 5f, 17f)
    arcTo(2f, 2f, 0f, true, true, 9f, 17f)
    close()
    moveTo(20f, 17f)
    arcTo(2f, 2f, 0f, true, true, 16f, 17f)
    arcTo(2f, 2f, 0f, true, true, 20f, 17f)
    close()
}

private fun cementIcon(name: String, path: PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        .path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.75f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = path,
        )
        .build()
