package com.example.megaposture

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate

private data class Task(val id: String, val title: String, val detail: String, val section: String)

private val tasks = listOf(
    Task("chin", "Chin tucks · 10 reps", "Move your chin gently straight backward.", "5-MIN POSTURE"),
    Task("wall", "Wall angels · 8 reps", "Slide arms against a wall only as far as comfortable.", "5-MIN POSTURE"),
    Task("shoulders", "Shoulder-blade squeezes · 10 reps", "Gently bring shoulder blades together; don't shrug.", "5-MIN POSTURE"),
    Task("catcow", "Cat-cow · 8 reps", "Slowly round and extend your back on hands and knees.", "5-MIN POSTURE"),
    Task("stance", "Comfortable tall stance · 30 sec", "Head balanced, shoulders relaxed, knees unlocked.", "5-MIN POSTURE"),
    Task("move", "Move your body", "Work toward 60 minutes of total activity across the day.", "SCHOOL-DAY HABITS"),
    Task("screen", "Phone / desk posture check", "Raise screen when possible; move between study sessions.", "SCHOOL-DAY HABITS"),
    Task("food", "Eat balanced meals", "Include enough food, protein, fruit/vegetables, and calcium sources.", "SCHOOL-DAY HABITS"),
    Task("water", "Drink water regularly", "Drink throughout the day; don't force excessive amounts.", "SCHOOL-DAY HABITS"),
    Task("sleep", "Get 8–10 hours of sleep", "If you're a teen, try moving bedtime earlier gradually.", "SCHOOL-DAY HABITS")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MegaPostureApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MegaPostureApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mega_posture", 0) }
    val today = LocalDate.now().toString()
    var savedDate by remember { mutableStateOf(prefs.getString("date", today) ?: today) }
    var checked by remember {
        mutableStateOf(
            if (savedDate == today) tasks.filter { prefs.getBoolean(it.id, false) }.map { it.id }.toSet()
            else emptySet()
        )
    }
    var heightText by remember { mutableStateOf(prefs.getString("height", "") ?: "") }
    var heightNote by remember { mutableStateOf("") }

    fun persistTask(id: String, value: Boolean) {
        val next = if (value) checked + id else checked - id
        checked = next
        prefs.edit().putString("date", today).putBoolean(id, value).apply()
        savedDate = today
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = androidx.compose.ui.graphics.Color(0xFF176B55),
            secondary = androidx.compose.ui.graphics.Color(0xFF477D70),
            background = androidx.compose.ui.graphics.Color(0xFFF6F8F5),
            surface = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("MEGA POSTURE", fontWeight = FontWeight.ExtraBold)
                            Text("Your daily healthy-growth checklist", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.padding(padding).fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("SMALL HABITS. STRONGER POSTURE.", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${checked.size} / ${tasks.size} tasks completed today", color = MaterialTheme.colorScheme.onPrimary)
                        LinearProgressIndicator(
                            progress = { checked.size.toFloat() / tasks.size },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
                        )
                    }
                }

                val sections = listOf("5-MIN POSTURE", "SCHOOL-DAY HABITS")
                sections.forEach { section ->
                    Text(section, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    tasks.filter { it.section == section }.forEach { task ->
                        Card(shape = RoundedCornerShape(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checked.contains(task.id),
                                    onCheckedChange = { persistTask(task.id, it) }
                                )
                                Column(Modifier.weight(1f).padding(start = 6.dp)) {
                                    Text(task.title, fontWeight = FontWeight.SemiBold)
                                    Text(task.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("MONTHLY HEIGHT LOG", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Measure barefoot, using the same wall and roughly the same time of day. Monthly tracking is enough.", style = MaterialTheme.typography.bodySmall)
                        OutlinedTextField(
                            value = heightText,
                            onValueChange = { heightText = it; prefs.edit().putString("height", it).apply() },
                            label = { Text("Current height (cm), e.g. 150") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(onClick = {
                            heightNote = if (heightText.toFloatOrNull() != null && heightText.toFloat() in 50f..250f) {
                                prefs.edit().putString("height_date", today).putString("height", heightText).apply()
                                "Saved: ${heightText} cm on $today"
                            } else "Enter a height in centimeters, such as 150."
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Save this measurement")
                        }
                        if (heightNote.isNotBlank()) Text(heightNote, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("REMEMBER", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Posture habits can help you stand comfortably upright, but they don't lengthen bones. Sleep, balanced food, activity, and genetics support natural growth.")
                        Text("Stop exercises if they cause pain. No height pills or forced stretching needed.", style = MaterialTheme.typography.bodySmall)
                    }
                }
                OutlinedButton(
                    onClick = {
                        checked = emptySet()
                        prefs.edit().putString("date", today).apply()
                        tasks.forEach { prefs.edit().putBoolean(it.id, false).apply() }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Reset today's checklist") }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
