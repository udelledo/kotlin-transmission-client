import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.udelledo"
version = "1.0-SNAPSHOT"


plugins {
    kotlin("jvm") version "1.3.61"
    maven
    jacoco
    id("info.solidsoft.pitest") version "1.4.7"

}
tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
repositories {
    mavenLocal()
    mavenCentral()
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
    implementation("javax.annotation:javax.annotation-api:1.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
    testImplementation("io.mockk:mockk:1.9.3.kotlin12")


}
jacoco {
    toolVersion = "0.8.5"
}
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.5".toBigDecimal()
            }
        }

        rule {
            enabled = false
            element = "CLASS"
            includes = listOf("org.udelledo.transmission.*")

            limit {
                counter = "LINE"
                value = "TOTALCOUNT"
                maximum = "0.3".toBigDecimal()
            }
        }
    }
}
tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
    finalizedBy("jacocoTestReport")
}
pitest {
    targetClasses.set(setOf("org.udelledo.transmission.client.*"))
    targetTests.set(setOf("org.udelledo.transmission.client.unit.*"))
    junit5PluginVersion.set("0.12")
    mutators.set(setOf("CONDITIONALS_BOUNDARY", "VOID_METHOD_CALLS", "NEGATE_CONDITIONALS",
            "INVERT_NEGS", "MATH", "INCREMENTS", "TRUE_RETURNS", "FALSE_RETURNS", "PRIMITIVE_RETURNS", "EMPTY_RETURNS", "NULL_RETURNS"
    ))
}