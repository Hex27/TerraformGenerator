group = "org.terraform"

dependencies {
    compileOnly(group = "org.spigotmc", name = "spigot", version = "1.18-R0.1-SNAPSHOT")
	// compileOnly(group = "org.spigotmc", name = "spigot", version = "1.16.4-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:20.1.0")
    compileOnly("com.github.AvarionMC:yaml:1.1.7")
}

// Set this to the lowest compat in implementation
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}