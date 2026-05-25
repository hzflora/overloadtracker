package com.example.overloadtracker.ui

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
import com.example.overloadtracker.data.WorkoutSet
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.launch

// --- Antrenman Programı Veritabanı ---
data class ProgramExercise(val name: String, val setsAndReps: String, val restTime: String)

val workoutSplits = mapOf(
    "PUSH" to listOf(
        ProgramExercise("Bench Press", "2+2 x 10", "2.5 - 3 Dakika"),
        ProgramExercise("Incline Press","3 x 10", "2 - 3 Dakika"),
        ProgramExercise("Pec Deck Fly","3 x Failure", "60 - 90 Saniye"),
        ProgramExercise("Shoulder Press","1+3 x Failure", "2.5 - 3 Dakika"),
        ProgramExercise("Lateral Raise","3 x 12", "60 - 90 Saniye"),
        ProgramExercise("Skull Crusher","3 x 10", "60 - 90 Saniye"),
        ProgramExercise("Triceps Pushdown","2 x Drop", "90 Saniye")
    ),
    "PULL" to listOf(
        ProgramExercise("Lat Pulldown", "2+2 x 10", "2 - 3 Dakika"),
        ProgramExercise("Rope Pullover","3 x 10", "60 - 90 Saniye"),
        ProgramExercise("Seated Cable Row","3 x 10", "2 - 3 Dakika"),
        ProgramExercise("T-Bar Row","1+2 x Failure", "2.5 - 3 Dakika"),
        ProgramExercise("Reverse Fly","3 x 10", "60 - 90 Saniye"),
        ProgramExercise("Dumbbell / Cable Curl","3 x Failure", "60 - 90 Saniye"),
        ProgramExercise("Hammer Curl", "3 x 10","60 - 90 Saniye")
    ),
    "LOWER" to listOf(
        ProgramExercise("Leg Press","2+2 x 10", "2.5 - 3 Dakika"),
        ProgramExercise("Hack Squat","2 x 12", "2.5 - 3 Dakika"),
        ProgramExercise("Leg Extension","2 x Failure", "90 Saniye"),
        ProgramExercise("Leg Curl","2 x Failure", "90 Saniye"),
        ProgramExercise("Calf Raise","2 x Failure", "60 - 90 Saniye"),
        ProgramExercise("Karın (Leg Raise / Crunch)","3 x Failure", "60 Saniye")
    ),
    "UPPER" to listOf(
        ProgramExercise("Incline Dumbbell Press","2+2 x Failure", "2.5 - 3 Dakika"),
        ProgramExercise("Pec Deck Fly","3 x Failure", "60 - 90 Saniye"),
        ProgramExercise("Dumbbell Press (Omuz)","2 x Failure", "2.5 - 3 Dakika"),
        ProgramExercise("Lat Pulldown","1+2 x 10", "2 - 3 Dakika"),
        ProgramExercise("Row Alternatifi","1+2 x Failure", "2.5 - 3 Dakika"),
        ProgramExercise("Lateral Raise","3 x 12", "60 - 90 Saniye"),
        ProgramExercise("Triceps Pushdown","3 x 10", "60 - 90 Saniye"),
        ProgramExercise("Dumbbell Curl","3 x 10", "60 - 90 Saniye")
    )
)

// --- Arayüz State Sınıfları ---
class ActiveSetState {
    var weight by mutableStateOf("")
    var reps by mutableStateOf("")
}

class ActiveExerciseState(val exercise: ProgramExercise) {
    val sets = mutableStateListOf(ActiveSetState())
}
// ---------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    viewModel: WorkoutViewModel,
    sessionId: Int,
    onNavigateBack: () -> Unit
) {
    var currentSessionId by remember { mutableStateOf(sessionId) }
    var selectedSplit by remember { mutableStateOf<String?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showProgramDialog by remember { mutableStateOf(false) } // Egzersizleri göreceğimiz pencere için
    val activeExercises = remember { mutableStateListOf<ActiveExerciseState>() }

    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }

    // Ekran açıldığında veritabanı işlemlerini yap
    LaunchedEffect(Unit) {
        if (currentSessionId == -1) {
            // Yeni Antrenman: Arka planda DB'de oluştur ve ID'yi al
            currentSessionId = viewModel.createNewSession()
            isLoading = false
        } else {
            // Var olan Aktif Antrenmanı yükle
            val sessionData = viewModel.getSessionWithSetsOnce(currentSessionId)
            if (sessionData != null && sessionData.sets.isNotEmpty()) {

                // YENİ: İlk hareketten antrenmanın türünü (PUSH, PULL vb.) tespit et
                val firstExName = sessionData.sets.first().exerciseName
                val detectedSplit = workoutSplits.entries.find { it.value.any { ex -> ex.name == firstExName } }?.key ?: "Antrenman"
                selectedSplit = detectedSplit // Artık "Kaldığın Yerden Devam" yerine "PUSH" vs. yazacak

                val groupedSets = sessionData.sets.groupBy { it.exerciseName }
                groupedSets.forEach { (exerciseName, sets) ->
                    val exerciseDef = workoutSplits.values.flatten().find { it.name == exerciseName }
                        ?: ProgramExercise(exerciseName, "-", "-")

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

    // Ortak Kaydetme ve Çıkış Fonksiyonu
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

    // Telefonun sistem geri tuşuna basıldığında ilerlemeyi kaydet (isCompleted = false)
    BackHandler {
        saveProgressAndExit(false)
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
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
                    // BURADA Egzersizler butonu OLMAYACAK
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                workoutSplits.keys.forEach { splitName ->
                    Button(
                        onClick = {
                            selectedSplit = splitName
                            workoutSplits[splitName]?.forEach { exercise ->
                                activeExercises.add(ActiveExerciseState(exercise))
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(80.dp).padding(vertical = 8.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(text = "$splitName GÜNÜ", fontSize = 24.sp, fontWeight = FontWeight.Bold)
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
                // EGZERSİZLER BUTONU BURADA OLMALI
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
                        onClick = { saveProgressAndExit(true) }, // isFinished = true
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

                // YENİ: Tüm hareketler yerine sadece o günün (selectedSplit) hareketlerini çekiyoruz
                val currentExercises = workoutSplits[selectedSplit] ?: emptyList()

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(currentExercises) { exercise ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                // Eğer hareket zaten listede yoksa ekle
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

    // 2. İsteğin: Sayfa kapanmadan o günün programını liste halinde görsün
    if (showProgramDialog) {
        AlertDialog(
            onDismissRequest = { showProgramDialog = false },
            title = { Text("$selectedSplit Programı", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val programExercises = workoutSplits[selectedSplit] ?: emptyList()
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
                TextButton(onClick = { showProgramDialog = false }) {
                    Text("Kapat")
                }
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
            // Hareket Adı ve Silme Butonu (Çarpı)
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
                Spacer(modifier = Modifier.weight(0.4f)) // Çöp kutusu için boşluk hizalaması
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
    // Önceki kayıtları kutulara doğru dağıtıyoruz
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

        // Yanlış eklenen seti silme butonu
        IconButton(onClick = onRemoveSetClick, modifier = Modifier.weight(0.4f)) {
            Icon(Icons.Default.Delete, contentDescription = "Seti Sil", tint = MaterialTheme.colorScheme.error)
        }
    }
}