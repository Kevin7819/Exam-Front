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
import androidx.compose.ui.unit.dp
import com.moviles.exam_front.models.Student
import com.moviles.exam_front.ui.theme.ExamFrontTheme
import com.moviles.exam_front.viewmodel.StudentViewModel

class StudentsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val courseId = intent.getIntExtra("courseId", -1)
        if (courseId == -1) finish()

        setContent {
            ExamFrontTheme {
                val viewModel: StudentViewModel by viewModels()
                LaunchedEffect(Unit) { viewModel.fetchStudentsByCourse(courseId) }
                StudentListScreen(viewModel = viewModel, courseId = courseId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListScreen(viewModel: StudentViewModel, courseId: Int) {
    val students by viewModel.students.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Students for Course $courseId") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedStudent = null
                showDialog = true
            }) { Text("+") }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            LazyColumn {
                items(students) { student ->
                    StudentItem(
                        student = student,
                        onEdit = {
                            selectedStudent = it
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteStudent(it.id!!) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        StudentDialog(
            student = selectedStudent,
            courseId = courseId,
            onDismiss = { showDialog = false },
            onSave = { student ->
                if (student.id == null) viewModel.addStudent(student)
                else viewModel.updateStudent(student)
                showDialog = false
            }
        )
    }
}

@Composable
fun StudentItem(student: Student, onEdit: (Student) -> Unit, onDelete: (Student) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = student.name, style = MaterialTheme.typography.titleMedium)
            Text(text = student.email, style = MaterialTheme.typography.bodySmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onEdit(student) }) { Text("Edit") }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = { onDelete(student) }) { Text("Delete") }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDialog(
    student: Student?,
    courseId: Int,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    var name by remember { mutableStateOf(student?.name ?: "") }
    var email by remember { mutableStateOf(student?.email ?: "") }
    var phone by remember { mutableStateOf(student?.phone ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (student == null) "Add Student" else "Edit Student") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = "Name") }
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(text = "Email") }
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(text = "Phone") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank()) {
                        onSave(Student(
                            id = student?.id,
                            name = name,
                            email = email,
                            phone = phone,
                            courseId = courseId
                        ))
                    }
                }
            ) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}