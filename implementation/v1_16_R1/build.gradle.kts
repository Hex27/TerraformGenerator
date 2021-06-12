group = "org.terraform"

dependencies {
    implementation(project(":common"))
    compileOnly(group = "org.spigotmc", name = "spigot", version = "1.16.1-R0.1-SNAPSHOT")
}
