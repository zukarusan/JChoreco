plugins {
    java
    id("application")
}

application {
    executableDir = ""
}

group = "com.github.zukarusan"
version = "0.9.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    implementation(project(":jchoreco"))
    implementation("com.github.axet:TarsosDSP:2.4")

    implementation("commons-cli:commons-cli:1.5.0")
    implementation("com.opencsv:opencsv:5.5.2")
    implementation("commons-io:commons-io:2.11.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}