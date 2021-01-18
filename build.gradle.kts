plugins {
    java
}

dependencies {

}

repositories {
    maven{ url = uri("https://repo.codemc.io/repository/nms/") }
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
