plugins {
    id("java-library")
    id("maven-publish")
    id("io.spring.dependency-management") version "1.1.0"
}

group = "malibu.request-mapper"
version = "3.0"
java.sourceCompatibility = JavaVersion.VERSION_17

java {
    withSourcesJar()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.slf4j:slf4j-api")
    api("jakarta.validation:jakarta.validation-api")
    api("org.apache.commons:commons-lang3")
    api("org.springframework:spring-core")

    compileOnly("org.projectlombok:lombok")
    compileOnly("io.projectreactor:reactor-core")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-core")
    testImplementation("io.projectreactor:reactor-core")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")

    annotationProcessor("org.projectlombok:lombok")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.0.5")
    }
}

tasks["publish"].dependsOn(tasks["build"])
tasks["publishToMavenLocal"].dependsOn(tasks["build"])

publishing {
    repositories {
        maven {
            name = "kiccNexusRepo"
            credentials {
                username = System.getenv("username")
                password = System.getenv("password")
            }

            val releaseRepoUrl = uri("")
            val snapshotsRepoUrl = uri("")
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releaseRepoUrl)
            isAllowInsecureProtocol = true
        }

//        maven {
//            name = "GithubPakcages"
//            credentials {
//                username = System.getenv("GP_USERNAME")
//                password = System.getenv("GP_TOKEN")
//            }
//            url = uri("")
//        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = group.toString()
            artifactId = project.name
            version = version

            from(components["java"])
            versionMapping {
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
        }
    }
}