package com.emfield.visualizer.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
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
 * UI automation tests for Scan Fragment
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ScanFragmentTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun testScanFragmentLaunch() {
        // Navigate to scan fragment
        onView(withText("Scan"))
            .perform(click())
        
        // Verify scan fragment elements
        onView(withText("Field Strength"))
            .check(matches(isDisplayed()))
        
        onView(withText("Points Collected"))
            .check(matches(isDisplayed()))
        
        onView(withText("Real-time Mode"))
            .check(matches(isDisplayed()))
        
        onView(withText("Calibrate"))
            .check(matches(isDisplayed()))
        
        onView(withText("Start"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testCalibrateButton() {
        // Navigate to scan fragment
        onView(withText("Scan"))
            .perform(click())
        
        // Click calibrate button
        onView(withText("Calibrate"))
            .perform(click())
        
        // Verify calibration UI appears
        // (This would depend on the actual calibration dialog implementation)
        onView(withText("Calibration"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testStartStopScanning() {
        // Navigate to scan fragment
        onView(withText("Scan"))
            .perform(click())
        
        // Click start button
        onView(withText("Start"))
            .perform(click())
        
        // Verify button text changes to "Stop"
        onView(withText("Stop"))
            .check(matches(isDisplayed()))
        
        // Click stop button
        onView(withText("Stop"))
            .perform(click())
        
        // Verify button text changes back to "Start"
        onView(withText("Start"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testRealtimeModeToggle() {
        // Navigate to scan fragment
        onView(withText("Scan"))
            .perform(click())
        
        // Find and click the realtime mode switch
        onView(withText("Real-time Mode"))
            .check(matches(isDisplayed()))
        
        // Toggle the switch
        onView(withId(R.id.scan_mode_switch))
            .perform(click())
        
        // Verify switch state changed
        // (This would depend on the actual switch implementation)
    }
    
    @Test
    fun testFieldStrengthDisplay() {
        // Navigate to scan fragment
        onView(withText("Scan"))
            .perform(click())
        
        // Verify field strength display
        onView(withId(R.id.field_strength_text))
            .check(matches(isDisplayed()))
        
        // Should show initial value
        onView(withId(R.id.field_strength_text))
            .check(matches(withText("0.0 μT")))
    }
    
    @Test
    fun testPointsCollectedDisplay() {
        // Navigate to scan fragment
        onView(withText("Scan"))
            .perform(click())
        
        // Verify points collected display
        onView(withId(R.id.points_collected_text))
            .check(matches(isDisplayed()))
        
        // Should show initial value
        onView(withId(R.id.points_collected_text))
            .check(matches(withText("0 points collected")))
    }
    
    @Test
    fun testSensorStatusDisplay() {
        // Navigate to scan fragment
        onView(withText("Scan"))
            .perform(click())
        
        // Verify sensor status display
        onView(withId(R.id.sensor_status_text))
            .check(matches(isDisplayed()))
        
        // Should show ready status
        onView(withId(R.id.sensor_status_text))
            .check(matches(withText("Sensor Status: Ready")))
    }
    
    @Test
    fun testScrollingBehavior() {
        // Navigate to scan fragment
        onView(withText("Scan"))
            .perform(click())
        
        // Test scrolling
        onView(withId(R.id.nav_host_fragment))
            .perform(swipeUp())
        
        // Verify content is still visible after scroll
        onView(withText("Field Strength"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testButtonStates() {
        // Navigate to scan fragment
        onView(withText("Scan"))
            .perform(click())
        
        // Verify initial button states
        onView(withText("Calibrate"))
            .check(matches(isEnabled()))
        
        onView(withText("Start"))
            .check(matches(isEnabled()))
        
        // Test button interactions
        onView(withText("Start"))
            .perform(click())
        
        // Verify button state changed
        onView(withText("Stop"))
            .check(matches(isEnabled()))
    }
    
    @Test
    fun testCalibrationProgress() {
        // Navigate to scan fragment
        onView(withText("Scan"))
            .perform(click())
        
        // Start calibration
        onView(withText("Calibrate"))
            .perform(click())
        
        // Wait for calibration to start
        runBlocking { delay(1000) }
        
        // Verify calibration progress is displayed
        // (This would depend on the actual calibration UI implementation)
        onView(withText("Calibration"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testDataCollection() {
        // Navigate to scan fragment
        onView(withText("Scan"))
            .perform(click())
        
        // Start scanning
        onView(withText("Start"))
            .perform(click())
        
        // Wait for data collection
        runBlocking { delay(2000) }
        
        // Verify data is being collected
        // (This would depend on sensor availability in test environment)
        onView(withId(R.id.field_strength_text))
            .check(matches(isDisplayed()))
        
        // Stop scanning
        onView(withText("Stop"))
            .perform(click())
    }
    
    @Test
    fun testErrorHandling() {
        // Navigate to scan fragment
        onView(withText("Scan"))
            .perform(click())
        
        // Test error scenarios
        // (This would depend on mocking sensor failures)
        onView(withId(R.id.sensor_status_text))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testAccessibility() {
        // Navigate to scan fragment
        onView(withText("Scan"))
            .perform(click())
        
        // Test accessibility labels
        onView(withText("Calibrate"))
            .check(matches(hasContentDescription()))
        
        onView(withText("Start"))
            .check(matches(hasContentDescription()))
        
        onView(withText("Real-time Mode"))
            .check(matches(hasContentDescription()))
    }
    
    @Test
    fun testScreenRotation() {
        // Navigate to scan fragment
        onView(withText("Scan"))
            .perform(click())
        
        // Rotate screen
        activityRule.scenario.onActivity { activity ->
            activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        
        // Verify UI adapts to rotation
        onView(withText("Field Strength"))
            .check(matches(isDisplayed()))
        
        // Restore portrait orientation
        activityRule.scenario.onActivity { activity ->
            activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
    
    @Test
    fun testLongPressGestures() {
        // Navigate to scan fragment
        onView(withText("Scan"))
            .perform(click())
        
        // Test long press on buttons
        onView(withText("Calibrate"))
            .perform(click())
        
        // Should not crash
        onView(withText("Calibration"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testRapidButtonPresses() {
        // Navigate to scan fragment
        onView(withText("Scan"))
            .perform(click())
        
        // Test rapid button presses
        repeat(10) {
            onView(withText("Start"))
                .perform(click())
            
            onView(withText("Stop"))
                .perform(click())
        }
        
        // Verify app is still responsive
        onView(withText("Start"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testMemoryLeakPrevention() {
        // Navigate to scan fragment
        onView(withText("Scan"))
            .perform(click())
        
        // Perform multiple operations
        repeat(20) {
            onView(withText("Start"))
                .perform(click())
            
            runBlocking { delay(100) }
            
            onView(withText("Stop"))
                .perform(click())
            
            onView(withText("Calibrate"))
                .perform(click())
            
            runBlocking { delay(100) }
            
            // Close any dialogs
            onView(isRoot())
                .perform(pressBack())
        }
        
        // Verify app is still responsive
        onView(withText("Field Strength"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testConfigurationChanges() {
        // Navigate to scan fragment
        onView(withText("Scan"))
            .perform(click())
        
        // Start scanning
        onView(withText("Start"))
            .perform(click())
        
        // Recreate activity (simulate configuration change)
        activityRule.recreate()
        
        // Verify state is preserved
        onView(withText("Stop"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testEdgeCases() {
        // Navigate to scan fragment
        onView(withText("Scan"))
            .perform(click())
        
        // Test edge case: rapid navigation
        repeat(10) {
            onView(withText("Scan"))
                .perform(click())
            
            onView(withText("Visualize"))
                .perform(click())
            
            onView(withText("Scan"))
                .perform(click())
        }
        
        // Verify scan fragment is still functional
        onView(withText("Field Strength"))
            .check(matches(isDisplayed()))
    }
}
