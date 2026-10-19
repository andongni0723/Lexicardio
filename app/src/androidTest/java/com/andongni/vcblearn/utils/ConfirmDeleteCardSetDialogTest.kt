package com.andongni.vcblearn.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.andongni.vcblearn.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ConfirmDeleteCardSetDialogTest {
    @get:Rule
    val compose = createComposeRule()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private var confirmed = 0
    private var dismissed = 0

    private fun showDialog(deleting: Boolean = false) {
        compose.setContent {
            MaterialTheme {
                ConfirmDeleteCardSetDialog(
                    visible = true,
                    cardSetName = "Vocabulary",
                    deleting = deleting,
                    onConfirm = { confirmed++ },
                    onDismiss = { dismissed++ }
                )
            }
        }
    }

    @Test
    fun cancelDoesNotDelete() {
        showDialog()
        compose.onNodeWithText(context.getString(R.string.delete_card_set_confirmation, "Vocabulary"))
            .assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.cancel)).performClick()
        compose.runOnIdle {
            assertEquals(0, confirmed)
            assertEquals(1, dismissed)
        }
    }

    @Test
    fun deletionRequiresExplicitConfirmation() {
        showDialog()
        compose.runOnIdle { assertEquals(0, confirmed) }
        compose.onNode(hasText(context.getString(R.string.delete_card_set)) and hasClickAction())
            .performClick()
        compose.runOnIdle { assertEquals(1, confirmed) }
    }

    @Test
    fun buttonsAreDisabledDuringDeletion() {
        showDialog(deleting = true)
        compose.onNodeWithText(context.getString(R.string.cancel)).assertIsNotEnabled()
        compose.onNode(hasText(context.getString(R.string.delete_card_set)) and hasClickAction())
            .assertIsNotEnabled()
    }
}
