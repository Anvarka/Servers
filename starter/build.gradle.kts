plugins {
    java
}

group = "org.itmo.java"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testCompile("junit", "junit", "4.12")
    implementation(project(":server"))
    implementation(project(":client"))
    implementation(project(":common"))
}
