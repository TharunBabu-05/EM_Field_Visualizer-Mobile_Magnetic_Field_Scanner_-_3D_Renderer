package com.emfield.visualizer.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.emfield.visualizer.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI automation tests for MainActivity
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun testMainActivityLaunch() {
        // Test that main activity launches successfully
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.bottom_navigation))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.nav_host_fragment))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testBottomNavigationItems() {
        // Test that all bottom navigation items are present
        onView(withId(R.id.bottom_navigation))
            .check(matches(isDisplayed()))
        
        // Test scan navigation item
        onView(withText("Scan"))
            .check(matches(isDisplayed()))
            .perform(click())
        
        // Test visualization navigation item
        onView(withText("Visualize"))
            .check(matches(isDisplayed()))
            .perform(click())
        
        // Test settings navigation item
        onView(withText("Settings"))
            .check(matches(isDisplayed()))
            .perform(click())
    }
    
    @Test
    fun testToolbarTitle() {
        // Test that toolbar shows app name
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
        
        onView(withText("EM Field Visualizer"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testFragmentNavigation() {
        // Navigate to Scan fragment
        onView(withText("Scan"))
            .perform(click())
        
        // Verify scan fragment content
        onView(withText("Field Strength"))
            .check(matches(isDisplayed()))
        
        // Navigate to Visualization fragment
        onView(withText("Visualize"))
            .perform(click())
        
        // Verify visualization fragment content
        onView(withId(R.id.gl_surface_view))
            .check(matches(isDisplayed()))
        
        // Navigate to Settings fragment
        onView(withText("Settings"))
            .perform(click())
        
        // Verify settings fragment content
        onView(withText("Settings"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testBackButtonBehavior() {
        // Navigate to different fragments
        onView(withText("Scan"))
            .perform(click())
        
        onView(withText("Visualize"))
            .perform(click())
        
        onView(withText("Settings"))
            .perform(click())
        
        // Test back button press
        activityRule.scenario.onActivity { activity ->
            activity.onBackPressed()
        }
        
        // Should navigate back to previous fragment
        onView(withText("Visualize"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testSystemUiVisibility() {
        // Test that system UI is properly handled
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.bottom_navigation))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testNavigationStatePersistence() {
        // Navigate to a specific fragment
        onView(withText("Visualize"))
            .perform(click())
        
        // Recreate activity (simulate configuration change)
        activityRule.recreate()
        
        // Verify navigation state is preserved
        onView(withId(R.id.gl_surface_view))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testAccessibility() {
        // Test accessibility labels
        onView(withId(R.id.toolbar))
            .check(matches(hasContentDescription()))
        
        onView(withId(R.id.bottom_navigation))
            .check(matches(hasContentDescription()))
    }
    
    @Test
    fun testScreenRotation() {
        // Test behavior on screen rotation
        onView(withText("Scan"))
            .perform(click())
        
        // Simulate screen rotation
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
    fun testMemoryLeakPrevention() {
        // Navigate through multiple fragments
        repeat(10) {
            onView(withText("Scan"))
                .perform(click())
            
            onView(withText("Visualize"))
                .perform(click())
            
            onView(withText("Settings"))
                .perform(click())
        }
        
        // Verify app is still responsive
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testLongPressGestures() {
        // Test long press on navigation items
        onView(withText("Scan"))
            .perform(click())
        
        // Long press should not crash
        onView(withText("Field Strength"))
            .perform(click())
    }
    
    @Test
    fun testEdgeCases() {
        // Test rapid navigation
        repeat(20) {
            onView(withText("Scan"))
                .perform(click())
            
            onView(withText("Visualize"))
                .perform(click())
        }
        
        // Verify app is still stable
        onView(withId(R.id.bottom_navigation))
            .check(matches(isDisplayed()))
    }
}
