dependencies {
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")
	implementation("org.springframework.cloud:spring-cloud-starter-config")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}