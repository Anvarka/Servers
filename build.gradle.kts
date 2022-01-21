plugins {
    java
    id("com.google.protobuf") apply false
}

group = "org.itmo.java"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

subprojects {
    apply {
        plugin("java")
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("com.google.protobuf:protobuf-java:3.19.0")
        testImplementation(platform("org.junit:junit-bom:5.8.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    sourceSets {
        main {
            java.srcDir("src")
            resources.srcDir("resources")
        }
        test {
            java.srcDir("test")
            resources.srcDir("testResources")
        }
    }

}

tasks.test {
    useJUnitPlatform()
}
