package com.example.smartledger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.smartledger.data.LedgerRepository
import com.example.smartledger.data.TransactionType
import com.example.smartledger.domain.MessageProcessor
import com.example.smartledger.ui.components.LedgerCard
import com.example.smartledger.ui.inbox.rememberInboxViewModel

@Composable
fun InboxScreen(
    processor: MessageProcessor,
    repository: LedgerRepository,
    onBack: () -> Unit = {}
) {
    val vm = rememberInboxViewModel(processor, repository)
    val state by vm.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("待确认消息", style = MaterialTheme.typography.titleMedium)
        if (state.pending.isEmpty()) {
            LedgerCard { Text("暂无待确认消息") }
            return@Column
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.pending) { pending ->
                val type = state.selectedType[pending.id] ?: TransactionType.Expense
                val defaultMethod = state.selectedMethod[pending.id] ?: state.methods.firstOrNull()?.id ?: 1L
                val cards = state.cardsByMethod[defaultMethod].orEmpty()
                val selectedCard = state.selectedCard[pending.id] ?: cards.firstOrNull()?.id
                val categories = if (type == TransactionType.Expense) state.expenseCategories else state.incomeCategories
                val selectedCat = state.selectedCategory[pending.id] ?: categories.firstOrNull()?.id ?: 1L
                LedgerCard {
                    Text("${pending.channel}：${pending.text}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("收支类型")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TransactionType.values().forEach { type ->
                            FilterChip(
                                selected = (state.selectedType[pending.id] ?: TransactionType.Expense) == type,
                                onClick = { vm.setType(pending.id, type) },
                                label = { Text(if (type == TransactionType.Expense) "支出" else "收入") }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.messageAmount[pending.id] ?: "",
                        onValueChange = { vm.setAmount(pending.id, it) },
                        label = { Text("金额") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("关键词（用于匹配规则）")
                    OutlinedTextField(
                        value = state.messageKeywords[pending.id] ?: "",
                        onValueChange = { vm.setKeywords(pending.id, it) },
                        label = { Text("以逗号分隔") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("类别（默认取首个同类型）")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.take(4).forEach { cat ->
                            FilterChip(
                                selected = selectedCat == cat.id,
                                onClick = { vm.setCategory(pending.id, cat.id) },
                                label = { Text(cat.name) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("方式（默认取首个）")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.methods.take(4).forEach { method ->
                            FilterChip(
                                selected = method.id == defaultMethod,
                                onClick = { vm.setMethod(pending.id, method.id) },
                                label = { Text(method.name) }
                            )
                        }
                    }
                    if (cards.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("卡片（默认首个）")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            cards.take(3).forEach { card ->
                                FilterChip(
                                    selected = selectedCard == card.id,
                                    onClick = { vm.setCard(pending.id, card.id) },
                                    label = { Text(card.label) }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            vm.confirm(
                                pending = pending,
                                fallbackType = type,
                                fallbackCategory = selectedCat,
                                fallbackMethod = defaultMethod,
                                fallbackCard = selectedCard
                            )
                        },
                        enabled = !state.savingIds.contains(pending.id)
                    ) {
                        Text(if (state.savingIds.contains(pending.id)) "保存中..." else "保存规则并入账")
                    }
                    state.error?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

private fun categoryIdForType(state: com.example.smartledger.ui.inbox.InboxUiState, type: TransactionType): Long {
    val list = if (type == TransactionType.Expense) state.expenseCategories else state.incomeCategories
    return list.firstOrNull()?.id ?: 1L
}

private fun defaultMethod(state: com.example.smartledger.ui.inbox.InboxUiState): Long {
    return state.methods.firstOrNull()?.id ?: 1L
}

private fun defaultCard(state: com.example.smartledger.ui.inbox.InboxUiState, methodId: Long): Long? {
    return state.cardsByMethod[methodId]?.firstOrNull()?.id
}

