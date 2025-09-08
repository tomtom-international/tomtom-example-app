/*
Copyright 2026 TomTom International BV.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

// Specify target Artifactory repository (optional Gradle property -PtargetRepo)
val targetRepo: String? by settings
val targetRepoValue = targetRepo?.takeIf { it.isNotBlank() }

if (targetRepoValue != null) {
    println("Building against: $targetRepoValue")
}

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
    val repositoriesTomtomComUsername: String? by extra
    val repositoriesTomtomComPassword: String? by extra

    @Suppress("UnstableApiUsage") // repositoriesMode
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    @Suppress("UnstableApiUsage") // repositories
    repositories {
        google()
        mavenCentral()

        maven {
            if (repositoriesTomtomComUsername?.isNotEmpty() == true) {
                credentials {
                    username = repositoriesTomtomComUsername
                    password = repositoriesTomtomComPassword
                }
            }
            url = uri(targetRepoValue ?: "https://repositories.tomtom.com/artifactory/maven")
        }
    }
}

rootProject.name = "Navigation Example"
include(":aaos-app")
include(":app")
