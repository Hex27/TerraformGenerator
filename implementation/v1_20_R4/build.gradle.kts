dependencies {
    implementation(project(":common"))
	compileOnly(group = "org.spigotmc", name = "spigot", version = "1.20.5-R0.1-SNAPSHOT")
}
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}