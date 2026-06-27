package com.example.overloadtracker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.overloadtracker.data.RoutineExercise
import com.example.overloadtracker.data.WorkoutSet
import kotlinx.coroutines.launch

class ActiveSetState {
    var weight by mutableStateOf("")
    var reps by mutableStateOf("")
}

class ActiveExerciseState(val exercise: RoutineExercise) {
    val sets = mutableStateListOf(ActiveSetState())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    viewModel: WorkoutViewModel,
    sessionId: Int,
    onNavigateBack: () -> Unit
) {
    val routines by viewModel.routines.collectAsState()

    var currentSessionId by remember { mutableStateOf(sessionId) }
    var selectedSplit by remember { mutableStateOf<String?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showProgramDialog by remember { mutableStateOf(false) }
    val activeExercises = remember { mutableStateListOf<ActiveExerciseState>() }

    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (currentSessionId == -1) {
            currentSessionId = viewModel.createNewSession()
            isLoading = false
        } else {
            val sessionData = viewModel.getSessionWithSetsOnce(currentSessionId)
            if (sessionData != null && sessionData.sets.isNotEmpty()) {
                val firstExName = sessionData.sets.first().exerciseName
                val detectedSplit = routines.find { it.exercises.any { ex -> ex.name == firstExName } }?.routine?.name ?: "Antrenman"
                selectedSplit = detectedSplit

                val groupedSets = sessionData.sets.groupBy { it.exerciseName }
                groupedSets.forEach { (exerciseName, sets) ->
                    val exerciseDef = routines.flatMap { it.exercises }.find { it.name == exerciseName }
                        ?: RoutineExercise(name = exerciseName, routineId = 0, setsAndReps = "-", restTime = "-")

                    val activeExercise = ActiveExerciseState(exerciseDef)
                    activeExercise.sets.clear()

                    sets.sortedBy { it.setNumber }.forEach { dbSet ->
                        val activeSet = ActiveSetState().apply {
                            weight = if (dbSet.weight > 0) dbSet.weight.toString() else ""
                            reps = if (dbSet.reps > 0) dbSet.reps.toString() else ""
                        }
                        activeExercise.sets.add(activeSet)
                    }
                    activeExercises.add(activeExercise)
                }
            }
            isLoading = false
        }
    }

    val saveProgressAndExit = { isFinished: Boolean ->
        coroutineScope.launch {
            val currentSetsToSave = activeExercises.flatMap { exState ->
                exState.sets.filter { it.weight.isNotBlank() && it.reps.isNotBlank() }.mapIndexed { index, set ->
                    WorkoutSet(
                        sessionId = currentSessionId,
                        exerciseName = exState.exercise.name,
                        weight = set.weight.toDoubleOrNull() ?: 0.0,
                        reps = set.reps.toIntOrNull() ?: 0,
                        setNumber = index + 1
                    )
                }
            }
            viewModel.saveProgress(currentSessionId, currentSetsToSave, isFinished)
            onNavigateBack()
        }
    }

    BackHandler { saveProgressAndExit(false) }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    if (selectedSplit == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Antrenman Türü Seç", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { saveProgressAndExit(false) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                routines.forEach { routineData ->
                    Button(
                        onClick = {
                            selectedSplit = routineData.routine.name
                            routineData.exercises.forEach { exercise ->
                                activeExercises.add(ActiveExerciseState(exercise))
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(80.dp).padding(vertical = 8.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(text = "${routineData.routine.name} GÜNÜ", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$selectedSplit Antrenmanı", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { saveProgressAndExit(false) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kaydet ve Çık")
                    }
                },
                actions = {
                    TextButton(onClick = { showProgramDialog = true }) {
                        Text("Egzersizler", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showBottomSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Egzersiz Ekle") },
                text = { Text("Hareket Ekle") },
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            items(activeExercises) { exerciseState ->
                ActiveExerciseCard(
                    exerciseState = exerciseState,
                    viewModel = viewModel,
                    onAddSetClick = { exerciseState.sets.add(ActiveSetState()) },
                    onRemoveExerciseClick = { activeExercises.remove(exerciseState) }
                )
            }

            if (activeExercises.isNotEmpty()) {
                item {
                    Button(
                        onClick = { saveProgressAndExit(true) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp).height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("ANTRENMANI BİTİR", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp)) {
                Text("$selectedSplit Hareketleri", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                val currentExercises = routines.find { it.routine.name == selectedSplit }?.exercises ?: emptyList()

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(currentExercises) { exercise ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (activeExercises.none { it.exercise.name == exercise.name }) {
                                    activeExercises.add(ActiveExerciseState(exercise))
                                }
                                showBottomSheet = false
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(text = exercise.name, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showProgramDialog) {
        AlertDialog(
            onDismissRequest = { showProgramDialog = false },
            title = { Text("$selectedSplit Programı", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val programExercises = routines.find { it.routine.name == selectedSplit }?.exercises ?: emptyList()
                    items(programExercises) { ex ->
                        Column {
                            Text(text = "• ${ex.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Set: ${ex.setsAndReps}  |  Dinlenme: ${ex.restTime}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProgramDialog = false }) { Text("Kapat") }
            }
        )
    }
}

@Composable
fun ActiveExerciseCard(
    exerciseState: ActiveExerciseState,
    viewModel: WorkoutViewModel,
    onAddSetClick: () -> Unit,
    onRemoveExerciseClick: () -> Unit
) {
    var previousRecord by remember { mutableStateOf<WorkoutSet?>(null) }

    LaunchedEffect(exerciseState.exercise.name) {
        previousRecord = viewModel.getPreviousRecord(exerciseState.exercise.name)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = exerciseState.exercise.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onRemoveExerciseClick) {
                    Icon(Icons.Default.Close, contentDescription = "Hareketi Sil", tint = MaterialTheme.colorScheme.secondary)
                }
            }

            AssistChip(
                onClick = { },
                label = { Text(exerciseState.exercise.restTime) },
                leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp)) },
                colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SET", modifier = Modifier.weight(0.4f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                Text("KG", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                Text("TEKRAR", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.weight(0.4f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            exerciseState.sets.forEachIndexed { index, setState ->
                SetRow(
                    displaySetNumber = index + 1,
                    setState = setState,
                    previousRecord = previousRecord,
                    onRemoveSetClick = { exerciseState.sets.remove(setState) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onAddSetClick, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Set Ekle")
            }
        }
    }
}

@Composable
fun SetRow(
    displaySetNumber: Int,
    setState: ActiveSetState,
    previousRecord: WorkoutSet?,
    onRemoveSetClick: () -> Unit
) {
    val weightPlaceholder = previousRecord?.weight?.toString() ?: "-"
    val repsPlaceholder = previousRecord?.reps?.toString() ?: "10"

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "$displaySetNumber", modifier = Modifier.weight(0.4f), fontSize = 18.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = setState.weight,
            onValueChange = { setState.weight = it },
            modifier = Modifier.weight(1.2f),
            placeholder = { Text(weightPlaceholder) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value = setState.reps,
            onValueChange = { setState.reps = it },
            modifier = Modifier.weight(1.2f),
            placeholder = { Text(repsPlaceholder) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        IconButton(onClick = onRemoveSetClick, modifier = Modifier.weight(0.4f)) {
            Icon(Icons.Default.Delete, contentDescription = "Seti Sil", tint = MaterialTheme.colorScheme.error)
        }
    }
}