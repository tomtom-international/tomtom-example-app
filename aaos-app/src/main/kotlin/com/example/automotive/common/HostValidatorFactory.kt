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

package com.example.automotive.common

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.car.app.validation.HostValidator
import com.example.automotive.R

/**
 * Factory for creating HostValidator instances with appropriate security configuration.
 * Debug builds allow all hosts for testing; release builds validate specific AAOS system hosts
 * using SHA-256 certificate digests.
 */
object HostValidatorFactory {
    /**
     * Creates a HostValidator based on the application's debug flag.
     *
     * @param context Application context for accessing app info
     * @return HostValidator configured for debug or release mode
     */
    fun create(context: Context): HostValidator {
        val isDebugBuild = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        return if (isDebugBuild) {
            // Development only: Allow all hosts for testing convenience
            HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
        } else {
            // Production: Explicitly allow only verified AAOS hosts
            createProductionValidator(context)
        }
    }

    /**
     * Creates a production HostValidator with explicit certificate digest validation.
     * Includes all standard Google AAOS host packages with their certificate digests.
     * Certificate digests are loaded from string-array resources to ensure ProGuard/R8 compatibility.
     */
    private fun createProductionValidator(context: Context): HostValidator {
        val builder = HostValidator.Builder(context)

        // Android Auto / AAOS Projection Host (com.google.android.projection.gearhead)
        val gearheadHost = context.getString(R.string.com_google_android_projection_gearhead)
        val gearheadDigests = context.resources.getStringArray(R.array.host_gearhead_digests)
        for (digest in gearheadDigests) {
            builder.addAllowedHost(gearheadHost, digest)
        }

        // Google Automotive Templates Host (com.google.android.apps.automotive.templates.host)
        val templatesHost = context.getString(R.string.com_google_android_apps_automotive_templates_host)
        val templatesDigests = context.resources.getStringArray(R.array.host_templates_digests)
        for (digest in templatesDigests) {
            builder.addAllowedHost(templatesHost, digest)
        }

        return builder.build()
    }
}
