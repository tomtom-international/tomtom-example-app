/*
 * © 2025 TomTom NV. All rights reserved.
 *
 * This software is the proprietary copyright of TomTom NV and its subsidiaries and may be
 * used for internal evaluation purposes or commercial use strictly subject to separate
 * license agreement between you and TomTom NV. If you are the licensee, you are only permitted
 * to use this software in accordance with the terms of your license agreement. If you are
 * not the licensee, you are not authorized to use this software in any manner and should
 * immediately return or destroy it.
 */

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    val repositoriesTomtomComUsername: String by extra
    val repositoriesTomtomComPassword: String by extra

    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        mavenLocal()

        maven {
            credentials {
                username = repositoriesTomtomComUsername
                password = repositoriesTomtomComPassword
            }
            url = uri("https://repositories.tomtom.com/artifactory/maven")
        }

        maven {
            credentials {
                username = repositoriesTomtomComUsername
                password = repositoriesTomtomComPassword
            }
            url = uri("https://repositories.tomtom.com/artifactory/sdk-maven-pre-release")
        }
    }

}

rootProject.name = "Navigation Example"
include(":app")
