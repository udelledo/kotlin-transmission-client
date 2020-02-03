import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.udelledo"
version = "1.0-SNAPSHOT"

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
plugins {
    kotlin("jvm") version "1.3.61"
    maven
    jacoco
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
            includes = listOf("org.transmission.*")

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
