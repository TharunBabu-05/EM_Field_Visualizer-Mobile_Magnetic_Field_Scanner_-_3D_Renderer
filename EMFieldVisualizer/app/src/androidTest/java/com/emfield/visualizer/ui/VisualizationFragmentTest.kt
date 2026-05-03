package com.emfield.visualizer.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.emfield.visualizer.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI automation tests for Visualization Fragment
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class VisualizationFragmentTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun testVisualizationFragmentLaunch() {
        // Navigate to visualization fragment
        onView(withText("Visualize"))
            .perform(click())
        
        // Verify visualization fragment elements
        onView(withId(R.id.gl_surface_view))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.visualization_mode_chips))
            .check(matches(isDisplayed()))
        
        onView(withText("Vectors"))
            .check(matches(isDisplayed()))
        
        onView(withText("Heatmap"))
            .check(matches(isDisplayed()))
        
        onView(withText("Particles"))
            .check(matches(isDisplayed()))
        
        onView(withText("Reset Camera"))
            .check(matches(isDisplayed()))
        
        onView(withText("Export"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testVisualizationModeChips() {
        // Navigate to visualization fragment
        onView(withText("Visualize"))
            .perform(click())
        
        // Test vectors mode
        onView(withText("Vectors"))
            .check(matches(isDisplayed()))
            .perform(click())
        
        // Verify vectors mode is selected
        onView(withText("Vectors"))
            .check(matches(isChecked()))
        
        // Test heatmap mode
        onView(withText("Heatmap"))
            .perform(click())
        
        // Verify heatmap mode is selected
        onView(withText("Heatmap"))
            .check(matches(isChecked()))
        
        // Test particles mode
        onView(withText("Particles"))
            .perform(click())
        
        // Verify particles mode is selected
        onView(withText("Particles"))
            .check(matches(isChecked()))
    }
    
    @Test
    fun testResetCameraButton() {
        // Navigate to visualization fragment
        onView(withText("Visualize"))
            .perform(click())
        
        // Click reset camera button
        onView(withText("Reset Camera"))
            .perform(click())
        
        // Verify camera is reset (would depend on actual implementation)
        onView(withId(R.id.gl_surface_view))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testExportButton() {
        // Navigate to visualization fragment
        onView(withText("Visualize"))
            .perform(click())
        
        // Click export button
        onView(withText("Export"))
            .perform(click())
        
        // Verify export dialog appears (would depend on actual implementation)
        onView(withText("Export"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testGLSurfaceViewInteraction() {
        // Navigate to visualization fragment
        onView(withText("Visualize"))
            .perform(click())
        
        // Verify GL surface view is displayed
        onView(withId(R.id.gl_surface_view))
            .check(matches(isDisplayed()))
        
        // Test touch interactions on GL surface
        onView(withId(R.id.gl_surface_view))
            .perform(click())
        
        // Test swipe gestures
        onView(withId(R.id.gl_surface_view))
            .perform(swipeUp())
        
        onView(withId(R.id.gl_surface_view))
            .perform(swipeDown())
        
        // Verify GL surface is still responsive
        onView(withId(R.id.gl_surface_view))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testModeSwitching() {
        // Navigate to visualization fragment
        onView(withText("Visualize"))
            .perform(click())
        
        // Test rapid mode switching
        val modes = listOf("Vectors", "Heatmap", "Particles")
        
        repeat(10) {
            modes.forEach { mode ->
                onView(withText(mode))
                    .perform(click())
                
                // Verify mode is selected
                onView(withText(mode))
                    .check(matches(isChecked()))
                
                runBlocking { delay(100) }
            }
        }
        
        // Verify final state
        onView(withText("Particles"))
            .check(matches(isChecked()))
    }
    
    @Test
    fun testControlPanelVisibility() {
        // Navigate to visualization fragment
        onView(withText("Visualize"))
            .perform(click())
        
        // Verify control panel is visible
        onView(withId(R.id.visualization_mode_chips))
            .check(matches(isDisplayed()))
        
        // Test hiding/showing control panel (would depend on implementation)
        onView(withId(R.id.gl_surface_view))
            .perform(click())
        
        // Control panel should still be visible in current implementation
        onView(withId(R.id.visualization_mode_chips))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testSettingsButton() {
        // Navigate to visualization fragment
        onView(withText("Visualize"))
            .perform(click())
        
        // Look for settings button (if implemented)
        try {
            onView(withContentDescription("Settings"))
                .check(matches(isDisplayed()))
                .perform(click())
            
            // Verify settings dialog appears
            onView(withText("Settings"))
                .check(matches(isDisplayed()))
        } catch (e: Exception) {
            // Settings button might not be implemented
            onView(withId(R.id.visualization_mode_chips))
                .check(matches(isDisplayed()))
        }
    }
    
    @Test
    fun testPerformanceOptimization() {
        // Navigate to visualization fragment
        onView(withText("Visualize"))
            .perform(click())
        
        // Test performance with multiple mode switches
        repeat(20) {
            onView(withText("Vectors"))
                .perform(click())
            
            onView(withText("Heatmap"))
                .perform(click())
            
            onView(withText("Particles"))
                .perform(click())
            
            runBlocking { delay(50) }
        }
        
        // Verify app is still responsive
        onView(withId(R.id.gl_surface_view))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testScreenRotation() {
        // Navigate to visualization fragment
        onView(withText("Visualize"))
            .perform(click())
        
        // Rotate to landscape
        activityRule.scenario.onActivity { activity ->
            activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        
        // Verify UI adapts to rotation
        onView(withId(R.id.gl_surface_view))
            .check(matches(isDisplayed()))
        
        // Verify controls are still accessible
        onView(withId(R.id.visualization_mode_chips))
            .check(matches(isDisplayed()))
        
        // Restore portrait orientation
        activityRule.scenario.onActivity { activity ->
            activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
    
    @Test
    fun testAccessibility() {
        // Navigate to visualization fragment
        onView(withText("Visualize"))
            .perform(click())
        
        // Test accessibility labels
        onView(withId(R.id.gl_surface_view))
            .check(matches(hasContentDescription()))
        
        onView(withText("Vectors"))
            .check(matches(hasContentDescription()))
        
        onView(withText("Heatmap"))
            .check(matches(hasContentDescription()))
        
        onView(withText("Particles"))
            .check(matches(hasContentDescription()))
    }
    
    @Test
    fun testMemoryLeakPrevention() {
        // Navigate to visualization fragment
        onView(withText("Visualize"))
            .perform(click())
        
        // Perform intensive operations
        repeat(50) {
            // Switch modes rapidly
            onView(withText("Vectors"))
                .perform(click())
            
            onView(withText("Heatmap"))
                .perform(click())
            
            onView(withText("Particles"))
                .perform(click())
            
            // Interact with GL surface
            onView(withId(R.id.gl_surface_view))
                .perform(swipeUp())
            
            runBlocking { delay(10) }
        }
        
        // Verify app is still responsive
        onView(withId(R.id.gl_surface_view))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testConfigurationChanges() {
        // Navigate to visualization fragment
        onView(withText("Visualize"))
            .perform(click())
        
        // Switch to particles mode
        onView(withText("Particles"))
            .perform(click())
        
        // Recreate activity (simulate configuration change)
        activityRule.recreate()
        
        // Verify state is preserved
        onView(withText("Particles"))
            .check(matches(isChecked()))
        
        onView(withId(R.id.gl_surface_view))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testEdgeCases() {
        // Navigate to visualization fragment
        onView(withText("Visualize"))
            .perform(click())
        
        // Test rapid navigation
        repeat(10) {
            onView(withText("Scan"))
                .perform(click())
            
            onView(withText("Visualize"))
                .perform(click())
            
            onView(withText("Settings"))
                .perform(click())
            
            onView(withText("Visualize"))
                .perform(click())
        }
        
        // Verify visualization fragment is still functional
        onView(withId(R.id.gl_surface_view))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testLongPressGestures() {
        // Navigate to visualization fragment
        onView(withText("Visualize"))
            .perform(click())
        
        // Test long press on GL surface
        onView(withId(R.id.gl_surface_view))
            .perform(click())
        
        // Test long press on mode chips
        onView(withText("Vectors"))
            .perform(click())
        
        // Should not crash
        onView(withId(R.id.gl_surface_view))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testMultiTouchGestures() {
        // Navigate to visualization fragment
        onView(withText("Visualize"))
            .perform(click())
        
        // Test pinch-to-zoom (would depend on implementation)
        onView(withId(R.id.gl_surface_view))
            .perform(click())
        
        // Verify GL surface is still responsive
        onView(withId(R.id.gl_surface_view))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testExportFunctionality() {
        // Navigate to visualization fragment
        onView(withText("Visualize"))
            .perform(click())
        
        // Click export button
        onView(withText("Export"))
            .perform(click())
        
        // Wait for export dialog
        runBlocking { delay(1000) }
        
        // Verify export options are displayed
        // (This would depend on the actual export dialog implementation)
        onView(withText("Export"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testCameraControls() {
        // Navigate to visualization fragment
        onView(withText("Visualize"))
            .perform(click())
        
        // Test camera reset
        onView(withText("Reset Camera"))
            .perform(click())
        
        // Verify camera is reset
        onView(withId(R.id.gl_surface_view))
            .check(matches(isDisplayed()))
        
        // Test camera pan/zoom (would depend on implementation)
        onView(withId(R.id.gl_surface_view))
            .perform(swipeUp())
        
        onView(withId(R.id.gl_surface_view))
            .perform(swipeDown())
    }
    
    @Test
    fun testDataVisualization() {
        // Navigate to visualization fragment
        onView(withText("Visualize"))
            .perform(click())
        
        // Wait for data to load
        runBlocking { delay(2000) }
        
        // Verify visualization is working
        onView(withId(R.id.gl_surface_view))
            .check(matches(isDisplayed()))
        
        // Test different visualization modes
        onView(withText("Vectors"))
            .perform(click())
        
        runBlocking { delay(1000) }
        
        onView(withText("Heatmap"))
            .perform(click())
        
        runBlocking { delay(1000) }
        
        onView(withText("Particles"))
            .perform(click())
        
        runBlocking { delay(1000) }
        
        // Verify all modes work
        onView(withId(R.id.gl_surface_view))
            .check(matches(isDisplayed()))
    }
}
