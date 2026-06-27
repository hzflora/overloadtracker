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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import java.util.Collections

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    viewModel: WorkoutViewModel,
    onNavigateToExerciseDetail: (String) -> Unit
) {
    val routines by viewModel.routines.collectAsState()
    val brandRed = PremiumTheme.BrandRed

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
                title = { 
                    Text(
                        "PROGRAMLARIM", 
                        fontWeight = FontWeight.Black, 
                        letterSpacing = 1.sp,
                        fontSize = 20.sp
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddRoutineDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("YENİ PROGRAM", fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
                containerColor = brandRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 100.dp,
                start = 24.dp,
                end = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(routines) { routineData ->

                var localExercises by remember(routineData.exercises) { mutableStateOf(routineData.exercises) }
                var isExpanded by remember { mutableStateOf(false) }

                var draggingIndex by remember { mutableStateOf<Int?>(null) }
                var dragOffset by remember { mutableStateOf(0f) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = PremiumTheme.Charcoal),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isExpanded = !isExpanded }
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = brandRed.copy(alpha = 0.15f),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.FitnessCenter,
                                        contentDescription = null,
                                        modifier = Modifier.padding(10.dp),
                                        tint = brandRed
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = routineData.routine.name.uppercase(),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = Color.White
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    selectedRoutineIdForExercise = routineData.routine.id
                                    showAddExerciseDialog = true
                                    isExpanded = true
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = "Ekle", tint = brandRed)
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                                if (localExercises.isEmpty()) {
                                    Text(
                                        text = "Henüz hareket eklenmedi.",
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                } else {
                                    localExercises.forEachIndexed { index, exercise ->
                                        key(exercise.id) {
                                            ExerciseListItem(
                                                exercise = exercise,
                                                brandRed = brandRed,
                                                isDragging = draggingIndex == index,
                                                dragOffset = if (draggingIndex == index) dragOffset else 0f,
                                                onNavigateToDetail = { onNavigateToExerciseDetail(exercise.name) },
                                                onEdit = {
                                                    exerciseToEdit = exercise
                                                    editExName = exercise.name
                                                    editExSets = exercise.setsAndReps
                                                    editExRest = exercise.restTime
                                                    showEditExerciseDialog = true
                                                },
                                                onDelete = { viewModel.removeExerciseFromRoutine(exercise.id) },
                                                onDragStart = {
                                                    draggingIndex = index
                                                    dragOffset = 0f
                                                },
                                                onDragEnd = {
                                                    draggingIndex = null
                                                    dragOffset = 0f
                                                    viewModel.updateRoutineExercisesOrder(localExercises)
                                                },
                                                onDrag = { offsetDelta ->
                                                    dragOffset += offsetDelta
                                                    val draggedIdx = draggingIndex ?: return@ExerciseListItem
                                                    val threshold = 150f 
                                                    
                                                    if (dragOffset > threshold && draggedIdx < localExercises.size - 1) {
                                                        val newList = localExercises.toMutableList()
                                                        Collections.swap(newList, draggedIdx, draggedIdx + 1)
                                                        localExercises = newList
                                                        draggingIndex = draggedIdx + 1
                                                        dragOffset -= threshold * 1.2f
                                                    } else if (dragOffset < -threshold && draggedIdx > 0) {
                                                        val newList = localExercises.toMutableList()
                                                        Collections.swap(newList, draggedIdx, draggedIdx - 1)
                                                        localExercises = newList
                                                        draggingIndex = draggedIdx - 1
                                                        dragOffset += threshold * 1.2f
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { viewModel.deleteRoutineAndExercises(routineData.routine.id) },
                                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.2f))
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("PROGRAMI SİL", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- PREMIUM STYLED DIALOGS ---
        if (showAddRoutineDialog) {
            PremiumDialog(
                onDismiss = { showAddRoutineDialog = false },
                title = "YENİ PROGRAM",
                confirmLabel = "EKLE",
                brandColor = brandRed,
                onConfirm = {
                    if (newRoutineName.isNotBlank()) viewModel.addNewRoutine(newRoutineName)
                    newRoutineName = ""
                    showAddRoutineDialog = false
                }
            ) {
                PremiumTextField(value = newRoutineName, onValueChange = { newRoutineName = it }, label = "Program Adı (Örn: PUSH)", brandColor = brandRed)
            }
        }

        if (showAddExerciseDialog) {
            PremiumDialog(
                onDismiss = { showAddExerciseDialog = false },
                title = "HAREKET EKLE",
                confirmLabel = "EKLE",
                brandColor = brandRed,
                onConfirm = {
                    if (newExName.isNotBlank() && selectedRoutineIdForExercise != null) {
                        viewModel.addExerciseToRoutine(selectedRoutineIdForExercise!!, newExName, newExSets, newExRest)
                    }
                    newExName = ""; newExSets = ""; newExRest = ""
                    showAddExerciseDialog = false
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PremiumTextField(value = newExName, onValueChange = { newExName = it }, label = "Hareket Adı", brandColor = brandRed)
                    PremiumTextField(value = newExSets, onValueChange = { newExSets = it }, label = "Set x Tekrar", brandColor = brandRed)
                    PremiumTextField(value = newExRest, onValueChange = { newExRest = it }, label = "Dinlenme (Örn: 2 dk)", brandColor = brandRed)
                }
            }
        }

        if (showEditExerciseDialog && exerciseToEdit != null) {
            PremiumDialog(
                onDismiss = { showEditExerciseDialog = false },
                title = "HAREKETİ DÜZENLE",
                confirmLabel = "GÜNCELLE",
                brandColor = brandRed,
                onConfirm = {
                    if (editExName.isNotBlank()) {
                        viewModel.updateExercise(exerciseToEdit!!, editExName, editExSets, editExRest)
                    }
                    showEditExerciseDialog = false
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PremiumTextField(value = editExName, onValueChange = { editExName = it }, label = "Hareket Adı", brandColor = brandRed)
                    PremiumTextField(value = editExSets, onValueChange = { editExSets = it }, label = "Set x Tekrar", brandColor = brandRed)
                    PremiumTextField(value = editExRest, onValueChange = { editExRest = it }, label = "Dinlenme Süresi", brandColor = brandRed)
                }
            }
        }
    }
}

@Composable
fun ExerciseListItem(
    exercise: com.example.overloadtracker.data.RoutineExercise,
    brandRed: Color,
    isDragging: Boolean,
    dragOffset: Float,
    onNavigateToDetail: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDrag: (Float) -> Unit
) {
    val rowBgColor by animateColorAsState(
        targetValue = if (isDragging) brandRed.copy(alpha = 0.08f) else Color.Transparent,
        label = "dragColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                translationY = dragOffset
                scaleX = if (isDragging) 1.02f else 1.0f
                scaleY = if (isDragging) 1.02f else 1.0f
                shadowElevation = if (isDragging) 8.dp.toPx() else 0f
            }
            .background(rowBgColor)
            .clickable { onNavigateToDetail() }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name, 
                fontSize = 16.sp, 
                fontWeight = FontWeight.Bold, 
                color = Color.White
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = exercise.setsAndReps, 
                    fontSize = 13.sp, 
                    color = brandRed, 
                    fontWeight = FontWeight.Black
                )
                Text(text = "  •  ", color = Color.Gray)
                Icon(
                    Icons.Default.Timer, 
                    contentDescription = null, 
                    modifier = Modifier.size(12.dp), 
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = exercise.restTime, 
                    fontSize = 13.sp, 
                    color = Color.Gray
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Edit, 
                    contentDescription = "Düzenle", 
                    tint = Color.White.copy(alpha = 0.1f), 
                    modifier = Modifier.size(18.dp)
                )
            }
            
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete, 
                    contentDescription = "Sil", 
                    tint = Color.White.copy(alpha = 0.1f), 
                    modifier = Modifier.size(18.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Sürükle",
                tint = Color.White.copy(alpha = 0.2f),
                modifier = Modifier
                    .size(32.dp)
                    .padding(4.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { onDragStart() },
                            onDragEnd = { onDragEnd() },
                            onDragCancel = { onDragEnd() },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount)
                            }
                        )
                    }
            )
        }
    }
}
