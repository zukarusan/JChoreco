plugins {
    java
    id("application")
}

application {
    executableDir = ""
}

group = "com.github.zukarusan"
version = "0.1.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {

    implementation(project(":jchoreco",configuration="shadow"))

    implementation("commons-cli:commons-cli:1.5.0")
    implementation("com.opencsv:opencsv:5.5.2")
    implementation("commons-io:commons-io:2.11.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}