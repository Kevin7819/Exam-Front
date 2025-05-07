package com.moviles.exam_front

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.moviles.exam_front.models.Course
import com.moviles.exam_front.ui.theme.ExamFrontTheme
import com.moviles.exam_front.viewmodel.CourseViewModel
import android.content.Intent
import android.util.Log
import android.widget.Toast

class MainActivity : ComponentActivity() {
    private val courseViewModel: CourseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    onDismissDialog: () -> Unit
) {
    val courses by viewModel.courses.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Courses") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (courses.isEmpty()) {
                Text("No courses available.")
            } else {
                LazyColumn {
                    items(courses) { course ->
                        CourseItem(
                            course = course,
                            onEdit = onEdit,
                            onDelete = onDelete
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        CourseDialog(
            course = selectedCourse,
            onDismiss = onDismissDialog,
            onSave = { course ->
                if (course.id == null) viewModel.addCourse(course)
                else viewModel.updateCourse(course)
                onDismissDialog()
            }
        )
    }
}

@Composable
fun CourseItem(course: Course, onEdit: (Course) -> Unit, onDelete: (Course) -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = course.name, style = MaterialTheme.typography.titleLarge)
            Text(text = course.description, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Schedule: ${course.schedule}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Professor: ${course.professor}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(onClick = { onEdit(course) }) { Text("Edit") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onDelete(course) }) { Text("Delete") }
                Button(    onClick = {
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
                }
                ) {
                    Text("Students")
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDialog(
    course: Course?,
    onDismiss: () -> Unit,
    onSave: (Course) -> Unit
) {
    var name by remember { mutableStateOf(course?.name ?: "") }
    var description by remember { mutableStateOf(course?.description ?: "") }
    var schedule by remember { mutableStateOf(course?.schedule ?: "") }
    var professor by remember { mutableStateOf(course?.professor ?: "") }
    var imageUrl by remember { mutableStateOf(course?.imageUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (course == null) "Add Course" else "Edit Course") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                OutlinedTextField(value = schedule, onValueChange = { schedule = it }, label = { Text("Schedule") })
                OutlinedTextField(value = professor, onValueChange = { professor = it }, label = { Text("Professor") })
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL") })
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank() && description.isNotBlank() && schedule.isNotBlank() && professor.isNotBlank()) {
                    onSave(Course(course?.id, name, description, imageUrl, schedule, professor))
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
