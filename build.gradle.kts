plugins {
    java;
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19" apply false
}

subprojects {
    apply<JavaPlugin>()

    group = "org.terraform"
    repositories {
        mavenCentral()
		
		//For spigot local jars
		mavenLocal()
		
		//For SpecialSource
		maven("https://hub.spigotmc.org/nexus/content/repositories/public/")
        maven("https://repo.codemc.io/repository/nms/")
		maven("https://repo.papermc.io/repository/maven-public/")
		maven("https://jitpack.io")
    }
}