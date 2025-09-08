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

package com.example.application.map

import androidx.compose.runtime.Stable
import com.example.application.common.PlaceDetails
import com.example.application.map.MapScreenUiState.Scenario.DESTINATION_ARRIVAL
import com.example.application.map.MapScreenUiState.Scenario.FREE_DRIVING
import com.example.application.map.MapScreenUiState.Scenario.GUIDANCE
import com.example.application.map.MapScreenUiState.Scenario.HOME
import com.example.application.map.MapScreenUiState.Scenario.POI_FOCUS
import com.example.application.map.MapScreenUiState.Scenario.ROUTE_PREVIEW
import com.example.application.search.SearchResultItemContent
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.camera.CameraTrackingMode
import com.tomtom.sdk.map.display.camera.FollowCameraOperatorConfig
import com.tomtom.sdk.navigation.NavigationOptions
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.routing.route.RouteStop
import com.tomtom.sdk.search.common.error.SearchFailure

/**
 * [MapScreen] UI State holder.
 */
@Stable
data class MapScreenUiState(
    val scenario: Scenario,
    val cameraTrackingMode: CameraTrackingMode = CameraTrackingMode.None,
    val zoomToAllMarkers: Boolean = false,
    val isInteractiveMode: Boolean = false,
    val destinationDetails: PlaceDetails? = null,
    val routeStop: RouteStop? = null,
    val placeDetails: PlaceDetails? = null,
    val poiPlaces: List<PlaceDetails> = emptyList(),
    val destinationMarker: GeoPoint? = null,
    val safeAreaTopPadding: Int = 0,
    val safeAreaBottomPadding: Int = 0,
) {
    enum class Scenario {
        HOME,
        POI_FOCUS,
        ROUTE_PREVIEW,
        GUIDANCE,
        FREE_DRIVING,
        DESTINATION_ARRIVAL,
    }

    sealed class ErrorState {
        object SearchError : ErrorState()

        object RoutingError : ErrorState()
    }

    fun isDrivingScenario(): Boolean = scenario == GUIDANCE || scenario == FREE_DRIVING

    fun getRecenterCameraTrackingMode(): CameraTrackingMode = when (scenario) {
        HOME, POI_FOCUS, DESTINATION_ARRIVAL -> CameraTrackingMode.None
        ROUTE_PREVIEW -> CameraTrackingMode.RouteOverview
        GUIDANCE -> CameraTrackingMode.FollowRouteDirection
        FREE_DRIVING ->
            CameraTrackingMode.FollowDirection(FollowCameraOperatorConfig(defaultZoom = FREE_DRIVING_CAMERA_ZOOM))
    }

    fun getRecenterCameraOptions(locationProvider: LocationProvider): CameraOptions? = when (scenario) {
        HOME -> CameraOptions(
            zoom = HOME_CAMERA_ZOOM,
            tilt = DEFAULT_TILT,
            rotation = DEFAULT_ROTATION,
            position = locationProvider.lastKnownLocation?.position,
        )

        POI_FOCUS, DESTINATION_ARRIVAL -> CameraOptions(
            zoom = POI_CAMERA_ZOOM,
            tilt = DEFAULT_TILT,
            position = placeDetails?.place?.coordinate,
        )

        ROUTE_PREVIEW, FREE_DRIVING, GUIDANCE -> null // managed by getRecenterCameraTrackingMode
    }

    companion object {
        const val HOME_CAMERA_ZOOM = 12.0
        const val POI_CAMERA_ZOOM = 14.0
        const val FREE_DRIVING_CAMERA_ZOOM = 16.0
        const val DEFAULT_TILT = 0.0
        const val DEFAULT_ROTATION = 0.0
    }
}

/**
 * Actions that can be dispatched to the [MapScreenViewModel] to update the map screen.
 */
sealed interface MapScreenAction {
    object ClearMap : MapScreenAction

    object ClearSearch : MapScreenAction

    object CloseWaypointPanel : MapScreenAction

    object RecenterMap : MapScreenAction

    object StartInteractiveMode : MapScreenAction

    data class ShowAddWaypointPanel(val geoPoint: GeoPoint) : MapScreenAction

    data class ShowRemoveWaypointPanel(val routeStop: RouteStop) : MapScreenAction

    data class ShowSearchResultFocus(val placeDetails: PlaceDetails) : MapScreenAction

    data class ShowPoiCategorySearchResultFocus(val poiResults: List<SearchResultItemContent>) : MapScreenAction

    data class ShowPoiFocus(val geoPoint: GeoPoint) : MapScreenAction

    data class CleanRoutePreview(val onClearRoutes: () -> Unit) : MapScreenAction

    object ShowRoutePreview : MapScreenAction

    data class ShowRoutingFailure(val routingFailure: RoutingFailure) : MapScreenAction

    data class ShowSearchFailure(val searchFailure: SearchFailure) : MapScreenAction

    data class ToggleBottomSheet(val isExpanded: Boolean?) : MapScreenAction

    data class StartGuidance(
        val navigationOptions: NavigationOptions,
    ) : MapScreenAction

    object StopGuidance : MapScreenAction

    data class ToggleCameraTrackingMode(val checked: Boolean) : MapScreenAction

    data class UpdateRoute(val route: Route, val routePlanningOptions: RoutePlanningOptions) : MapScreenAction
}
