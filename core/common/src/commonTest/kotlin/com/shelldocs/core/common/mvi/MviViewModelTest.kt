package com.shelldocs.core.common.mvi

import com.shelldocs.core.common.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private data class CounterState(val count: Int) : MviState

private sealed interface CounterIntent : MviIntent {
    data object Increment : CounterIntent
    data object Celebrate : CounterIntent
}

private sealed interface CounterEffect : MviEffect {
    data object Confetti : CounterEffect
}

private class TestDispatchers(dispatcher: CoroutineDispatcher) : DispatcherProvider {
    override val main = dispatcher
    override val default = dispatcher
    override val io = dispatcher
}

private class CounterViewModel(dispatchers: DispatcherProvider) :
    MviViewModel<CounterIntent, CounterState, CounterEffect>(CounterState(0), dispatchers) {

    override suspend fun handleIntent(intent: CounterIntent) {
        when (intent) {
            CounterIntent.Increment -> setState { copy(count = count + 1) }
            CounterIntent.Celebrate -> sendEffect(CounterEffect.Confetti)
        }
    }
}

class MviViewModelTest {

    @Test
    fun intentsReduceStateSequentially() = runTest {
        val viewModel = CounterViewModel(TestDispatchers(StandardTestDispatcher(testScheduler)))

        repeat(3) { viewModel.onIntent(CounterIntent.Increment) }
        testScheduler.advanceUntilIdle()

        assertEquals(3, viewModel.currentState.count)
        viewModel.clear()
    }

    @Test
    fun effectsAreDeliveredToCollectors() = runTest {
        val viewModel = CounterViewModel(TestDispatchers(StandardTestDispatcher(testScheduler)))
        val collected = mutableListOf<CounterEffect>()
        val job = launch { collected += viewModel.effects.first() }
        testScheduler.runCurrent()

        viewModel.onIntent(CounterIntent.Celebrate)
        testScheduler.advanceUntilIdle()
        job.join()

        assertEquals(listOf(CounterEffect.Confetti), collected)
        viewModel.clear()
    }

    @Test
    fun initialStateIsExposedBeforeAnyIntent() = runTest {
        val viewModel = CounterViewModel(TestDispatchers(StandardTestDispatcher(testScheduler)))
        assertEquals(CounterState(0), viewModel.state.value)
        viewModel.clear()
    }
}
