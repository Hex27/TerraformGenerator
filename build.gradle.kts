plugins {
    java
}

subprojects {
    apply<JavaPlugin>()

    group = "org.terraform"
    repositories {
        // mavenLocal()
        mavenCentral()
        maven("https://repo.codemc.io/repository/nms/")
		// maven("https://libraries.minecraft.net/minecraft-server")
		maven("https://repo.papermc.io/repository/maven-public/")
		maven("https://jitpack.io")
    }
//    Handle this inside each implementation as different minecraft versions support a different max jvm version
//    java {
//        sourceCompatibility = JavaVersion.VERSION_16
//        targetCompatibility = JavaVersion.VERSION_16
//    }
}