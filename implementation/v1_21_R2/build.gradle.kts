dependencies {
    implementation(project(":common"))
	compileOnly(group = "org.spigotmc", name = "spigot", version = "1.21.2-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:20.1.0")
    compileOnly("com.github.AvarionMC:yaml:1.1.3")
}
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}