plugins {
  id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

dependencies {
    compileOnly(project(":common"))
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
	
	compileOnly("org.jetbrains:annotations:20.1.0")
    compileOnly("com.github.AvarionMC:yaml:1.1.7")
}
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}