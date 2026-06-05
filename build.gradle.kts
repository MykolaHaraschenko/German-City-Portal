plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "4.0.6"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "2.2.21"
}

group = "com.deutrust"
version = "0.0.1-SNAPSHOT"
description = "DeuTrust"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("com.h2database:h2")
	runtimeOnly("org.xerial:sqlite-jdbc:3.46.0.0")
	implementation("org.hibernate.orm:hibernate-community-dialects:6.6.1.Final")

	// Swagger / OpenAPI documentation
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")

	// EUDI Reference Libraries
	implementation("eu.europa.ec.eudi:eudi-lib-jvm-sdjwt-kt:0.20.1")
	implementation("eu.europa.ec.eudi:eudi-lib-jvm-openid4vci-kt:0.11.0")
	implementation("eu.europa.ec.eudi:eudi-lib-jvm-presentation-exchange-kt:0.5.0")
	implementation("com.nimbusds:nimbus-jose-jwt:9.48")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
