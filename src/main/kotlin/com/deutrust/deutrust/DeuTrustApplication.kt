package com.deutrust.deutrust

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class DeuTrustApplication

fun main(args: Array<String>) {
	runApplication<DeuTrustApplication>(*args)
}
