dependencies {
    implementation(project(":common"))
    compileOnly(group = "org.spigotmc", name = "spigot", version = "1.16.3-R0.1-SNAPSHOT")
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}