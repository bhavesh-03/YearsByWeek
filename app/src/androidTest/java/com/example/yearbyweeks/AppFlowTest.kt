package com.example.yearbyweeks

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.yearbyweeks.data.AppPreferences
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class AppFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private fun screenshot(name: String) {
        rule.waitForIdle()
        // Window and keyboard transitions run outside the Compose test clock.
        android.os.SystemClock.sleep(600)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        File(instrumentation.targetContext.cacheDir, "$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }

    @Test fun appearanceAndCountdownSurviveRecreation() {
        rule.activity.getSharedPreferences("appearance", android.content.Context.MODE_PRIVATE).edit().clear().commit()
        rule.activityRule.scenario.recreate()
        rule.onNodeWithText("Style").performClick()
        rule.onNodeWithText("Light").performScrollTo().performClick()
        screenshot("light-settings")
        rule.onNodeWithText("Year").performClick()
        screenshot("light-home")
        rule.onNodeWithText("Days").performClick()
        rule.onNodeWithText("Add days widget").performScrollTo().assertExists()
        screenshot("days-home")
        rule.onNodeWithText("Style").performClick()
        rule.onNodeWithText("Dark").performScrollTo().performClick()
        rule.onNode(SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsActions.SetProgress)).performScrollTo().performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.SetProgress) { it(0.55f) }
        screenshot("dark-settings")
        rule.onNodeWithText("Year").performClick()
        screenshot("dark-home")
        rule.onNodeWithText("Countdowns").performClick()
        rule.onNodeWithText("+  New countdown").performScrollTo().performClick()
        rule.onNodeWithText("Event name").performTextInput("A new adventure")
        screenshot("countdown-editor")
        rule.onNodeWithText("Save countdown").performClick()
        rule.onNodeWithText("A new adventure").assertExists()
        screenshot("countdown-card")
        rule.activityRule.scenario.recreate()
        rule.waitForIdle()
        val prefs = AppPreferences(rule.activity)
        assertEquals("Dark", prefs.theme)
        assertEquals(0.55f, prefs.opacity, 0.01f)
        assertEquals("A new adventure", prefs.events().single().name)
        rule.onNodeWithContentDescription("Options for A new adventure").performClick()
        rule.onNodeWithText("Edit").performClick()
        rule.onNodeWithText("Event name").performTextReplacement("A fresh start")
        rule.onNodeWithText("Save countdown").performClick()
        assertEquals("A fresh start", prefs.events().single().name)
        rule.onNodeWithContentDescription("Options for A fresh start").performClick()
        rule.onNodeWithText("Delete").performClick()
        rule.onNode(hasText("Delete") and hasAnyAncestor(isDialog())).performClick()
        rule.waitForIdle()
        assertTrue(prefs.events().isEmpty())
        rule.onNodeWithText("Style").performClick()
        rule.onNodeWithText("System").performScrollTo().performClick()
        assertEquals("System", prefs.theme)
    }
}
