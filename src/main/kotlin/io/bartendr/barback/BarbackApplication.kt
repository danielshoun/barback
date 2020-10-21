package io.bartendr.barback

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class BarbackApplication

fun main(args: Array<String>) {
    runApplication<BarbackApplication>(*args)
}
