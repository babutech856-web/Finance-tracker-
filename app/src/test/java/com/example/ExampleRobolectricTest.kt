package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CurrencyConfig
import com.example.data.model.TransactionType
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Finance Tracker", appName)
    }

    @Test
    fun `nepali currency formatting test`() {
        val amount = 120000.0
        val formatted = CurrencyFormatter.format(amount, CurrencyConfig.NPR)
        assertEquals("Rs. 1,20,000", formatted)

        val smallAmount = 450.0
        val formattedSmall = CurrencyFormatter.format(smallAmount, CurrencyConfig.NPR)
        assertEquals("Rs. 450", formattedSmall)

        val lakhAmount = 1000000.0
        val formattedLakh = CurrencyFormatter.format(lakhAmount, CurrencyConfig.NPR)
        assertEquals("Rs. 10,00,000", formattedLakh)
    }

    @Test
    fun `viewmodel initializes and opens bottom sheet`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = FinanceViewModel(app)
        assertNotNull(vm.uiState.value)
        vm.openAddTransactionSheet(TransactionType.EXPENSE)
        // Verify sheet trigger
        assertEquals(TransactionType.EXPENSE, vm.uiState.value.prefillType)
    }
}


