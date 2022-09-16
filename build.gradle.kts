import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val appName = "time_clock"

object Versions {
    const val tornadoFx = "2.0.0-SNAPSHOT"
    const val jvmTarget = "11"
}

plugins {
    // tornadoFx 동작에 문제가 있으면  1.2.60 으로 변경 필요. coroutine version 도 같이 내려야함
    kotlin("jvm") version "1.4.31"
    application
    id("org.openjfx.javafxplugin") version "0.0.9"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

javafx {
    version = "11.0.2"
    modules = listOf("javafx.controls", "javafx.graphics", "javafx.fxml", "javafx.media")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // logger
    implementation("io.github.microutils:kotlin-logging:2.1.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.1")

    // uart
    implementation("com.fazecast:jSerialComm:2.7.0")

    // io
    implementation("com.squareup.okio:okio:3.0.0")
    implementation("commons-io:commons-io:2.11.0")

    // tornadofx
    implementation("no.tornado:tornadofx:${Versions.tornadoFx}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.5.2")
    //implementation("eu.hansolo.enzo:Enzo:0.3.6")
    implementation("com.jfoenix:jfoenix:9.0.10")

    //implementation("no.tornado:tornadofx-controlsfx:0.1")
    //implementation("org.controlsfx:controlsfx:8.40.18")
    //implementation("de.jensd:fontawesomefx:8.9")
    //implementation("org.kordamp.ikonli:ikonli-javafx:2.4.0")
    //implementation("org.kordamp.ikonli:ikonli-fontawesome5-pack:2.4.0")

    // db
    implementation("org.jetbrains.exposed:exposed-core:0.36.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.36.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.36.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.36.1")
    implementation("org.xerial:sqlite-jdbc:3.36.0.2")

    // network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
//    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
//    implementation("com.github.kittinunf.fuel:fuel-coroutines:2.3.1")
//    implementation("com.github.kittinunf.fuel:fuel-kotlinx-serialization:2.3.1")

    implementation("com.github.sarxos:webcam-capture:0.3.12")
    implementation("com.github.sarxos:webcam-capture-driver-fswebcam:0.3.12")
//    implementation("com.github.sarxos:v4l4j:0.9.1-r507")
//    implementation("com.github.sarxos:webcam-capture-driver-v4l4j:0.3.12")

    implementation("org.comtel2000:fx-onscreen-keyboard:11.0.1") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation("org.apache.commons:commons-compress:1.21")
    implementation("org.tukaani:xz:1.9")

    implementation("com.jcraft:jsch:0.1.55")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.jvmTarget
}

tasks {
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "com.techrove.timeclock.Application"))
        }
        exclude(
            "Android/",
            "OSX/",
            "Solaris/",
            "Windows/",
            "*.dll",
            "org/sqlite/native/DragonFlyBSD/",
            "org/sqlite/native/FreeBSD/",
            "org/sqlite/native/Linux/ppc64/",
            "org/sqlite/native/Linux/x86/",
            "org/sqlite/native/Linux/x86_64/",
            "org/sqlite/native/Linux-Alpine/",
            "org/sqlite/native/Mac/",
            "org/sqlite/native/Windows/"
        )
        archiveBaseName.set(appName)
        archiveClassifier.set("")
        archiveVersion.set("")
        doLast {
            copy {
                from(layout.buildDirectory.dir("libs/${appName}.jar"))
                into(layout.projectDirectory.dir("./"))
            }
        }
    }
}

//tasks {
//    build {
//        dependsOn(shadowJar)
//    }
//}

application {
    mainClassName = "com.techrove.timeclock.Application"
    applicationDefaultJvmArgs = listOf("--add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED")
}
