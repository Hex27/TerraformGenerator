dependencies {
    compileOnly(project(":common"))
	compileOnly("org.spigotmc:spigot:26.1-R0.1-SNAPSHOT")
	
	compileOnly("org.jetbrains:annotations:20.1.0")
    compileOnly("com.github.AvarionMC:yaml:1.1.7")
}
java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}