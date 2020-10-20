package io.bartendr.barback

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BarbackApplication

fun main(args: Array<String>) {
    runApplication<BarbackApplication>(*args)
}
