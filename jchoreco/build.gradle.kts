plugins {
    java
    id("java-library")
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

    implementation("com.github.axet:TarsosDSP:2.4")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("com.github.yannrichet:JMathPlot:1.0.1")
    implementation("javazoom:jlayer:1.0.1")
    implementation("com.github.wendykierp:JTransforms:3.1")
    implementation("org.tensorflow:tensorflow-core-platform:0.4.0")

    compileOnly ("org.projectlombok:lombok:1.18.22")
    annotationProcessor ("org.projectlombok:lombok:1.18.22")

    testCompileOnly ("org.projectlombok:lombok:1.18.22")
    testAnnotationProcessor ("org.projectlombok:lombok:1.18.22")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}