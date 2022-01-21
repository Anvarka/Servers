buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.17")
    }
}

rootProject.name = "java-2-2021"

include("common", "main", "server")
include("client")
include("ServerStarter")
include("serverStarter")
include("starter")
