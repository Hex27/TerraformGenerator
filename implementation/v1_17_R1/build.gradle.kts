dependencies {
	implementation(files("libs/spigot-1.17.jar"))
    implementation(project(":common"))
    compileOnly("org.spigotmc", "spigot", "1.17-R0.1-SNAPSHOT")
}