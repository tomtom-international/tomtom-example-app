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

package com.example.application.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.application.common.extension.getSpeedData
import com.example.application.ui.theme.NavSdkExampleTheme
import com.tomtom.sdk.navigation.locationcontext.LocationContext
import kotlinx.coroutines.flow.StateFlow

@Composable
fun Speedometer(
    locationContextFlow: StateFlow<LocationContext?>,
    isDeviceInLandscape: Boolean,
    modifier: Modifier = Modifier,
) {
    val locationContext by locationContextFlow.collectAsStateWithLifecycle()
    val speedData = locationContext.getSpeedData()
    val speedUnits = speedData.isMph?.let {
        stringResource(if (it) R.string.common_speed_unit_mph else R.string.common_speed_unit_kph)
    } ?: ""

    if (isDeviceInLandscape) {
        HorizontalSpeedometer(speedData.speed, speedData.speedLimit, speedUnits, modifier)
    } else {
        VerticalSpeedometer(speedData.speed, speedData.speedLimit, speedUnits, modifier)
    }
}

@Composable
private fun VerticalSpeedometer(
    speed: String,
    speedLimit: String,
    speedUnits: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
            )
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = CircleShape,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .border(
                    width = 4.dp,
                    color = Color.Red,
                    shape = CircleShape,
                )
                .background(
                    color = Color.White,
                    shape = CircleShape,
                )
                .width(56.dp)
                .height(56.dp),
        ) {
            Text(
                text = speedLimit,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = speed,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
        )

        Text(
            text = speedUnits,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
}

@Composable
private fun HorizontalSpeedometer(
    speed: String,
    speedLimit: String,
    speedUnits: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
            )
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = CircleShape,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .border(
                    width = 4.dp,
                    color = Color.Red,
                    shape = CircleShape,
                )
                .background(
                    color = Color.White,
                    shape = CircleShape,
                )
                .width(56.dp)
                .height(56.dp),
        ) {
            Text(
                text = speedLimit,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(
                start = 8.dp,
                end = 24.dp,
            ),
        ) {
            Text(
                text = speed,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            )
            Text(
                text = speedUnits,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun VerticalSpeedometerPreview() {
    NavSdkExampleTheme {
        VerticalSpeedometer("123", "120", "km/h")
    }
}

@PreviewLightDark
@Composable
private fun HorizontalSpeedometerPreview() {
    NavSdkExampleTheme {
        HorizontalSpeedometer("123", "120", "km/h")
    }
}
