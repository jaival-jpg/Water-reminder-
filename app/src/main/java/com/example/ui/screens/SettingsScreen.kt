package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.HydrateViewModel
import com.example.ui.theme.*

@Composable
fun SettingsScreen(viewModel: HydrateViewModel) {
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Data?") },
            text = { Text("This will delete all water logs and preferences. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetData()
                    showResetDialog = false
                    // Process to exit or restart would go here typically
                }) {
                    Text("Reset", color = Color(0xFFFF5252))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = DeepOceanBlue)
                }
            },
            containerColor = Color.White,
            titleContentColor = DeepOceanBlue,
            textContentColor = Color(0xFF94A3B8)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftWhite)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text(
                "Settings",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = DeepOceanBlue,
                modifier = Modifier.padding(top = 16.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            // Appearance
            ControlSection("Appearance", Icons.Filled.Palette) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Theme", color = DeepOceanBlue, fontSize = 16.sp)
                    Text("Geometric Balance", color = RoyalBlue, fontSize = 14.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Data
            ControlSection("Data", Icons.Filled.Warning) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33FF5252))
                        .clickable { showResetDialog = true }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Reset All Data", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // About
            ControlSection("About", Icons.Filled.Info) {
                ControlRow("Version", "1.0.0")
                Spacer(modifier = Modifier.height(16.dp))
                ControlRow("Developer", "Jaival Pandya")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Crafted for your health.", color = Color(0xFF94A3B8), fontSize = 14.sp)
            }
        }
    }
}
