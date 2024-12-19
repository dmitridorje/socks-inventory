plugins {
	java
	id("org.springframework.boot") version "2.7.0"
	id("io.spring.dependency-management") version "1.0.15.RELEASE"
	id("jacoco")
}

group = "org.sellsocks"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	/**
	 * Spring Boot Starters
	 */
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")

	/**
	 * Database
	 */
	runtimeOnly("org.postgresql:postgresql")
	implementation("org.liquibase:liquibase-core")

	/**
	 * Lombok
	 */
	annotationProcessor("org.projectlombok:lombok")
	compileOnly("org.projectlombok:lombok")

	/**
	 * Mapping and Processing
	 */
	implementation("org.mapstruct:mapstruct:1.5.3.Final")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.3.Final")
	implementation ("com.opencsv:opencsv:5.8")

	/**
	 * OpenAPI Documentation
	 */
	implementation ("org.springdoc:springdoc-openapi-ui:1.8.0")

	/**
	 * Test dependencies
	 */
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")


	/**
	 * Test containers
	 */
	implementation(platform("org.testcontainers:testcontainers-bom:1.18.0"))
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
	testImplementation("com.redis.testcontainers:testcontainers-redis-junit-jupiter:1.4.6")
}

/**
 * JaCoCo settings
 */
val jacocoInclude = listOf(
	"**/controller/**",
	"**/service/**",
	"**/validation/**",
	"**/mapper/**"
)

jacoco {
	toolVersion = "0.8.12"
}

tasks.test {
	finalizedBy(tasks.jacocoTestReport)
}

tasks.build {
	dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.jacocoTestCoverageVerification {
	mustRunAfter(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)

	reports {
		xml.required.set(false)
		csv.required.set(false)
		html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
	}

	classDirectories.setFrom(
		sourceSets.main.get().output.asFileTree.matching {
			include(jacocoInclude)
		}
	)
}

tasks.jacocoTestCoverageVerification {
	violationRules {
		rule {
			element = "BUNDLE"
			enabled = true

			limit {
				counter = "INSTRUCTION"
				value = "COVEREDRATIO"
				minimum = "0.70".toBigDecimal()
			}
		}
	}

	classDirectories.setFrom(
		sourceSets.main.get().output.asFileTree.matching {
			include(jacocoInclude)
		}
	)
}

tasks.withType<Test> {
	useJUnitPlatform()
}
