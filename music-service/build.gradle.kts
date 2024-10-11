dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.cloud:spring-cloud-starter-config")
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
	implementation("org.springframework.kafka:spring-kafka")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// querydsl
	implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
	implementation("com.querydsl:querydsl-apt:5.1.0:jakarta")
	implementation("jakarta.persistence:jakarta.persistence-api")
	implementation("jakarta.annotation:jakarta.annotation-api")
	kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")
	kapt("org.springframework.boot:spring-boot-configuration-processor")

	// redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.redisson:redisson-spring-boot-starter:3.27.0")

	// swagger
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

	// global
	implementation(project(":global"))

	runtimeOnly("com.mysql:mysql-connector-j")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}