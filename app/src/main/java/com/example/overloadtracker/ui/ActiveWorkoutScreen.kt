package com.example.overloadtracker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onNavigateBack: () -> Unit,
    onNavigateToExerciseDetail: (String) -> Unit
) {
    val routines by viewModel.routines.collectAsState()
    val brandRed = PremiumTheme.BrandRed

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
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black), 
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = brandRed) }
        return
    }

    if (selectedSplit == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ANTRENMAN SEÇ", fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
                    navigationIcon = {
                        IconButton(onClick = { saveProgressAndExit(false) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black, titleContentColor = Color.White)
                )
            },
            containerColor = Color.Black
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
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
                        modifier = Modifier.fillMaxWidth().height(80.dp).padding(vertical = 10.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumTheme.Charcoal, contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Text(text = routineData.routine.name.uppercase(), fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    }
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedSplit?.uppercase() ?: "", fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
                navigationIcon = {
                    IconButton(onClick = { saveProgressAndExit(false) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kaydet ve Çık", tint = Color.White)
                    }
                },
                actions = {
                    TextButton(onClick = { showProgramDialog = true }) {
                        Text("PROGRAM", fontWeight = FontWeight.Black, color = brandRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black, titleContentColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showBottomSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("HAREKET EKLE", fontWeight = FontWeight.Black) },
                containerColor = brandRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            items(activeExercises) { exerciseState ->
                ActiveExercisePremiumCard(
                    exerciseState = exerciseState,
                    brandRed = brandRed,
                    onAddSetClick = { exerciseState.sets.add(ActiveSetState()) },
                    onRemoveExerciseClick = { activeExercises.remove(exerciseState) },
                    onTitleClick = { onNavigateToExerciseDetail(exerciseState.exercise.name) }
                )
            }

            if (activeExercises.isNotEmpty()) {
                item {
                    Button(
                        onClick = { saveProgressAndExit(true) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp).height(64.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = brandRed, contentColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("ANTRENMANI BİTİR", fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            containerColor = PremiumTheme.Charcoal,
            contentColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray.copy(alpha = 0.3f)) }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp)) {
                Text("${selectedSplit?.uppercase()} LİSTESİ", fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(20.dp))

                val currentExercises = routines.find { it.routine.name == selectedSplit }?.exercises ?: emptyList()

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(currentExercises) { exercise ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (activeExercises.none { it.exercise.name == exercise.name }) {
                                    activeExercises.add(ActiveExerciseState(exercise))
                                }
                                showBottomSheet = false
                            },
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(text = exercise.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
            containerColor = PremiumTheme.Charcoal,
            title = { Text("${selectedSplit?.uppercase()} PROGRAMI", fontWeight = FontWeight.Black, color = Color.White) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val programExercises = routines.find { it.routine.name == selectedSplit }?.exercises ?: emptyList()
                    items(programExercises) { ex ->
                        Column {
                            Text(text = "• ${ex.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                text = "Set: ${ex.setsAndReps}  |  Dinlenme: ${ex.restTime}",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProgramDialog = false }) { 
                    Text("KAPAT", fontWeight = FontWeight.Bold, color = brandRed) 
                }
            }
        )
    }
}

@Composable
fun ActiveExercisePremiumCard(
    exerciseState: ActiveExerciseState,
    brandRed: Color,
    onAddSetClick: () -> Unit,
    onRemoveExerciseClick: () -> Unit,
    onTitleClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PremiumTheme.Charcoal),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exerciseState.exercise.name, 
                    fontSize = 20.sp, 
                    fontWeight = FontWeight.Black, 
                    color = Color.White,
                    modifier = Modifier.weight(1f).clickable { onTitleClick() }
                )
                IconButton(onClick = onRemoveExerciseClick) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }

            Surface(
                color = brandRed.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp), tint = brandRed)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = exerciseState.exercise.restTime, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = brandRed)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SET", modifier = Modifier.weight(0.4f), fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.Gray)
                Text("KG", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.Gray)
                Text("TEKRAR", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.weight(0.4f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            exerciseState.sets.forEachIndexed { index, setState ->
                ActiveSetRow(
                    displaySetNumber = index + 1,
                    setState = setState,
                    brandRed = brandRed,
                    onRemoveSetClick = { exerciseState.sets.remove(setState) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onAddSetClick, 
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.textButtonColors(contentColor = brandRed)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("SET EKLE", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun ActiveSetRow(
    displaySetNumber: Int,
    setState: ActiveSetState,
    brandRed: Color,
    onRemoveSetClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "$displaySetNumber", modifier = Modifier.weight(0.4f), fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)

        OutlinedTextField(
            value = setState.weight,
            onValueChange = { setState.weight = it },
            modifier = Modifier.weight(1.2f),
            placeholder = { Text("-", color = Color.Gray) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedBorderColor = brandRed,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        OutlinedTextField(
            value = setState.reps,
            onValueChange = { setState.reps = it },
            modifier = Modifier.weight(1.2f),
            placeholder = { Text("-", color = Color.Gray) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedBorderColor = brandRed,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        IconButton(onClick = onRemoveSetClick, modifier = Modifier.weight(0.4f)) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White.copy(alpha = 0.1f), modifier = Modifier.size(18.dp))
        }
    }
}
