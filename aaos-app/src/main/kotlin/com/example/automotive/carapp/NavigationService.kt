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

package com.example.automotive.carapp

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.SessionInfo
import androidx.car.app.validation.HostValidator
import com.example.automotive.common.HostValidatorFactory

/**
 * CarAppService entry point for the AAOS navigation application.
 */
class NavigationService : CarAppService() {
    override fun createHostValidator(): HostValidator {
        return HostValidatorFactory.create(applicationContext)
    }

    override fun onCreateSession(sessionInfo: SessionInfo): Session {
        return CarNavigationSession()
    }
}
