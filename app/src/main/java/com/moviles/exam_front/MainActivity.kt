package com.moviles.exam_front

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.moviles.exam_front.common.Constants.IMAGES_BASE_URL
import com.moviles.exam_front.models.Course
import com.moviles.exam_front.ui.theme.ExamFrontTheme
import com.moviles.exam_front.viewmodel.CourseViewModel
import java.io.File

class MainActivity : ComponentActivity() {
    private val courseViewModel: CourseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        courseViewModel.fetchCourses()

        setContent {
            ExamFrontTheme {
                var showDialog by remember { mutableStateOf(false) }
                var selectedCourse by remember { mutableStateOf<Course?>(null) }
                CourseListScreen(
                    courseViewModel,
                    onAddClick = {
                        selectedCourse = null
                        showDialog = true
                    },
                    onEdit = {
                        selectedCourse = it
                        showDialog = true
                    },
                    onDelete = { courseViewModel.deleteCourse(it.id!!) },
                    showDialog = showDialog,
                    selectedCourse = selectedCourse,
                    onDismissDialog = { showDialog = false },
                    onSaveCourse = { course, imageFile ->
                        if (course.id == 0) {
                            courseViewModel.addCourse(course, imageFile)
                        } else {
                            courseViewModel.updateCourse(course, imageFile)
                        }
                        showDialog = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListScreen(
    viewModel: CourseViewModel,
    onAddClick: () -> Unit,
    onEdit: (Course) -> Unit,
    onDelete: (Course) -> Unit,
    showDialog: Boolean,
    selectedCourse: Course?,
    onDismissDialog: () -> Unit,
    onSaveCourse: (Course, File?) -> Unit
) {
    val courses by viewModel.courses.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "CURSOS",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.tertiary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Course",
                    tint = MaterialTheme.colorScheme.onTertiary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Button(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                onClick = { viewModel.fetchCourses() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text("Refrescar Cursos")
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (courses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No courses available.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                    items(courses) { course ->
                        CourseItem(course = course, onEdit = onEdit, onDelete = onDelete)
                    }
                }
            }
        }

        if (showDialog) {
            CourseDialog(
                course = selectedCourse,
                onDismiss = onDismissDialog,
                onSave = onSaveCourse
            )
        }
    }
}

@Composable
fun CourseItem(
    course: Course,
    onEdit: (Course) -> Unit,
    onDelete: (Course) -> Unit
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme

    val imageUrl = if (!course.imageUrl.isNullOrBlank()) {
        IMAGES_BASE_URL.trimEnd('/') + "/" + course.imageUrl.trimStart('/')
    } else {
        null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface,
            contentColor = colors.onSurface
        ),
        elevation = CardDefaults.cardElevation(8.dp),
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            imageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Course Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = course.name,
                style = MaterialTheme.typography.titleLarge.copy(color = colors.primary),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = course.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(icon = "📅", text = course.schedule)
                InfoChip(icon = "👨‍🏫", text = course.professor)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { onEdit(course) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.secondary,
                        contentColor = colors.onSecondary
                    )
                ) {
                    Text("Editar")
                }

                Button(
                    onClick = { onDelete(course) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.errorContainer,
                        contentColor = colors.error
                    )
                ) {
                    Text("Eliminar")
                }

                Button(
                    onClick = {
                        try {
                            val intent = Intent(context, StudentsActivity::class.java).apply {
                                putExtra("courseId", course.id)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e("NAVIGATION", "Error starting StudentsActivity", e)
                            Toast.makeText(context, "Error opening students", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.tertiary,
                        contentColor = colors.onTertiary
                    )
                ) {
                    Text("Estudiantes")
                }
            }
        }
    }
}

@Composable
fun InfoChip(icon: String, text: String) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.small,
        color = colors.primaryContainer,
        contentColor = colors.onPrimaryContainer,
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, modifier = Modifier.padding(end = 4.dp))
            Text(text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDialog(
    course: Course?,
    onDismiss: () -> Unit,
    onSave: (Course, File?) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    var name by remember { mutableStateOf(course?.name ?: "") }
    var description by remember { mutableStateOf(course?.description ?: "") }
    var schedule by remember { mutableStateOf(course?.schedule ?: "") }
    var professor by remember { mutableStateOf(course?.professor ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Text(
                if (course == null) "Add Course" else "Edit Course",
                color = colors.primary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedLabelColor = colors.primary,
                        focusedIndicatorColor = colors.primary
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedLabelColor = colors.primary,
                        focusedIndicatorColor = colors.primary
                    )
                )

                OutlinedTextField(
                    value = schedule,
                    onValueChange = { schedule = it },
                    label = { Text("Schedule") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedLabelColor = colors.primary,
                        focusedIndicatorColor = colors.primary
                    )
                )

                OutlinedTextField(
                    value = professor,
                    onValueChange = { professor = it },
                    label = { Text("Professor") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedLabelColor = colors.primary,
                        focusedIndicatorColor = colors.primary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.secondaryContainer,
                        contentColor = colors.onSecondaryContainer
                    )
                ) {
                    Text("Select Course Image")
                }

                imageUri?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = it,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                }

                if (course?.imageUrl != null && imageUri == null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Current Image:",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurface
                    )
                    AsyncImage(
                        model = IMAGES_BASE_URL.trimEnd('/') + "/" + course.imageUrl.trimStart('/'),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || description.isBlank() || schedule.isBlank() || professor.isBlank()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (imageUri == null && course?.imageUrl == null) {
                        Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val updatedCourse = Course(
                        id = course?.id ?: 0,
                        name = name,
                        description = description,
                        imageUrl = course?.imageUrl ?: "",
                        schedule = schedule,
                        professor = professor
                    )

                    val imageFile = imageUri?.let { uriToFile(it, context) }
                    onSave(updatedCourse, imageFile)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colors.primary
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

fun uriToFile(uri: Uri, context: android.content.Context): File {
    Log.d("FILE_CONVERSION", "Starting URI to file conversion for: $uri")
    val inputStream = context.contentResolver.openInputStream(uri)!!
    val file = File.createTempFile("course_img", ".jpg", context.cacheDir)
    Log.d("FILE_CONVERSION", "Created temp file at: ${file.absolutePath}")

    inputStream.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
    return file
}