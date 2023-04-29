package com.backup7

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableAsync
class Application

fun main(args: Array<String>) {
    System.setProperty("java.awt.headless", "false")
    runApplication<Application>(*args)
}

@Configuration
class AppConfig {
    @EventListener(ApplicationReadyEvent::class)
    fun doAfterStartup() {
        uiStartup()
    }
}
