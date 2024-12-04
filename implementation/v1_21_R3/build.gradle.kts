dependencies {
    implementation(project(":common"))
	//compileOnly(group = "org.spigotmc", name = "spigot", version = "1.21.4-R0.1-SNAPSHOT")
	//Replace this local file stuff with the codemc repo above after they finally release 1.21.4.
    compileOnly(files("../../libs/spigot-1.21.4-R0.1-SNAPSHOT.jar", "../../libs/spigot-api-1.21.4-R0.1-SNAPSHOT.jar","../../libs/datafixerupper-8.0.16.jar","../../libs/fastutil-8.5.15.jar","../../libs/brigadier-1.3.10.jar"))
	compileOnly("org.jetbrains:annotations:20.1.0")
    compileOnly("com.github.AvarionMC:yaml:1.1.7")
}
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}