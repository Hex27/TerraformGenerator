plugins {
    java
}

subprojects {
    apply<JavaPlugin>()

    group = "org.terraform"
    repositories {
        mavenCentral()
		//For SpecialSource
		maven("https://hub.spigotmc.org/nexus/content/repositories/public/")
        maven("https://repo.codemc.io/repository/nms/")
		maven("https://repo.papermc.io/repository/maven-public/")
		maven("https://jitpack.io")
    }
}