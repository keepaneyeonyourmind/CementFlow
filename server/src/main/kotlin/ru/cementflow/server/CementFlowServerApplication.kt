package ru.cementflow.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CementFlowServerApplication

fun main(args: Array<String>) {
    runApplication<CementFlowServerApplication>(*args)
}
