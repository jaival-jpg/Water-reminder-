package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.HydrateViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(navController: NavController, viewModel: HydrateViewModel) {
    var currentStep by remember { mutableStateOf(1) }
    
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var dailyGoal by remember { mutableStateOf(3000) }
    var cupSize by remember { mutableStateOf(250) }
    var country by remember { mutableStateOf("India") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(30.dp), clip = false)
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xFF0E258C))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(30.dp))
                .padding(28.dp)
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) with fadeOut(animationSpec = tween(400))
                }, label = "onboarding_step"
            ) { targetStep ->
                when (targetStep) {
                    1 -> StepUserInformation(
                        name = name, onNameChange = { name = it },
                        age = age, onAgeChange = { age = it },
                        weight = weight, onWeightChange = { weight = it },
                        selectedCountry = country, onCountryChange = { country = it },
                        onNext = { currentStep = 2 }
                    )
                    2 -> StepWaterGoal(
                        goal = dailyGoal, onGoalChange = { dailyGoal = it },
                        onNext = { currentStep = 3 }
                    )
                    3 -> StepCupSize(
                        cupSize = cupSize, onCupSizeChange = { cupSize = it },
                        onFinish = {
                            viewModel.saveOnboarding(
                                name = name,
                                age = age,
                                weight = weight,
                                dailyGoal = dailyGoal,
                                reminderInterval = 60
                            )
                            viewModel.updateCountry(country)
                            viewModel.updateCupSize(cupSize)
                            viewModel.completeOnboarding()
                            navController.navigate("home") { popUpTo("onboarding") { inclusive = true } }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StepUserInformation(
    name: String, onNameChange: (String) -> Unit,
    age: String, onAgeChange: (String) -> Unit,
    weight: String, onWeightChange: (String) -> Unit,
    selectedCountry: String, onCountryChange: (String) -> Unit,
    onNext: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val countries = listOf(
        "India", "United States", "United Kingdom", "Canada", "Australia",
        "Japan", "Brazil", "Germany", "France", "South Africa"
    )

    Column {
        Text("Welcome 👋", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = SoftWhite)
        Text("Let’s personalize your journey", fontSize = 16.sp, color = SoftWhite.copy(alpha = 0.7f))
        
        Spacer(modifier = Modifier.height(20.dp))
        
        OutlinedTextField(
            value = name, onValueChange = onNameChange,
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF1F5F9),
                unfocusedContainerColor = Color(0xFFF1F5F9),
                focusedBorderColor = Color(0xFF0E258C),
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                focusedTextColor = Color(0xFF0E258C),
                unfocusedTextColor = Color(0xFF0E258C),
                focusedLabelColor = Color(0xFF0E258C),
                unfocusedLabelColor = Color(0xFF8E9CB2),
                cursorColor = Color(0xFF0E258C)
            ),
            shape = RoundedCornerShape(20.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = age, onValueChange = onAgeChange,
                label = { Text("Age") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1F5F9),
                    unfocusedContainerColor = Color(0xFFF1F5F9),
                    focusedBorderColor = Color(0xFF0E258C),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedTextColor = Color(0xFF0E258C),
                    unfocusedTextColor = Color(0xFF0E258C),
                    focusedLabelColor = Color(0xFF0E258C),
                    unfocusedLabelColor = Color(0xFF8E9CB2),
                    cursorColor = Color(0xFF0E258C)
                ),
                shape = RoundedCornerShape(20.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = weight, onValueChange = onWeightChange,
                label = { Text("Weight (kg)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1F5F9),
                    unfocusedContainerColor = Color(0xFFF1F5F9),
                    focusedBorderColor = Color(0xFF0E258C),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedTextColor = Color(0xFF0E258C),
                    unfocusedTextColor = Color(0xFF0E258C),
                    focusedLabelColor = Color(0xFF0E258C),
                    unfocusedLabelColor = Color(0xFF8E9CB2),
                    cursorColor = Color(0xFF0E258C)
                ),
                shape = RoundedCornerShape(20.dp),
                singleLine = true
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        // Country Selector Dropdown
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedCountry,
                onValueChange = {},
                readOnly = true,
                label = { Text("Country") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown Arrow",
                        tint = Color(0xFF0E258C)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1F5F9),
                    unfocusedContainerColor = Color(0xFFF1F5F9),
                    focusedBorderColor = Color(0xFF0E258C),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedTextColor = Color(0xFF0E258C),
                    unfocusedTextColor = Color(0xFF0E258C),
                    focusedLabelColor = Color(0xFF0E258C),
                    unfocusedLabelColor = Color(0xFF8E9CB2)
                ),
                shape = RoundedCornerShape(20.dp)
            )
            // Invisible touch overlay to handle clicking the dropdown safely
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = !expanded }
            )
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .background(Color(0xFF0E258C))
                    .border(1.dp, GlassWhiteLight, RoundedCornerShape(12.dp))
            ) {
                countries.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = item,
                                color = if (item == selectedCountry) CyanAccent else SoftWhite,
                                fontWeight = if (item == selectedCountry) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 16.sp
                            )
                        },
                        onClick = {
                            onCountryChange(item)
                            expanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(28.dp))
        
        Button(
            onClick = { if (name.isNotBlank()) onNext() },
            enabled = name.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(if (name.isNotBlank()) 12.dp else 0.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (name.isNotBlank()) Color.White else Color.White.copy(alpha = 0.3f),
                contentColor = if (name.isNotBlank()) DarkBlueBackground else SoftWhite.copy(alpha = 0.5f)
            )
        ) {
            Text("Next", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
fun StepWaterGoal(goal: Int, onGoalChange: (Int) -> Unit, onNext: () -> Unit) {
    Column {
        Text("Daily Goal", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = SoftWhite)
        Text("Choose your hydration target", fontSize = 16.sp, color = SoftWhite.copy(alpha = 0.7f))
        
        Spacer(modifier = Modifier.height(24.dp))
        
        val goals = listOf(1500, 2000, 2500, 3000, 3500, 4000)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            goals.chunked(2).forEach { rowGoals ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowGoals.forEach { option ->
                        val isSelected = goal == option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) CyanAccent else GlassWhiteLight)
                                .clickable { onGoalChange(option) }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${option}ml",
                                color = if (isSelected) DarkBlueBackground else SoftWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(36.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(12.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = DarkBlueBackground
            )
        ) {
            Text("Next", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
fun StepCupSize(cupSize: Int, onCupSizeChange: (Int) -> Unit, onFinish: () -> Unit) {
    Column {
        Text("Cup Size", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = SoftWhite)
        Text("Choose your preferred cup size", fontSize = 16.sp, color = SoftWhite.copy(alpha = 0.7f))
        
        Spacer(modifier = Modifier.height(24.dp))
        
        val cups = listOf(150, 200, 250, 300, 400, 500)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            cups.chunked(2).forEach { rowCups ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowCups.forEach { option ->
                        val isSelected = cupSize == option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) CyanAccent else GlassWhiteLight)
                                .clickable { onCupSizeChange(option) }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${option}ml",
                                color = if (isSelected) DarkBlueBackground else SoftWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(36.dp))
        
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(12.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = DarkBlueBackground
            )
        ) {
            Text("Get Started", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}
