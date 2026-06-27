package com.example.overloadtracker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import java.util.Collections

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(viewModel: WorkoutViewModel) {
    val routines by viewModel.routines.collectAsState()

    var showAddRoutineDialog by remember { mutableStateOf(false) }
    var newRoutineName by remember { mutableStateOf("") }

    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var selectedRoutineIdForExercise by remember { mutableStateOf<Int?>(null) }
    var newExName by remember { mutableStateOf("") }
    var newExSets by remember { mutableStateOf("") }
    var newExRest by remember { mutableStateOf("") }

    var showEditExerciseDialog by remember { mutableStateOf(false) }
    var exerciseToEdit by remember { mutableStateOf<com.example.overloadtracker.data.RoutineExercise?>(null) }
    var editExName by remember { mutableStateOf("") }
    var editExSets by remember { mutableStateOf("") }
    var editExRest by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Programlarım", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddRoutineDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Program Ekle") },
                text = { Text("Yeni Program Ekle") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = paddingValues.calculateBottomPadding() + 88.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(routines) { routineData ->

                var localExercises by remember(routineData.exercises) { mutableStateOf(routineData.exercises) }

                // YENİ: Kartın açık/kapalı durumunu tutan değişken (Varsayılan olarak kapalı: false)
                var isExpanded by remember { mutableStateOf(false) }

                // Sürükle bırak animasyonu için state'ler
                var draggingIndex by remember { mutableStateOf<Int?>(null) }
                var dragOffset by remember { mutableStateOf(0f) }
                val itemHeightPx = with(LocalDensity.current) { 70.dp.toPx() }

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // KART BAŞLIĞI (Tıklanabilir yapıldı)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { isExpanded = !isExpanded } // Tıklanınca aç/kapat
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Ok İkonu ve İsim Yan Yana
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Genişlet",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${routineData.routine.name} GÜNÜ",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            // Sağ Taraftaki Butonlar
                            Row {
                                IconButton(onClick = {
                                    selectedRoutineIdForExercise = routineData.routine.id
                                    showAddExerciseDialog = true
                                    isExpanded = true // Hareket eklerken kart otomatik açılsın
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = "Hareket Ekle", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                IconButton(onClick = { viewModel.deleteRoutineAndExercises(routineData.routine.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Programı Sil", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        // KART İÇERİĞİ (Animasyonlu olarak gösterilip gizlenir)
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            if (localExercises.isEmpty()) {
                                Text(
                                    text = "Bu programa henüz hareket eklenmemiş.",
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 14.sp
                                )
                            } else {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    localExercises.forEachIndexed { index, exercise ->

                                        val currentIndex by rememberUpdatedState(index)
                                        val isBeingDragged = draggingIndex == index

                                        val rowBgColor by animateColorAsState(
                                            targetValue = if (isBeingDragged) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                            label = "dragColor"
                                        )

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .zIndex(if (isBeingDragged) 1f else 0f)
                                                .graphicsLayer {
                                                    translationY = if (isBeingDragged) dragOffset else 0f
                                                    shadowElevation = if (isBeingDragged) 8.dp.toPx() else 0f
                                                }
                                                .background(rowBgColor)
                                                .clickable {
                                                    exerciseToEdit = exercise
                                                    editExName = exercise.name
                                                    editExSets = exercise.setsAndReps
                                                    editExRest = exercise.restTime
                                                    showEditExerciseDialog = true
                                                }
                                                .padding(horizontal = 16.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Sol: Hareket Bilgileri
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = exercise.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(text = exercise.setsAndReps, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                                                    Text(text = "  •  ", color = MaterialTheme.colorScheme.secondary)
                                                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(text = exercise.restTime, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                                                }
                                            }

                                            // Sağ: Silme ve Sürükleme İkonları
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = { viewModel.removeExerciseFromRoutine(exercise.id) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                                }

                                                Icon(
                                                    imageVector = Icons.Default.DragHandle,
                                                    contentDescription = "Sürükle",
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .padding(4.dp)
                                                        .pointerInput(Unit) {
                                                            detectVerticalDragGestures(
                                                                onDragStart = {
                                                                    draggingIndex = currentIndex
                                                                    dragOffset = 0f
                                                                },
                                                                onDragEnd = {
                                                                    draggingIndex = null
                                                                    dragOffset = 0f
                                                                    viewModel.updateRoutineExercisesOrder(localExercises)
                                                                },
                                                                onDragCancel = {
                                                                    draggingIndex = null
                                                                    dragOffset = 0f
                                                                },
                                                                onVerticalDrag = { change, dragAmount ->
                                                                    change.consume()
                                                                    if (draggingIndex != null) {
                                                                        dragOffset += dragAmount

                                                                        val threshold = itemHeightPx * 0.6f
                                                                        if (dragOffset > threshold && draggingIndex!! < localExercises.size - 1) {
                                                                            val newList = localExercises.toMutableList()
                                                                            Collections.swap(newList, draggingIndex!!, draggingIndex!! + 1)
                                                                            localExercises = newList
                                                                            draggingIndex = draggingIndex!! + 1
                                                                            dragOffset -= itemHeightPx
                                                                        } else if (dragOffset < -threshold && draggingIndex!! > 0) {
                                                                            val newList = localExercises.toMutableList()
                                                                            Collections.swap(newList, draggingIndex!!, draggingIndex!! - 1)
                                                                            localExercises = newList
                                                                            draggingIndex = draggingIndex!! - 1
                                                                            dragOffset += itemHeightPx
                                                                        }
                                                                    }
                                                                }
                                                            )
                                                        }
                                                )
                                            }
                                        }
                                        if (index < localExercises.size - 1) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(horizontal = 16.dp),
                                                color = MaterialTheme.colorScheme.outlineVariant,
                                                thickness = 0.5.dp
                                            )
                                        }
                                    }
                                }
                            }
                        } // AnimatedVisibility Sonu
                    }
                }
            }
        }

        // --- DİALOGLAR ---
        if (showAddRoutineDialog) {
            AlertDialog(
                onDismissRequest = { showAddRoutineDialog = false },
                title = { Text("Yeni Program") },
                text = {
                    OutlinedTextField(
                        value = newRoutineName,
                        onValueChange = { newRoutineName = it },
                        label = { Text("Program Adı (Örn: FULL BODY)") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newRoutineName.isNotBlank()) viewModel.addNewRoutine(newRoutineName)
                        newRoutineName = ""
                        showAddRoutineDialog = false
                    }) { Text("Ekle") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddRoutineDialog = false }) { Text("İptal") }
                }
            )
        }

        if (showAddExerciseDialog) {
            AlertDialog(
                onDismissRequest = { showAddExerciseDialog = false },
                title = { Text("Hareket Ekle") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = newExName, onValueChange = { newExName = it }, label = { Text("Hareket Adı") }, singleLine = true)
                        OutlinedTextField(value = newExSets, onValueChange = { newExSets = it }, label = { Text("Set x Tekrar") }, singleLine = true)
                        OutlinedTextField(value = newExRest, onValueChange = { newExRest = it }, label = { Text("Dinlenme Süresi") }, singleLine = true)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newExName.isNotBlank() && selectedRoutineIdForExercise != null) {
                            viewModel.addExerciseToRoutine(selectedRoutineIdForExercise!!, newExName, newExSets, newExRest)
                        }
                        newExName = ""; newExSets = ""; newExRest = ""
                        showAddExerciseDialog = false
                    }) { Text("Ekle") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddExerciseDialog = false }) { Text("İptal") }
                }
            )
        }

        if (showEditExerciseDialog && exerciseToEdit != null) {
            AlertDialog(
                onDismissRequest = { showEditExerciseDialog = false },
                title = { Text("Hareketi Düzenle") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = editExName, onValueChange = { editExName = it }, label = { Text("Hareket Adı") }, singleLine = true)
                        OutlinedTextField(value = editExSets, onValueChange = { editExSets = it }, label = { Text("Set x Tekrar") }, singleLine = true)
                        OutlinedTextField(value = editExRest, onValueChange = { editExRest = it }, label = { Text("Dinlenme Süresi") }, singleLine = true)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (editExName.isNotBlank()) {
                            viewModel.updateExercise(exerciseToEdit!!, editExName, editExSets, editExRest)
                        }
                        showEditExerciseDialog = false
                    }) { Text("Güncelle") }
                },
                dismissButton = {
                    TextButton(onClick = { showEditExerciseDialog = false }) { Text("İptal") }
                }
            )
        }
    }
}