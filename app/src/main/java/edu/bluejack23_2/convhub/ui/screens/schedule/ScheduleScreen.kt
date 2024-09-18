package edu.bluejack23_2.convhub.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import edu.bluejack23_2.convhub.data.model.Job
import edu.bluejack23_2.convhub.ui.screens.schedule.ScheduleViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = hiltViewModel()) {
    var selectedDate by remember { mutableStateOf(Calendar.getInstance().time) }
    val jobs by viewModel.jobs.collectAsState()

    LaunchedEffect(selectedDate) {
        viewModel.fetchJobs(selectedDate)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My Taken Job Schedule",
            fontSize = 20.sp,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.CenterHorizontally)
        )

        CalendarView(selectedDate = selectedDate) { date ->
            selectedDate = date
        }


        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            if (jobs.isEmpty()) {
                item {
                    Text(
                        text = "No jobs found for this date",
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
                return@LazyColumn
            }

            items(jobs) { job ->
                JobCard(job)
            }
        }
    }
}

@Composable
fun JobCard(job: Job) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .background(Color(0xFF266EC4), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(text = job.title, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = "Price: ${job.price}", color = Color.White)
        Text(text = "Address: ${job.address}", color = Color.White)
        Text(
            text = "Posted at: ${
                SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
                ).format(job.posted_at)
            }", color = Color.White
        )
        // Add other job details here
    }
}

@Composable
fun CalendarView(selectedDate: Date, onDateSelected: (Date) -> Unit) {
    var currentMonth by remember {
        mutableStateOf(
            Calendar.getInstance().apply { time = selectedDate })
    }
    val calendar = Calendar.getInstance().apply { time = currentMonth.time }
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK)
    val days = (1..daysInMonth).toList()
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    // Calculate number of rows needed to display all days in the month
    val totalDays = (daysInMonth + (firstDayOfMonth - 1))
    val numberOfRows = (totalDays / 7) + if (totalDays % 7 > 0) 1 else 0

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    calendar.add(Calendar.MONTH, -1)
                    currentMonth = calendar
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF266EC4) // Set button background color
                ),
                shape = RoundedCornerShape(8.dp), // Set border radius
                modifier = Modifier
            ) {
                Text(text = "<", color = Color.White)
            }

            Text(
                text = monthFormat.format(currentMonth.time),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = {
                    calendar.add(Calendar.MONTH, 1)
                    currentMonth = calendar
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF266EC4) // Set button background color
                ),
                shape = RoundedCornerShape(8.dp), // Set border radius
                modifier = Modifier
            ) {
                Text(text = ">", color = Color.White)
            }
        }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(numberOfRows) { rowIndex ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (dayOffset in 0 until 7) {
                        val day = (rowIndex * 7 + dayOffset)
                        if (day in 1..daysInMonth) {
                            val dayDate = Calendar.getInstance().apply {
                                time = currentMonth.time
                                set(Calendar.DAY_OF_MONTH, day)
                            }.time

                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .padding(2.dp)
                                    .background(
                                        if (dayDate == selectedDate) Color(0xFF266EC4) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onDateSelected(dayDate) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    color = if (dayDate == selectedDate) Color.White else Color.Black,
                                    fontWeight = if (dayDate == selectedDate) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(40.dp))
                        }
                    }
                }
            }
        }
    }
}
