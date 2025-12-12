package com.example.smartledger.ui.screens

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.smartledger.data.Language
import com.example.smartledger.data.LedgerRepository
import com.example.smartledger.data.SettingsRepository
import com.example.smartledger.data.ThemeMode
import com.example.smartledger.data.TransactionType
import com.example.smartledger.domain.MessageProcessor
import com.example.smartledger.ui.components.LedgerCard
import com.example.smartledger.ui.inbox.rememberInboxViewModel
import com.example.smartledger.ui.methods.rememberMethodsViewModel
import com.example.smartledger.ui.settings.rememberSettingsViewModel
import com.example.smartledger.ui.stats.rememberStatsViewModel
import com.example.smartledger.ui.screens.components.BudgetBar
import com.example.smartledger.ui.screens.components.PieChartSection
import com.example.smartledger.ui.screens.components.LineChartSection
import com.example.smartledger.ui.transactions.rememberAddTransactionViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Composable
fun HomeScreen(
    repository: LedgerRepository,
    processor: MessageProcessor,
    onNavigateInbox: () -> Unit
) {
    val statsVm = rememberStatsViewModel(repository)
    val stats by statsVm.state.collectAsState()
    val inboxVm = rememberInboxViewModel(processor)
    val pending by inboxVm.pending.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("概览 / Overview", style = MaterialTheme.typography.titleMedium)
        LedgerCard {
            Text("本月收入：${"%.2f".format(stats.monthIncome)}")
            Text("本月支出：${"%.2f".format(stats.monthExpense)}")
            Text("结余：${"%.2f".format(stats.balance)}")
        }
        LedgerCard {
            Text("近期流水", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            if (stats.recent.isEmpty()) {
                Text("暂无流水")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    stats.recent.forEach { tx ->
                        Text("${tx.type}  ¥${"%.2f".format(tx.amount)}  备注:${tx.note}")
                    }
                }
            }
        }
        LedgerCard {
            Text("待确认短信/通知", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            if (pending.isEmpty()) {
                Text("暂无待确认")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    pending.forEach { p ->
                        Text("${p.channel}: ${p.text.take(40)}")
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onNavigateInbox) {
                        Text("去处理")
                    }
                }
            }
        }
    }
}

@Composable
fun AddTransactionScreen(repository: LedgerRepository) {
    val vm = rememberAddTransactionViewModel(repository)
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("记一笔", style = MaterialTheme.typography.titleMedium)
        LedgerCard {
            Text("收支类型")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TransactionType.values().forEach { type ->
                    FilterChip(
                        selected = state.type == type,
                        onClick = { vm.setType(type) },
                        label = { Text(if (type == TransactionType.Expense) "支出" else "收入") }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.amount,
                onValueChange = vm::setAmount,
                label = { Text("金额") },
                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(Modifier.height(8.dp))
            Text("类别")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.categories.take(4).forEach { cat ->
                    FilterChip(
                        selected = state.selectedCategoryId == cat.id,
                        onClick = { vm.selectCategory(cat.id) },
                        label = { Text(cat.name) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("支付/收款方式")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.methods.take(4).forEach { method ->
                    FilterChip(
                        selected = state.selectedMethodId == method.id,
                        onClick = { vm.selectMethod(method.id) },
                        label = { Text(method.name) }
                    )
                }
            }
            if (state.cards.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("卡片")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.cards.forEach { card ->
                        FilterChip(
                            selected = state.selectedCardId == card.id,
                            onClick = { vm.selectCard(card.id) },
                            label = { Text(card.label) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("日期")
            Button(onClick = {
                val now = state.occurredAt
                val picker = android.app.DatePickerDialog(
                    context,
                    { _, y, m, d ->
                        val newDate = LocalDate.of(y, m + 1, d)
                        val newDateTime = LocalDateTime.of(newDate, now.toLocalTime())
                        vm.setDate(newDateTime)
                    },
                    state.occurredAt.year,
                    state.occurredAt.monthValue - 1,
                    state.occurredAt.dayOfMonth
                )
                picker.show()
            }) {
                Text(state.occurredAt.toLocalDate().toString())
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.note,
                onValueChange = vm::setNote,
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { vm.save { } },
                enabled = !state.saving
            ) {
                Text(if (state.saving) "保存中…" else "保存")
            }
            if (state.error != null) {
                Spacer(Modifier.height(4.dp))
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun StatsScreen(repository: LedgerRepository) {
    val vm = rememberStatsViewModel(repository)
    val state by vm.state.collectAsState()
    val budgetInput = state.budgetInput
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("统计", style = MaterialTheme.typography.titleMedium)
        LedgerCard {
            Text("本月支出：${"%.2f".format(state.monthExpense)}")
            Text("本月收入：${"%.2f".format(state.monthIncome)}")
            Text("结余：${"%.2f".format(state.balance)}")
            Spacer(Modifier.height(8.dp))
            BudgetBar(spent = state.spent, budget = state.budgetAmount)
            if (state.overspent) {
                Spacer(Modifier.height(4.dp))
                Text("已超支", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = budgetInput,
                onValueChange = vm::setBudgetInput,
                label = { Text("本月预算") },
                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Button(onClick = { vm.saveBudget() }) {
                Text("保存本月预算")
            }
            Spacer(Modifier.height(12.dp))
            Text("支出类别占比（饼图）")
            PieChartSection(data = state.pieData, modifier = Modifier
                .fillMaxWidth()
                .height(220.dp))
            Spacer(Modifier.height(12.dp))
            Text("按支付方式拆分")
            PieChartSection(data = state.methodPie, modifier = Modifier
                .fillMaxWidth()
                .height(220.dp))
            Spacer(Modifier.height(12.dp))
            Text("本月支出趋势")
            LineChartSection(points = state.trendPoints, modifier = Modifier
                .fillMaxWidth()
                .height(240.dp))
        }
    }
}

@Composable
fun MethodsScreen(repository: LedgerRepository) {
    val vm = rememberMethodsViewModel(repository)
    val state by vm.state.collectAsState()
    val newMethod = remember { mutableStateOf("") }
    val newMethodLead = remember { mutableStateOf("3") }
    val newCardLabel = remember { mutableStateOf("") }
    val newCardLead = remember { mutableStateOf("3") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("支付方式 & 卡片", style = MaterialTheme.typography.titleMedium)
        LedgerCard {
            Text("新增方式")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = newMethod.value,
                onValueChange = { newMethod.value = it },
                label = { Text("方式名称") }
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = newMethodLead.value,
                onValueChange = { newMethodLead.value = it },
                label = { Text("提前提醒天数(信用方式)") },
                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                if (newMethod.value.isNotBlank()) {
                    val lead = newMethodLead.value.toIntOrNull()
                    vm.addMethod(newMethod.value, isCredit = false, billDay = null, dueDay = null, repayLeadDays = lead)
                    newMethod.value = ""
                }
            }) { Text("添加") }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.methods) { method ->
                LedgerCard {
                    Text("${method.name}${if (method.isCredit) "（信用）" else ""}",
                        style = MaterialTheme.typography.titleMedium)
                    val cards = state.cardsByMethod[method.id].orEmpty()
                    if (cards.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("卡片：")
                        cards.forEach { card ->
                            Text("- ${card.label} 账单日:${card.billDay ?: "--"} 还款日:${card.dueDay ?: "--"}")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCardLabel.value,
                        onValueChange = { newCardLabel.value = it },
                        label = { Text("新增卡片标签") }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCardLead.value,
                        onValueChange = { newCardLead.value = it },
                        label = { Text("提前提醒天数") },
                        keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        if (newCardLabel.value.isNotBlank()) {
                            val lead = newCardLead.value.toIntOrNull()
                            vm.addCard(method.id, newCardLabel.value, method.billDay, method.dueDay, lead)
                            newCardLabel.value = ""
                        }
                    }) { Text("添加卡片") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(settingsRepository: SettingsRepository) {
    val viewModel = rememberSettingsViewModel(settingsRepository)
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("设置 / Settings", style = MaterialTheme.typography.titleMedium)

        LedgerCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text("通知监听", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "用于解析微信/支付宝通知，需在系统里授予访问权限",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = state.notificationListenerEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.toggleNotificationListener(enabled)
                        if (enabled) {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        }
                    }
                )
            }
        }

        LedgerCard {
            Text("主题模式", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.values().forEach { mode ->
                    FilterChip(
                        selected = state.themeMode == mode,
                        onClick = { viewModel.setTheme(mode) },
                        label = { Text(modeLabel(mode)) }
                    )
                }
            }
        }

        LedgerCard {
            Text("语言 / Language", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Language.values().forEach { lang ->
                    FilterChip(
                        selected = state.language == lang,
                        onClick = { viewModel.setLanguage(lang) },
                        label = { Text(languageLabel(lang)) }
                    )
                }
            }
        }

        LedgerCard {
            Text("还款提醒提前天数（占位）", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Slider(
                value = state.repayLeadDays.toFloat(),
                onValueChange = { viewModel.setRepayLeadDays(it.toInt()) },
                valueRange = 0f..7f,
                steps = 6
            )
            Text("${state.repayLeadDays} 天（默认 3 天 + 当日）", style = MaterialTheme.typography.bodySmall)
        }

        LedgerCard {
            Text("预算与备份（占位）", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { /* TODO */ }) { Text("CSV 导出（占位）") }
        }
    }
}

private fun modeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.Light -> "亮"
    ThemeMode.Dark -> "暗"
    ThemeMode.System -> "跟随系统"
}

private fun languageLabel(lang: Language): String = when (lang) {
    Language.ZhHans -> "简体"
    Language.En -> "English"
    Language.ZhHant -> "繁體"
    Language.System -> "系统"
}

