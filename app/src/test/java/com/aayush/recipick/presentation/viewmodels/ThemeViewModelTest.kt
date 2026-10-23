package com.aayush.recipick.presentation.viewmodels

import com.aayush.recipick.domain.model.AppThemeConfig
import com.aayush.recipick.domain.model.ColorPalette
import com.aayush.recipick.domain.model.ThemeMode
import com.aayush.recipick.domain.repository.ThemePreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FakeThemePreferencesRepository : ThemePreferencesRepository {
    private val configFlow = MutableStateFlow(AppThemeConfig())

    override fun getThemeConfig(): Flow<AppThemeConfig> = configFlow

    override suspend fun setThemeMode(mode: ThemeMode) {
        configFlow.update { it.copy(themeMode = mode) }
    }

    override suspend fun setColorPalette(palette: ColorPalette) {
        configFlow.update { it.copy(colorPalette = palette) }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialDefaultThemeConfig() = runTest {
        val repo = FakeThemePreferencesRepository()
        val viewModel = ThemeViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()
        val config = viewModel.themeConfig.value

        assertEquals(ThemeMode.SYSTEM, config.themeMode)
        assertEquals(ColorPalette.ORANGE, config.colorPalette)
    }

    @Test
    fun testSetThemeModeUpdatesState() = runTest {
        val repo = FakeThemePreferencesRepository()
        val viewModel = ThemeViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setThemeMode(ThemeMode.DARK)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ThemeMode.DARK, viewModel.themeConfig.value.themeMode)
    }

    @Test
    fun testSetColorPaletteUpdatesState() = runTest {
        val repo = FakeThemePreferencesRepository()
        val viewModel = ThemeViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setColorPalette(ColorPalette.MINT)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ColorPalette.MINT, viewModel.themeConfig.value.colorPalette)
    }
}
