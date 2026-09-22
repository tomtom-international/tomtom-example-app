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

package com.example.demo.map.manualmapmanagement

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tomtom.quantity.Distance
import com.tomtom.quantity.Memory
import com.tomtom.sdk.common.Result
import com.tomtom.sdk.datamanagement.nds.region.DownloadedRegionDataAction
import com.tomtom.sdk.datamanagement.nds.region.Region
import com.tomtom.sdk.datamanagement.nds.region.RegionInfoUpdateListener
import com.tomtom.sdk.datamanagement.nds.region.RegionInstallState
import com.tomtom.sdk.datamanagement.nds.region.RegionOperation
import com.tomtom.sdk.datamanagement.nds.region.RegionOperationStatus
import com.tomtom.sdk.datamanagement.nds.region.RegionQueryCallback
import com.tomtom.sdk.datamanagement.nds.region.RegionState
import com.tomtom.sdk.datamanagement.nds.region.RegionStateInfo
import com.tomtom.sdk.datamanagement.nds.region.RegionStructure
import com.tomtom.sdk.datamanagement.nds.region.RegionUpdater
import com.tomtom.sdk.datamanagement.nds.update.MapUpdateError
import com.tomtom.sdk.datamanagement.regionstore.RegionStore
import com.tomtom.sdk.location.GeoPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val MADRID = GeoPoint(40.4166, -3.7000)
private const val SEARCH_RADIUS_KILOMETERS = 5.0

enum class OperationState {
    STARTING,
    CANCELLING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
}

/**
 * ViewModel for the Manual Map Management demo.
 *
 * Note: This demo requires extended permissions only available on production keys.
 * [Contact sales](https://www.tomtom.com/contact-sales?source_app=developerportal&source_product=tomtom-sdk-for-android)
 * to get started.
 */
class ManualMapManagementViewModel(
    private val regionStore: RegionStore,
    val onSetIsLoading: (Boolean) -> Unit,
    private val onOperationFailed: () -> Unit,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private lateinit var regionUpdater: RegionUpdater
    private lateinit var allRegions: Set<Region>
    private lateinit var regionsState: Map<Region.Id, RegionState>
    private var downloadRegion: Region.Id? = null

    private val _operationState = MutableStateFlow(OperationState.COMPLETED)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    private val _operationProgress = MutableStateFlow(0u)
    val operationProgress: StateFlow<UInt> = _operationProgress.asStateFlow()

    private val _installedSize = MutableStateFlow<Memory?>(null)
    val installedSize: StateFlow<Memory?> = _installedSize.asStateFlow()

    private val _updateSize = MutableStateFlow<Memory?>(null)
    val updateSize: StateFlow<Memory?> = _updateSize.asStateFlow()

    private val _installState = MutableStateFlow<RegionInstallState?>(RegionInstallState.NotInstalled)
    val installState: StateFlow<RegionInstallState?> = _installState.asStateFlow()

    private val _operationType = MutableStateFlow<RegionOperation.Type?>(null)
    val operationType: StateFlow<RegionOperation.Type?> = _operationType.asStateFlow()

    private val regionStructureChangedListener = object : RegionInfoUpdateListener {
        override fun onStructureChanged(structureResult: Result<RegionStructure, MapUpdateError>) {
            when (structureResult) {
                is Result.Success -> {
                    allRegions = structureResult.value().roots
                    regionsState = structureResult.value().regionStates
                    getRegionsAroundPosition()
                }
                is Result.Failure -> {
                    Log.e(TAG, "Failed to get region structure: ${structureResult.failure()}")
                }
            }
        }

        override fun onStatesChanged(stateInfo: RegionStateInfo) {
            val regionState = stateInfo.regionStates[downloadRegion]
            if (regionState != null) {
                _installState.value = regionState.installState
                _installedSize.value = regionState.dataInfo?.dataSize
                _updateSize.value = regionState.updateState?.downloadSize
            }
        }

        override fun onOperationStatusesChanged(statuses: Set<RegionOperationStatus>) {
            val operationStatus = statuses.firstOrNull()
            _operationType.value = operationStatus?.operation?.type
            when (operationStatus) {
                is RegionOperationStatus.Progress -> {
                    _operationProgress.value = operationStatus.progress
                    _operationState.value = OperationState.IN_PROGRESS
                }
                is RegionOperationStatus.Completed -> {
                    when (operationStatus.error) {
                        null -> _operationState.value = OperationState.COMPLETED
                        is MapUpdateError.Canceled -> Unit
                        else -> {
                            _operationState.value = OperationState.FAILED
                            onOperationFailed()
                        }
                    }
                }
            }
        }
    }

    init {
        onSetIsLoading(true)
        viewModelScope.launch(ioDispatcher) {
            regionStore.setUpdatesEnabled(true)
            regionUpdater = regionStore.obtainRegionUpdater()
            regionUpdater.addRegionInfoUpdateListener(regionStructureChangedListener)
        }
    }

    private fun getRegionsAroundPosition() = regionUpdater.findRegionsAroundPosition(
        position = MADRID,
        radius = Distance.kilometers(SEARCH_RADIUS_KILOMETERS),
        callback = object : RegionQueryCallback {
            override fun onSuccess(result: Set<Region.Id>) {
                downloadRegion = result.firstOrNull()
                val regionState = regionsState[downloadRegion]

                if (downloadRegion == null || regionState == null) {
                    Log.e(
                        TAG,
                        "Failed to find regions around Madrid. " +
                            "No regions found within the specified radius.",
                    )
                    onSetIsLoading(false)
                    return
                }

                _installState.value = regionState.installState
                _installedSize.value = regionState.dataInfo?.dataSize
                _updateSize.value = regionState.updateState?.downloadSize

                onSetIsLoading(false)
            }

            override fun onFailure(failure: MapUpdateError) {
                Log.e(TAG, "Failed to find regions around Madrid: $failure")
            }
        },
    )

    fun downloadRegion() {
        _operationState.value = OperationState.STARTING
        viewModelScope.launch(ioDispatcher) {
            when (val result = regionUpdater.download(listOfNotNull(downloadRegion))) {
                is Result.Success -> Unit
                is Result.Failure -> {
                    _operationState.value = OperationState.FAILED
                    onOperationFailed()
                    Log.e(TAG, "Failed to start download: ${result.failure()}")
                }
            }
        }
    }

    fun deleteRegion() {
        _operationState.value = OperationState.STARTING
        viewModelScope.launch(ioDispatcher) {
            when (val result = regionUpdater.delete(listOfNotNull(downloadRegion))) {
                is Result.Success -> Unit
                is Result.Failure -> {
                    _operationState.value = OperationState.FAILED
                    onOperationFailed()
                    Log.e(TAG, "Failed to delete region: ${result.failure()}")
                }
            }
        }
    }

    fun cancelOperation() {
        _operationState.value = OperationState.CANCELLING
        viewModelScope.launch(ioDispatcher) {
            regionUpdater.cancelOperations(
                regionIds = setOfNotNull(downloadRegion),
                action = DownloadedRegionDataAction.Keep,
            )
            _operationType.value = null
            _operationState.value = OperationState.COMPLETED
        }
    }

    companion object {
        val OFFLINE_MAP_KEY = object : CreationExtras.Key<RegionStore> {}
        val ON_SET_IS_LOADING_KEY = object : CreationExtras.Key<(Boolean) -> Unit> {}
        val ON_OPERATION_FAILED_KEY = object : CreationExtras.Key<() -> Unit> {}

        const val TAG = "ManualMapManagementViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ManualMapManagementViewModel(
                    regionStore = this[OFFLINE_MAP_KEY] as RegionStore,
                    onSetIsLoading = this[ON_SET_IS_LOADING_KEY] as (Boolean) -> Unit,
                    onOperationFailed = this[ON_OPERATION_FAILED_KEY] as () -> Unit,
                )
            }
        }
    }
}
