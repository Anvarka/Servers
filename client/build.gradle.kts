plugins {
    application
}

configure<JavaApplication> {
    mainClass.set("ru.itmo.java.client.Client")
}

dependencies {
    implementation(project(":common"))
}
