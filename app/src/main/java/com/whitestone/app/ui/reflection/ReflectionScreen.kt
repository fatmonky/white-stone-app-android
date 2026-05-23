package com.whitestone.app.ui.reflection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.whitestone.app.data.Reflection
import com.whitestone.app.ui.onboarding.ReflectionsTourOverlay
import com.whitestone.app.ui.theme.BrownAccent
import com.whitestone.app.util.DateHelpers
import com.whitestone.app.util.ReflectionQuestions

private enum class ReflectionMode {
    Today,
    ByQuestion
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflectionScreen(
    onNavigateToReflectionDetail: (String) -> Unit,
    showTourOverlay: Boolean = false,
    onFinishTour: () -> Unit = {},
    onSkipTour: () -> Unit = {},
    viewModel: ReflectionViewModel = hiltViewModel()
) {
    var mode by rememberSaveable { mutableStateOf(ReflectionMode.Today) }
    var expandedQuestionIndex by rememberSaveable { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reflections") },
                actions = {
                    IconButton(
                        enabled = !showTourOverlay,
                        onClick = {
                            mode = if (mode == ReflectionMode.Today) {
                                ReflectionMode.ByQuestion
                            } else {
                                ReflectionMode.Today
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (mode == ReflectionMode.Today) {
                                Icons.AutoMirrored.Filled.MenuBook
                            } else {
                                Icons.Filled.Edit
                            },
                            contentDescription = if (mode == ReflectionMode.Today) {
                                "Questions View"
                            } else {
                                "Daily View"
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (mode) {
                ReflectionMode.Today -> ReflectionTodayScreen(
                    viewModel = viewModel,
                    onOpenQuestionHistory = { index ->
                        expandedQuestionIndex = index
                        mode = ReflectionMode.ByQuestion
                    }
                )

                ReflectionMode.ByQuestion -> ByQuestionScreen(
                    viewModel = viewModel,
                    expandedQuestionIndex = expandedQuestionIndex,
                    onExpandedQuestionChange = { expandedQuestionIndex = it },
                    onNavigateToReflectionDetail = onNavigateToReflectionDetail
                )
            }

            ReflectionsTourOverlay(
                visible = showTourOverlay,
                onFinishTour = onFinishTour,
                onSkipTour = onSkipTour
            )
        }
    }
}

@Composable
private fun ReflectionTodayScreen(
    viewModel: ReflectionViewModel,
    onOpenQuestionHistory: (Int) -> Unit
) {
    val todayReflection by viewModel.todayReflection.collectAsState(initial = null)
    val previousCount by viewModel.previousCount.collectAsState()
    var responseText by rememberSaveable { mutableStateOf("") }
    var lastSavedAt by rememberSaveable { mutableStateOf<Long?>(null) }
    val focusManager = LocalFocusManager.current
    val todayDate = DateHelpers.dateFromDayKey(viewModel.todayDayKey)

    LaunchedEffect(todayReflection?.id) {
        responseText = todayReflection?.responseText.orEmpty()
        lastSavedAt = todayReflection?.updatedAt
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = todayDate?.let { DateHelpers.fullDateString(it) } ?: viewModel.todayDayKey,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = viewModel.todayQuestion.second,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            if (previousCount > 0) {
                Text(
                    text = "you've reflected on this question $previousCount ${if (previousCount == 1) "time" else "times"} before.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BrownAccent,
                    modifier = Modifier.clickable { onOpenQuestionHistory(viewModel.todayQuestion.first) }
                )
            }
        }

        OutlinedTextField(
            value = responseText,
            onValueChange = { responseText = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            placeholder = { Text("Take your time. There's no need to write anything.") },
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        Text(
            text = "Each date keeps one reflection. Saving again will overwrite the previous save for that date.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.saveToday(responseText)
                    lastSavedAt = System.currentTimeMillis()
                }
            ) {
                Text("Save")
            }

            lastSavedAt?.let {
                Text(
                    text = "Saved at ${DateHelpers.timeString(it)}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            }
        }

        ReflectionAttribution()
    }
}

@Composable
private fun ByQuestionScreen(
    viewModel: ReflectionViewModel,
    expandedQuestionIndex: Int?,
    onExpandedQuestionChange: (Int?) -> Unit,
    onNavigateToReflectionDetail: (String) -> Unit
) {
    val allReflections by viewModel.allReflections.collectAsState(initial = emptyList())
    val grouped = remember(allReflections) { allReflections.groupBy { it.questionIndex } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { ReflectionAttribution() }

        ReflectionQuestions.questions.forEachIndexed { index, question ->
            val reflections = grouped[index].orEmpty()
            item(key = "question-$index") {
                QuestionHeader(
                    question = question,
                    count = reflections.size,
                    expanded = expandedQuestionIndex == index,
                    onClick = {
                        if (reflections.isNotEmpty()) {
                            onExpandedQuestionChange(if (expandedQuestionIndex == index) null else index)
                        }
                    }
                )
            }
            if (expandedQuestionIndex == index) {
                items(reflections, key = { it.id }) { reflection ->
                    ReflectionListItem(
                        reflection = reflection,
                        onClick = { onNavigateToReflectionDetail(reflection.dayKey) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionHeader(
    question: String,
    count: Int,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (count == 0) {
                    "no reflections yet on this question."
                } else {
                    "$count ${if (count == 1) "reflection" else "reflections"}."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (count > 0) {
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReflectionListItem(
    reflection: Reflection,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 12.dp, top = 8.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = DateHelpers.dateFromDayKey(reflection.dayKey)?.let { DateHelpers.fullDateString(it) }
                ?: reflection.dayKey,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = reflection.responseText,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflectionDetailScreen(
    dayKey: String,
    onNavigateBack: () -> Unit,
    viewModel: ReflectionViewModel = hiltViewModel()
) {
    var currentDayKey by rememberSaveable(dayKey) { mutableStateOf(dayKey) }
    val currentReflection by viewModel.reflectionForDay(currentDayKey).collectAsState(initial = null)
    val questionIndex = currentReflection?.questionIndex ?: 0
    val siblings by viewModel.reflectionsForQuestion(questionIndex).collectAsState(initial = emptyList())
    var editedText by rememberSaveable(dayKey) { mutableStateOf("") }

    LaunchedEffect(currentReflection?.id, currentDayKey) {
        editedText = currentReflection?.responseText.orEmpty()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reflection") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            currentReflection?.let {
                                viewModel.save(it.dayKey, it.questionIndex, editedText)
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        val active = currentReflection
        if (active == null) {
            Text(
                text = "Reflection not found",
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            )
        } else {
            val dateText = DateHelpers.dateFromDayKey(active.dayKey)?.let { DateHelpers.fullDateString(it) }
                ?: active.dayKey
            val currentIndex = siblings.indexOfFirst { it.dayKey == active.dayKey }
            val previous = currentIndex.takeIf { it > 0 }?.let { siblings[it - 1] }
            val next = currentIndex.takeIf { it >= 0 && it < siblings.lastIndex }?.let { siblings[it + 1] }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Last saved ${DateHelpers.fullDateString(active.updatedAt)} ${DateHelpers.timeString(active.updatedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = ReflectionQuestions.questions.getOrNull(active.questionIndex).orEmpty(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = {
                            currentReflection?.let { viewModel.save(it.dayKey, it.questionIndex, editedText) }
                            previous?.let { currentDayKey = it.dayKey }
                        },
                        enabled = previous != null
                    ) {
                        Text("previous on this question")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            currentReflection?.let { viewModel.save(it.dayKey, it.questionIndex, editedText) }
                            next?.let { currentDayKey = it.dayKey }
                        },
                        enabled = next != null
                    ) {
                        Text("next on this question")
                    }
                }
            }
        }
    }
}

@Composable
fun DayReflectionCard(
    reflection: Reflection,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = ReflectionQuestions.questions.getOrNull(reflection.questionIndex).orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = reflection.responseText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ReflectionAttribution(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = ReflectionQuestions.attributionPrefix + ReflectionQuestions.attributionLinkText,
            style = MaterialTheme.typography.bodySmall,
            color = BrownAccent
        )
        Text(
            text = ReflectionQuestions.attributionSuffix,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
