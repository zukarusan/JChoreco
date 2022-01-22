plugins {
    java
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.10"
}

javafx {
    version = "11"
    modules("javafx.controls",  "javafx.fxml")
//    configuration = "compileOnly"
}

group = "com.github.zukarusan"
version = "1.0.0-alpha"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    implementation("com.github.axet:TarsosDSP:2.4")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("com.github.yannrichet:JMathPlot:1.0.1")
    implementation("javazoom:jlayer:1.0.1")
    implementation("com.github.wendykierp:JTransforms:3.1")
    implementation("commons-cli:commons-cli:1.5.0")
    implementation("com.opencsv:opencsv:5.5.2")
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.tensorflow:tensorflow-core-platform:0.3.3")

    compileOnly ("org.projectlombok:lombok:1.18.22")
    annotationProcessor ("org.projectlombok:lombok:1.18.22")

    testCompileOnly ("org.projectlombok:lombok:1.18.22")
    testAnnotationProcessor ("org.projectlombok:lombok:1.18.22")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}