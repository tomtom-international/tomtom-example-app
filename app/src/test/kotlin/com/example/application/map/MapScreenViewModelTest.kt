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

import android.util.Log
import com.example.application.common.PlaceDetails
import com.example.application.freedriving.FreeDrivingManager
import com.example.application.map.MapScreenAction.CleanRoutePreview
import com.example.application.map.MapScreenAction.ClearMap
import com.example.application.map.MapScreenAction.CloseWaypointPanel
import com.example.application.map.MapScreenAction.RecenterMap
import com.example.application.map.MapScreenAction.ShowAddWaypointPanel
import com.example.application.map.MapScreenAction.ShowPoiCategorySearchResultFocus
import com.example.application.map.MapScreenAction.ShowPoiFocus
import com.example.application.map.MapScreenAction.ShowRemoveWaypointPanel
import com.example.application.map.MapScreenAction.ShowRoutePreview
import com.example.application.map.MapScreenAction.ShowSearchResultFocus
import com.example.application.map.MapScreenAction.StartGuidance
import com.example.application.map.MapScreenAction.StartInteractiveMode
import com.example.application.map.MapScreenAction.StopGuidance
import com.example.application.map.MapScreenAction.UpdateRoute
import com.example.application.map.MapScreenUiState.Scenario.FREE_DRIVING
import com.example.application.map.MapScreenUiState.Scenario.GUIDANCE
import com.example.application.map.MapScreenUiState.Scenario.HOME
import com.example.application.map.MapScreenUiState.Scenario.POI_FOCUS
import com.example.application.map.MapScreenUiState.Scenario.ROUTE_PREVIEW
import com.example.application.search.SearchResultItemContent
import com.example.application.settings.data.SettingsRepository
import com.example.application.tts.TextToSpeechEngine
import com.tomtom.sdk.common.Cancellable
import com.tomtom.sdk.location.BetaLocationApi
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.map.display.camera.CameraTrackingMode
import com.tomtom.sdk.map.display.mapdatastore.common.MapDataStore
import com.tomtom.sdk.navigation.NavigationOptions
import com.tomtom.sdk.navigation.RoutePlan
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.routing.route.RouteStop
import com.tomtom.sdk.search.common.error.SearchFailure
import com.tomtom.sdk.search.reversegeocoder.ReverseGeocoder
import com.tomtom.sdk.search.reversegeocoder.ReverseGeocoderCallback
import com.tomtom.sdk.search.reversegeocoder.ReverseGeocoderResponse
import com.tomtom.sdk.search.reversegeocoder.model.location.PlaceMatch
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapScreenViewModelTest {
    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    private lateinit var defaultLocationProvider: LocationProvider

    @RelaxedMockK
    private lateinit var mapMatchedLocationProvider: LocationProvider

    @MockK
    private lateinit var reverseGeocoder: ReverseGeocoder

    @RelaxedMockK
    private lateinit var navigation: TomTomNavigation

    @RelaxedMockK
    private lateinit var freeDrivingManager: FreeDrivingManager

    @RelaxedMockK
    private lateinit var mapDataStore: MapDataStore

    @RelaxedMockK
    private lateinit var settingsRepository: SettingsRepository

    @RelaxedMockK
    private lateinit var onClearMap: () -> Unit

    @RelaxedMockK
    private lateinit var onCheckLocationPermission: () -> Boolean

    @RelaxedMockK
    private lateinit var textToSpeechEngine: TextToSpeechEngine

    private val defaultGeoPoint = GeoPoint(0.0, 0.0)

    private lateinit var viewModel: MapScreenViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { onCheckLocationPermission() } returns true

        viewModel = MapScreenViewModel(
            mapDataStore,
            defaultLocationProvider,
            mapMatchedLocationProvider,
            reverseGeocoder,
            navigation,
            freeDrivingManager,
            settingsRepository,
            onClearMap,
            onCheckLocationPermission,
            textToSpeechEngine,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    @Test
    fun `on initialization, verify that MapScreenUiState Type is HOME by default`() {
        assertEquals(HOME, viewModel.mapScreenUiState.value.scenario)
    }

    @Test
    fun `on initialization, when location permission is granted, the defaultLocationProvider is enabled`() {
        verify { defaultLocationProvider.addOnLocationUpdateListener(any()) }
        verify { defaultLocationProvider.enable() }
        verify(exactly = 0) { mapMatchedLocationProvider.enable() }
    }

    @Test
    fun `on initialization, when location permission is denied, the defaultLocationProvider is not enabled`() {
        // 1st initialization with granted permission
        verify(exactly = 1) { defaultLocationProvider.addOnLocationUpdateListener(any()) }
        verify(exactly = 1) { defaultLocationProvider.enable() }

        // 2nd initialization with denied permission
        every { onCheckLocationPermission() } returns false
        viewModel = MapScreenViewModel(
            mapDataStore,
            defaultLocationProvider,
            mapMatchedLocationProvider,
            reverseGeocoder,
            navigation,
            freeDrivingManager,
            settingsRepository,
            onClearMap,
            onCheckLocationPermission,
            textToSpeechEngine,
        )

        // Verify that the defaultLocationProvider is not enabled again
        verify(exactly = 1) { defaultLocationProvider.addOnLocationUpdateListener(any()) }
        verify(exactly = 1) { defaultLocationProvider.enable() }
    }

    @Test
    fun `when onCleared, defaultLocationProvider and mapMatchedLocationProvider are disabled`() {
        viewModel.onCleared()

        verify { defaultLocationProvider.removeOnLocationUpdateListener(any()) }
        verify { defaultLocationProvider.disable() }
        verify { mapMatchedLocationProvider.disable() }
    }

    @Test
    fun `when onCleared, given ActiveGuidance scenario, navigation is stopped and listeners are removed`() {
        viewModel.dispatchAction(StartGuidance(navigationOptions = mockk<NavigationOptions>(relaxed = true)))

        viewModel.onCleared()

        verifyOrder {
            navigation.removeLocationContextUpdatedListener(any())
            navigation.removeProgressUpdatedListener(any())
            navigation.removeGuidanceUpdatedListener(any())
            navigation.removeDestinationArrivalListener(any())
            navigation.removeLaneGuidanceUpdatedListener(any())
            navigation.removeHorizonUpdatedListener(any())
            navigation.stop()
            mapMatchedLocationProvider.disable()
        }
    }

    @Test
    fun `when onCleared, given FreeDriving scenario, navigation is stopped and listeners are removed`() {
        every { freeDrivingManager.getIsDrivingFlow(any()) } returns flowOf(true)

        viewModel.onResume()
        assertEquals(FREE_DRIVING, viewModel.mapScreenUiState.value.scenario)
        viewModel.onCleared()

        verifyOrder {
            navigation.removeLocationContextUpdatedListener(any())
            navigation.stop()
            mapMatchedLocationProvider.disable()
        }
    }

    @Test
    fun `when StartInteractiveMode, isInteractiveMode is set to true and cameraTrackingMode is set to None`() {
        viewModel.dispatchAction(StartInteractiveMode)

        val state = viewModel.mapScreenUiState.value
        assertTrue(state.isInteractiveMode)
        assertEquals(CameraTrackingMode.None, viewModel.mapScreenUiState.value.cameraTrackingMode)
    }

    @Test
    fun `when the recenter button is clicked in HOME scenario, the map is centered on user position`() {
        every { defaultLocationProvider.lastKnownLocation?.position } returns defaultGeoPoint

        viewModel.dispatchAction(RecenterMap)

        val state = viewModel.mapScreenUiState.value
        assertFalse(state.isInteractiveMode)
        assertEquals(defaultGeoPoint, viewModel.cameraOptions.value?.position)
    }

    @Test
    fun `when the recenter button is clicked in POI_FOCUS scenario, the map is centered on poi position`() {
        val firstPlace = mockk<PlaceMatch> { every { place.coordinate } returns defaultGeoPoint }
        val response = mockk<ReverseGeocoderResponse> { every { places } returns listOf(firstPlace) }

        val captureCallback = slot<ReverseGeocoderCallback>()
        every { reverseGeocoder.reverseGeocode(any(), capture(captureCallback)) } answers {
            captureCallback.captured.onSuccess(response)
            Cancellable {}
        }

        viewModel.dispatchAction(ShowPoiFocus(defaultGeoPoint))
        viewModel.dispatchAction(RecenterMap)

        val state = viewModel.mapScreenUiState.value
        assertFalse(state.isInteractiveMode)
        assertEquals(defaultGeoPoint, viewModel.cameraOptions.value?.position)
    }

    @Test
    fun `when the recenter button is clicked in ROUTE_PREVIEW scenario, the map is centered on route overview`() {
        viewModel.dispatchAction(ShowRoutePreview)
        viewModel.dispatchAction(RecenterMap)

        val state = viewModel.mapScreenUiState.value
        assertFalse(state.isInteractiveMode)
        assertEquals(CameraTrackingMode.RouteOverview, state.cameraTrackingMode)
    }

    @Test
    fun `when the recenter button is clicked in GUIDANCE scenario, the map is centered on follow route`() {
        viewModel.dispatchAction(StartGuidance(navigationOptions = mockk(relaxed = true)))
        viewModel.dispatchAction(RecenterMap)

        val state = viewModel.mapScreenUiState.value
        assertFalse(state.isInteractiveMode)
        assertEquals(CameraTrackingMode.FollowRouteDirection, state.cameraTrackingMode)
    }

    @Test
    fun `when startGuidance, navigation calls start method and scenario is set to GUIDANCE`() {
        viewModel.dispatchAction(StartGuidance(navigationOptions = mockk<NavigationOptions>(relaxed = true)))

        verify { navigation.start(any()) }
        assertEquals(GUIDANCE, viewModel.mapScreenUiState.value.scenario)
    }

    @Test
    fun `when startGuidance with simulation disabled, navigation calls start method and scenario is set to GUIDANCE`() {
        viewModel =
            MapScreenViewModel(
                mapDataStore,
                defaultLocationProvider,
                mapMatchedLocationProvider,
                reverseGeocoder,
                navigation,
                freeDrivingManager,
                settingsRepository,
                onClearMap = {},
                onCheckLocationPermission,
                textToSpeechEngine,
            )

        viewModel.dispatchAction(StartGuidance(navigationOptions = mockk()))

        verify { navigation.start(any()) }
        assertEquals(GUIDANCE, viewModel.mapScreenUiState.value.scenario)
        assertEquals(CameraTrackingMode.FollowRouteDirection, viewModel.mapScreenUiState.value.cameraTrackingMode)
    }

    @Test
    fun `when stopGuidance, navigation calls stop method and scenario is set to HOME`() {
        viewModel.dispatchAction(StopGuidance)

        verify { navigation.stop() }
        assertEquals(HOME, viewModel.mapScreenUiState.value.scenario)
    }

    @OptIn(BetaLocationApi::class)
    @Test
    fun `when ShowPoiCategorySearchResultFocus, map is updated with the POIs`() {
        val place = mockk<Place> { every { coordinate } returns defaultGeoPoint }
        val placeDetails = PlaceDetails(place)
        val resultItemContent = SearchResultItemContent(
            iconId = 25,
            placeDetails = placeDetails,
            distance = "25 km",
        )
        val searchResults = listOf(resultItemContent, resultItemContent, resultItemContent)

        viewModel.dispatchAction(ShowPoiCategorySearchResultFocus(searchResults))

        assertEquals(HOME, viewModel.mapScreenUiState.value.scenario)
        assertEquals(searchResults.size, viewModel.mapScreenUiState.value.poiPlaces.size)
        assertEquals(CameraTrackingMode.None, viewModel.mapScreenUiState.value.cameraTrackingMode)
    }

    @OptIn(BetaLocationApi::class)
    @Test
    fun `when ShowSearchResultFocus, map is updated with the POI`() {
        val place = mockk<Place> { every { coordinate } returns defaultGeoPoint }
        val placeDetails = PlaceDetails(place)

        viewModel.dispatchAction(ShowSearchResultFocus(placeDetails))

        assertEquals(POI_FOCUS, viewModel.mapScreenUiState.value.scenario)
        assertEquals(placeDetails, viewModel.mapScreenUiState.value.placeDetails)
        assertEquals(defaultGeoPoint, viewModel.mapScreenUiState.value.destinationMarker)
        assertEquals(CameraTrackingMode.None, viewModel.mapScreenUiState.value.cameraTrackingMode)
    }

    @Test
    fun `when ShowPoiFocus, given getPoiData succeeds, map is updated with the POI`() {
        val firstPlace = mockk<PlaceMatch> { every { place.coordinate } returns defaultGeoPoint }
        val response = mockk<ReverseGeocoderResponse> { every { places } returns listOf(firstPlace) }

        val captureCallback = slot<ReverseGeocoderCallback>()
        every { reverseGeocoder.reverseGeocode(any(), capture(captureCallback)) } answers {
            captureCallback.captured.onSuccess(response)
            Cancellable {}
        }

        viewModel.dispatchAction(ShowPoiFocus(defaultGeoPoint))

        assertEquals(POI_FOCUS, viewModel.mapScreenUiState.value.scenario)
        assertEquals(firstPlace.place, viewModel.mapScreenUiState.value.placeDetails?.place)
        assertEquals(defaultGeoPoint, viewModel.mapScreenUiState.value.destinationMarker)
        assertEquals(CameraTrackingMode.None, viewModel.mapScreenUiState.value.cameraTrackingMode)
    }

    @Test
    fun `when ShowPoiFocus, given getPoiData fails, map is not updated`() {
        val captureCallback = slot<ReverseGeocoderCallback>()
        every { reverseGeocoder.reverseGeocode(any(), capture(captureCallback)) } answers {
            captureCallback.captured.onFailure(SearchFailure.NetworkFailure(""))
            Cancellable {}
        }

        viewModel.dispatchAction(ShowPoiFocus(defaultGeoPoint))

        assertNotEquals(POI_FOCUS, viewModel.mapScreenUiState.value.scenario)
        assertNull(viewModel.mapScreenUiState.value.placeDetails)
        assertNull(viewModel.mapScreenUiState.value.destinationMarker)
    }

    @Test
    fun `when ClearMap, map is cleared`() {
        viewModel.dispatchAction(ClearMap)

        assertEquals(HOME, viewModel.mapScreenUiState.value.scenario)
        assertFalse(viewModel.mapScreenUiState.value.isInteractiveMode)
        assertNull(viewModel.mapScreenUiState.value.destinationMarker)
        assertNull(viewModel.mapScreenUiState.value.routeStop)
        assertNull(viewModel.mapScreenUiState.value.placeDetails)
        assertEquals(CameraTrackingMode.None, viewModel.mapScreenUiState.value.cameraTrackingMode)
        verify(exactly = 1) { onClearMap() }
    }

    @Test
    fun `when ShowRoutePreview, scenario is updated to ROUTE_PREVIEW`() {
        viewModel.dispatchAction(ShowRoutePreview)

        assertEquals(ROUTE_PREVIEW, viewModel.mapScreenUiState.value.scenario)
    }

    @Test
    fun `when onResume and on HOME state and isDriving, scenario is FREE_DRIVING`() = runTest {
        every { freeDrivingManager.getIsDrivingFlow(any()) } returns flowOf(true)

        assertEquals(HOME, viewModel.mapScreenUiState.value.scenario)
        viewModel.onResume()

        assertEquals(FREE_DRIVING, viewModel.mapScreenUiState.value.scenario)
    }

    @Test
    fun `when onResume and on POI_FOCUS state and isDriving, scenario is still POI_FOCUS`() {
        every { freeDrivingManager.getIsDrivingFlow(any()) } returns flowOf(true)

        viewModel.dispatchAction(
            ShowSearchResultFocus(
                mockk<PlaceDetails> {
                    every { place.coordinate } returns defaultGeoPoint
                },
            ),
        )
        assertEquals(POI_FOCUS, viewModel.mapScreenUiState.value.scenario)

        viewModel.onResume()

        assertEquals(POI_FOCUS, viewModel.mapScreenUiState.value.scenario)
    }

    @Test
    fun `when onResume and not isDriving and scenario is FREE_DRIVING, stops navigation and scenario is HOME`() {
        every { freeDrivingManager.getIsDrivingFlow(any()) } returns flowOf(true)
        viewModel.onResume()
        assertEquals(FREE_DRIVING, viewModel.mapScreenUiState.value.scenario)

        every { freeDrivingManager.getIsDrivingFlow(any()) } returns flowOf(false)
        viewModel.onResume()

        assertEquals(HOME, viewModel.mapScreenUiState.value.scenario)
    }

    @Test
    fun `when onResume and not isDriving and scenario is POI_FOCUS, scenario is still POI_FOCUS`() {
        every { freeDrivingManager.getIsDrivingFlow(any()) } returns flowOf(false)

        viewModel.dispatchAction(
            ShowSearchResultFocus(
                mockk<PlaceDetails> {
                    every { place.coordinate } returns defaultGeoPoint
                },
            ),
        )
        assertEquals(POI_FOCUS, viewModel.mapScreenUiState.value.scenario)

        viewModel.onResume()

        assertEquals(POI_FOCUS, viewModel.mapScreenUiState.value.scenario)
    }

    @Test
    fun `when is FREE_DRIVING and call showPoiFocus, scenario is updated to POI_FOCUS and navigation is stopped`() {
        every { freeDrivingManager.getIsDrivingFlow(any()) } returns flowOf(true)

        assertEquals(HOME, viewModel.mapScreenUiState.value.scenario)
        viewModel.onResume()

        assertEquals(FREE_DRIVING, viewModel.mapScreenUiState.value.scenario)

        // call showPoiFocus
        val firstPlace = mockk<PlaceMatch> { every { place.coordinate } returns defaultGeoPoint }
        val response = mockk<ReverseGeocoderResponse> { every { places } returns listOf(firstPlace) }

        val captureCallback = slot<ReverseGeocoderCallback>()
        every { reverseGeocoder.reverseGeocode(any(), capture(captureCallback)) } answers {
            captureCallback.captured.onSuccess(response)
            Cancellable {}
        }

        viewModel.dispatchAction(ShowPoiFocus(defaultGeoPoint))

        // verify state
        assertEquals(POI_FOCUS, viewModel.mapScreenUiState.value.scenario)
        verify(exactly = 1) { navigation.stop() }
    }

    @Test
    fun `when ShowAddWaypointPanel, given getPoiData succeeds, placeDetails is set`() {
        val firstPlace = mockk<PlaceMatch> { every { place.coordinate } returns defaultGeoPoint }
        val response = mockk<ReverseGeocoderResponse> { every { places } returns listOf(firstPlace) }

        val captureCallback = slot<ReverseGeocoderCallback>()
        every { reverseGeocoder.reverseGeocode(any(), capture(captureCallback)) } answers {
            captureCallback.captured.onSuccess(response)
            Cancellable {}
        }

        viewModel.dispatchAction(ShowAddWaypointPanel(defaultGeoPoint))

        assertEquals(firstPlace.place, viewModel.mapScreenUiState.value.placeDetails?.place)
        assertEquals(defaultGeoPoint, viewModel.mapScreenUiState.value.destinationMarker)
        assertEquals(CameraTrackingMode.None, viewModel.mapScreenUiState.value.cameraTrackingMode)
        assertEquals(null, viewModel.mapScreenUiState.value.routeStop)
    }

    @Test
    fun `when ShowRemoveWaypointPanel, given getPoiData succeeds, routeStop is set`() {
        val firstPlace = mockk<PlaceMatch> { every { place.coordinate } returns defaultGeoPoint }
        val response = mockk<ReverseGeocoderResponse> { every { places } returns listOf(firstPlace) }

        val captureCallback = slot<ReverseGeocoderCallback>()
        every { reverseGeocoder.reverseGeocode(any(), capture(captureCallback)) } answers {
            captureCallback.captured.onSuccess(response)
            Cancellable {}
        }

        val routeStop = mockk<RouteStop>(relaxed = true)
        viewModel.dispatchAction(ShowRemoveWaypointPanel(routeStop))

        assertEquals(firstPlace.place, viewModel.mapScreenUiState.value.placeDetails?.place)
        assertEquals(null, viewModel.mapScreenUiState.value.destinationMarker)
        assertEquals(CameraTrackingMode.None, viewModel.mapScreenUiState.value.cameraTrackingMode)
        assertEquals(routeStop, viewModel.mapScreenUiState.value.routeStop)
    }

    @Test
    fun `when CloseWaypointPanel, placeDetails, destinationMarker and routeStop are cleared`() {
        viewModel.dispatchAction(CloseWaypointPanel)

        assertNull(viewModel.mapScreenUiState.value.placeDetails)
        assertNull(viewModel.mapScreenUiState.value.destinationMarker)
        assertEquals(CameraTrackingMode.FollowRouteDirection, viewModel.mapScreenUiState.value.cameraTrackingMode)
        assertNull(viewModel.mapScreenUiState.value.routeStop)
    }

    @Test
    fun `when UpdateRoute, the route is set in navigation`() {
        val route = mockk<Route>(relaxed = true)
        val routePlanningOptions = mockk<RoutePlanningOptions>(relaxed = true)
        viewModel.dispatchAction(UpdateRoute(route, routePlanningOptions))

        verify { navigation.setActiveRoutePlan(RoutePlan(route, routePlanningOptions)) }

        assertNull(viewModel.mapScreenUiState.value.placeDetails)
        assertNull(viewModel.mapScreenUiState.value.destinationMarker)
        assertEquals(CameraTrackingMode.FollowRouteDirection, viewModel.mapScreenUiState.value.cameraTrackingMode)
        assertNull(viewModel.mapScreenUiState.value.routeStop)
    }

    @Test
    fun `when CleanRoutePreview, it goes back to POI_FOCUS scenario and cleans the routes`() {
        val onClearRoutesMock = mockk<() -> Unit>(relaxed = true)
        viewModel.dispatchAction(CleanRoutePreview(onClearRoutesMock))

        assertNotEquals(POI_FOCUS, viewModel.mapScreenUiState.value.scenario)
        verify(exactly = 1) { onClearRoutesMock() }
    }
}
