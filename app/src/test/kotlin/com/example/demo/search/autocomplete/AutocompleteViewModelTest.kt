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

package com.example.demo.search.autocomplete

import android.util.Log
import com.example.application.common.DEFAULT_DEBOUNCE_TIMEOUT
import com.tomtom.sdk.common.Result
import com.tomtom.sdk.location.poi.Brand
import com.tomtom.sdk.search.Search
import com.tomtom.sdk.search.SearchResponse
import com.tomtom.sdk.search.common.error.SearchFailure
import com.tomtom.sdk.search.model.result.AutocompleteSegmentBrand
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AutocompleteViewModelTest {
    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var search: Search

    private lateinit var viewModel: AutocompleteViewModel

    @Before
    fun setup() {
        val dispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(dispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        viewModel = AutocompleteViewModel(
            search = search,
            ioDispatcher = dispatcher,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    @Test
    fun `when setInputQuery, inputQuery is updated`() {
        val text = "test query"
        viewModel.setInputQuery(text)

        assertEquals(text, viewModel.inputQuery.value)
    }

    @Test
    fun `when clearAutocompleteResults, autocompleteResults are cleared`() {
        viewModel.clearAutocompleteResults()

        assertEquals(0, viewModel.autocompleteResults.value.size)
    }

    @Test
    fun `when selectAutocompleteOption, autocompleteResults are cleared and search is called`() = runTest {
        val response = mockk<SearchResponse>().apply { every { results } returns emptyList() }
        every { search.search(any()) } returns Result.success(response)
        val brand = mockk<Brand>(relaxed = true)
        viewModel.selectAutocompleteOption(AutocompleteSegmentBrand(brand), {}, {})

        assertEquals(0, viewModel.autocompleteResults.value.size)
        verify(exactly = 1) { search.search(any()) }
    }

    @Test
    fun `when performSearch, when the response is a failure, it calls onSearchFailure`() {
        every { search.search(any()) } returns Result.failure(SearchFailure.NetworkFailure(""))

        val onSearchFailure: (SearchFailure) -> Unit = mockk(relaxed = true)
        viewModel.performSearch(query = "query", onSearchSuccess = {}, onSearchFailure = onSearchFailure)

        verify(exactly = 1) { onSearchFailure(any()) }
    }

    @Test
    fun `when setting an input query, it should call autocomplete`() = runTest {
        every { search.autocompleteSearch(any()) } returns Result.success(mockk(relaxed = true))
        viewModel.setInputQuery("query")

        advanceTimeBy(DEFAULT_DEBOUNCE_TIMEOUT + 1)

        verify(exactly = 1) { search.autocompleteSearch(any()) }
    }
}
