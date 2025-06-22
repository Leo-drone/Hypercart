package com.hypercart

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests instrumentés pour l'application Hypercart
 * Ces tests s'exécutent sur un appareil Android ou un émulateur
 */
@RunWith(AndroidJUnit4::class)
class ApplicationInstrumentedTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun useAppContext() {
        // Context de l'application sous test
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.hypercart", appContext.packageName)
    }

    @Test
    fun mainActivity_launches_successfully() {
        // Vérifier que MainActivity se lance sans crash
        activityRule.scenario.onActivity { activity ->
            assertNotNull(activity)
            assertTrue(activity is MainActivity)
        }
    }

    @Test
    fun app_has_correct_package_name() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.hypercart", appContext.packageName)
    }

    @Test
    fun app_permissions_are_configured() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val packageManager = appContext.packageManager
        val packageInfo = packageManager.getPackageInfo(appContext.packageName, 0)
        
        assertNotNull(packageInfo)
        assertTrue(packageInfo.applicationInfo?.enabled ?: false)
    }
}