package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IndigoAccent
import com.example.ui.viewmodel.FinanceUiState
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.DateTimeUtils
import com.example.util.IconMapper
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    uiState: FinanceUiState,
    viewModel: FinanceViewModel,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val editing = uiState.editingTransaction

    var selectedType by remember { mutableStateOf(editing?.type ?: uiState.prefillType) }
    var amountInput by remember { mutableStateOf(editing?.let { if (it.amount % 1 == 0.0) it.amount.toLong().toString() else it.amount.toString() } ?: "") }
    var descriptionInput by remember { mutableStateOf(editing?.description ?: "") }
    var selectedCategoryId by remember { mutableStateOf(editing?.categoryId ?: "") }
    var selectedAccount by remember { mutableStateOf(editing?.fromAccount ?: (uiState.accounts.firstOrNull()?.name ?: "Cash in Hand")) }
    var toAccount by remember { mutableStateOf(editing?.toAccount ?: (uiState.accounts.getOrNull(1)?.name ?: "Nabil Bank")) }
    var dateMillis by remember { mutableLongStateOf(editing?.dateMillis ?: System.currentTimeMillis()) }
    var notesInput by remember { mutableStateOf(editing?.notes ?: "") }

    var accountDropdownExpanded by remember { mutableStateOf(false) }
    var toAccountDropdownExpanded by remember { mutableStateOf(false) }

    // Synchronize category list for selected type
    val matchingCategories = uiState.categories.filter { it.type == selectedType }

    LaunchedEffect(selectedType, matchingCategories) {
        if (selectedCategoryId.isBlank() || matchingCategories.none { it.id == selectedCategoryId }) {
            selectedCategoryId = matchingCategories.firstOrNull()?.id ?: ""
        }
    }

    ModalBottomSheet(
        onDismissRequest = { viewModel.closeAddTransactionSheet() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("add_transaction_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Sheet Header & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (editing != null) "Edit Transaction" else "Add Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = { viewModel.closeAddTransactionSheet() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Transaction Type Tab Selector (Expense | Income | Transfer)
            val types = listOf(TransactionType.EXPENSE, TransactionType.INCOME, TransactionType.TRANSFER)
            val selectedTabIndex = types.indexOf(selectedType).coerceAtLeast(0)

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = when (selectedType) {
                            TransactionType.EXPENSE -> ExpenseRed
                            TransactionType.INCOME -> IncomeGreen
                            TransactionType.TRANSFER -> IndigoAccent
                        },
                        height = 3.dp
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(14.dp))
            ) {
                types.forEach { type ->
                    val isSelected = selectedType == type
                    val tabColor = when (type) {
                        TransactionType.EXPENSE -> ExpenseRed
                        TransactionType.INCOME -> IncomeGreen
                        TransactionType.TRANSFER -> IndigoAccent
                    }
                    Tab(
                        selected = isSelected,
                        onClick = { selectedType = type },
                        text = {
                            Text(
                                text = when (type) {
                                    TransactionType.EXPENSE -> "Expense"
                                    TransactionType.INCOME -> "Income"
                                    TransactionType.TRANSFER -> "Transfer"
                                },
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) tabColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Large Amount Input Display
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Enter Amount (${uiState.currency.symbol.trim()})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.currency.symbol.trim() + " ",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (selectedType) {
                                TransactionType.EXPENSE -> ExpenseRed
                                TransactionType.INCOME -> IncomeGreen
                                TransactionType.TRANSFER -> IndigoAccent
                            }
                        )

                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { input ->
                                if (input.count { it == '.' } <= 1 && input.all { it.isDigit() || it == '.' }) {
                                    amountInput = input
                                }
                            },
                            placeholder = { Text("0", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .width(200.dp)
                                .testTag("amount_input_field")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Category Selector (Only for Income & Expense)
            if (selectedType != TransactionType.TRANSFER) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    matchingCategories.forEach { cat ->
                        val isSelected = selectedCategoryId == cat.id
                        val catColor = Color(cat.color)

                        Surface(
                            onClick = { selectedCategoryId = cat.id },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) catColor else MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = if (isSelected) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = IconMapper.getIcon(cat.icon),
                                    contentDescription = cat.name,
                                    tint = if (isSelected) Color.White else catColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // "+ Category" Chip
                    Surface(
                        onClick = { viewModel.setCategoryDialogOpen(true) },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 4. Description Field
            OutlinedTextField(
                value = descriptionInput,
                onValueChange = { descriptionInput = it },
                label = { Text("Description / Merchant") },
                placeholder = {
                    Text(
                        when (selectedType) {
                            TransactionType.EXPENSE -> "e.g. Lunch at cafe, Fuel, Grocery"
                            TransactionType.INCOME -> "e.g. Salary, Freelance project, Bonus"
                            TransactionType.TRANSFER -> "e.g. ATM withdrawal, Wallet topup"
                        }
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("description_input_field")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Account Selector (From Account & To Account)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // From Account Dropdown
                ExposedDropdownMenuBox(
                    expanded = accountDropdownExpanded,
                    onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedAccount,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (selectedType == TransactionType.TRANSFER) "From Account" else "Payment Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = accountDropdownExpanded,
                        onDismissRequest = { accountDropdownExpanded = false }
                    ) {
                        uiState.accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.name) },
                                onClick = {
                                    selectedAccount = acc.name
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // If Transfer: To Account Dropdown
                if (selectedType == TransactionType.TRANSFER) {
                    ExposedDropdownMenuBox(
                        expanded = toAccountDropdownExpanded,
                        onExpandedChange = { toAccountDropdownExpanded = !toAccountDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = toAccount,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("To Account") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toAccountDropdownExpanded) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = toAccountDropdownExpanded,
                            onDismissRequest = { toAccountDropdownExpanded = false }
                        ) {
                            uiState.accounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text(acc.name) },
                                    onClick = {
                                        toAccount = acc.name
                                        toAccountDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Date & Time Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Date Picker Button
                Surface(
                    onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                cal.set(Calendar.YEAR, year)
                                cal.set(Calendar.MONTH, month)
                                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                dateMillis = cal.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = DateTimeUtils.formatDate(dateMillis),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Time Picker Button
                Surface(
                    onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                cal.set(Calendar.MINUTE, minute)
                                dateMillis = cal.timeInMillis
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            false
                        ).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = DateTimeUtils.formatTime(dateMillis),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7. Optional Notes
            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                label = { Text("Notes (Optional)") },
                placeholder = { Text("Add any tags or details") },
                maxLines = 2,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Helper to execute save
            fun doSave(keepOpen: Boolean) {
                val amount = amountInput.toDoubleOrNull() ?: 0.0
                if (amount <= 0.0) return

                val category = if (selectedType == TransactionType.TRANSFER) {
                    CategoryEntity("transfer", "Transfer", TransactionType.TRANSFER, "swap_horiz", 0xFF6366F1)
                } else {
                    uiState.categories.find { it.id == selectedCategoryId }
                        ?: matchingCategories.firstOrNull()
                        ?: CategoryEntity("other", "General", selectedType, "category", 0xFF6B7280)
                }

                val desc = if (descriptionInput.isNotBlank()) descriptionInput else category.name

                viewModel.saveTransaction(
                    type = selectedType,
                    amount = amount,
                    categoryId = category.id,
                    categoryName = category.name,
                    categoryIcon = category.icon,
                    categoryColor = category.color,
                    description = desc,
                    fromAccount = selectedAccount,
                    toAccount = if (selectedType == TransactionType.TRANSFER) toAccount else null,
                    dateMillis = dateMillis,
                    notes = notesInput,
                    keepOpenForNext = keepOpen
                )

                if (keepOpen) {
                    amountInput = ""
                    descriptionInput = ""
                    notesInput = ""
                }
            }

            // 8. Action Buttons
            val primaryActionColor = when (selectedType) {
                TransactionType.EXPENSE -> ExpenseRed
                TransactionType.INCOME -> IncomeGreen
                TransactionType.TRANSFER -> IndigoAccent
            }

            Button(
                onClick = { doSave(keepOpen = false) },
                enabled = (amountInput.toDoubleOrNull() ?: 0.0) > 0.0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_transaction_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryActionColor)
            ) {
                Text(
                    text = if (editing != null) "Update Transaction" else "Save Transaction",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Quick Sequential Entry: "Save & Add Another" (only when creating new)
            if (editing == null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { doSave(keepOpen = true) },
                    enabled = (amountInput.toDoubleOrNull() ?: 0.0) > 0.0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_and_add_another_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Save & Add Another",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                // Delete Option for Existing Transaction
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(editing)
                        viewModel.closeAddTransactionSheet()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Transaction", color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
