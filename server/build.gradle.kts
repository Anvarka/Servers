plugins {
    application
}

configure<JavaApplication> {
    mainClass.set("ru.itmo.java.server.Server")
}

dependencies {
    implementation(project(":common"))
    implementation("com.github.yannrichet:JMathPlot:1.0.1")
}
