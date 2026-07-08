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

package com.example.application.common.extension

import kotlin.coroutines.cancellation.CancellationException

/**
 * Performs the given [action] on the encapsulated [Throwable] exception if this instance represents
 * [failure][Result.isFailure] that is not a CancellationException. If it is a CancellationException, it is rethrown.
 * This method is useful for handling exceptions in coroutines where CancellationException should be propagated.
 * Returns the original `Result` unchanged.
 */
inline fun <T> Result<T>.onFailureIgnoreCancellation(action: (Throwable) -> Unit): Result<T> =
    onFailureOrRethrow<CancellationException, T>(action)

/**
 * Performs the given [action] on the encapsulated [Throwable] exception if this instance represents
 * [failure][Result.isFailure] that is not of type [E]. If it is of type [E], it is rethrown.
 * This method is useful for handling specific exceptions while propagating others.
 * Returns the original `Result` unchanged.
 */
inline fun <reified E : Throwable, T> Result<T>.onFailureOrRethrow(action: (Throwable) -> Unit): Result<T> =
    onFailure { if (it is E) throw it else action(it) }
