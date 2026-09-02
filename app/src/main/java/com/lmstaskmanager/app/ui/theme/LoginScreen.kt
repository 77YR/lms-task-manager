package com.lmstaskmanager.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    onConnectBlackboard: () -> Unit,
    onContinueOffline: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top blue section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Blue600),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "LMS Task Manager",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Text(
                    text = "Your academic tasks, organized.",
                    fontSize = 14.sp,
                    color = White.copy(alpha = 0.8f)
                )
            }
        }

        // Bottom actions section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onConnectBlackboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue600,
                    contentColor = White
                )
            ) {
                Text("Connect with Blackboard", fontSize = 15.sp)
            }

            OutlinedButton(
                onClick = onContinueOffline,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Blue600
                )
            ) {
                Text("Continue without account", fontSize = 15.sp)
            }

            Text(
                text = "Connecting with Blackboard allows the app to sync your courses and assignments automatically.",
                fontSize = 11.sp,
                color = Gray400,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}