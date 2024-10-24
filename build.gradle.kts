import java.net.URI
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "2.0.0"
	kotlin("plugin.jpa") version "2.0.0"
	kotlin("plugin.spring") version "2.0.0"
	kotlin("kapt") version "1.8.22"
	id("org.springframework.boot") version "3.3.1"
	id("io.spring.dependency-management") version "1.1.5"
}

allprojects {
	group = "com.sound-bind"
	version = "0.0.1"

	repositories {
		mavenCentral()
	}
}

subprojects {
	apply(plugin = "kotlin")
	apply(plugin = "kotlin-spring")
	apply(plugin = "kotlin-allopen")
	apply(plugin = "kotlin-noarg")
	apply(plugin = "kotlin-kapt")
	apply(plugin = "org.springframework.boot")
	apply(plugin = "io.spring.dependency-management")

	extra["springCloudVersion"] = "2023.0.2"

	repositories {
		maven {
			url = URI.create("https://plugins.gradle.org/m2/")
			url = URI.create("https://artifactory-oss.prod.netflix.net/artifactory/maven-oss-candidates")
		}
	}

	dependencyManagement {
		imports {
			mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.1")
			mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
		}
	}

	dependencies {
		implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
		implementation("org.springframework.boot:spring-boot-starter")
		implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
		implementation("org.danilopianini:khttp:1.3.1")
		implementation("org.springframework.boot:spring-boot-starter-log4j2")
		compileOnly("org.projectlombok:lombok")
		developmentOnly("org.springframework.boot:spring-boot-devtools")
		annotationProcessor("org.projectlombok:lombok")
		testImplementation("org.springframework.boot:spring-boot-starter-test")
		testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	}

	configurations.forEach {
		it.exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
		it.exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
	}

	tasks.withType<KotlinCompile> {
		kotlinOptions {
			jvmTarget = "21"
			freeCompilerArgs+="-Xjsr305=strict"
		}
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}

	allOpen {
		annotation("jakarta.persistence.Entity")
		annotation("jakarta.persistence.MappedSuperclass")
		annotation("jakarta.persistence.Embeddable")
		annotation("org.springframework.data.elasticsearch.annotations.Document")
	}

	noArg {
		annotation("jakarta.persistence.Entity")
		annotation("jakarta.persistence.MappedSuperclass")
		annotation("jakarta.persistence.Embeddable")
		annotation("org.springframework.data.elasticsearch.annotations.Document")
	}
}

project(":global")
project(":eureka-server")
project(":config-server")
project(":kafka-server")
project(":api-gateway")
project(":auth-service")
project(":music-service")
project(":review-service")
project(":notification-service")
project(":pay-service")