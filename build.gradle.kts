import java.net.URI

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

plugins {
	java
	id("org.springframework.boot") version "3.3.1"
	id("io.spring.dependency-management") version "1.1.5"
}

allprojects {
	group = "com.sound-bind"
	version = "0.0.1"

	tasks.withType<JavaCompile> {
		sourceCompatibility = "21"
		targetCompatibility = "21"
	}

	repositories {
		mavenCentral()
	}
}

subprojects {
	apply(plugin = "java")
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
		implementation("org.springframework.boot:spring-boot-starter")
		compileOnly("org.projectlombok:lombok")
		developmentOnly("org.springframework.boot:spring-boot-devtools")
		annotationProcessor("org.projectlombok:lombok")
		testImplementation("org.springframework.boot:spring-boot-starter-test")
		testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	}
}

project(":eureka-server")
