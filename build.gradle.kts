buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
//        maven("http://maven.xxx.com/") {
//            // 信任http协议
//            isAllowInsecureProtocol = true
//        }
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
    dependencies {
        classpath("io.objectbox:objectbox-gradle-plugin:3.7.0")
    }
}

plugins {
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}


tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}