package com.example.padelscorewear

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import kotlin.math.min

@Composable
fun PadelScoreApp(
    viewModel: PadelGameViewModel = viewModel()
) {
    val context = LocalContext.current
    val vibrator = context.getSystemService(Vibrator::class.java)
    
    val team1Score by viewModel.team1Score.collectAsState()
    val team2Score by viewModel.team2Score.collectAsState()
    val team1Sets by viewModel.team1Sets.collectAsState()
    val team2Sets by viewModel.team2Sets.collectAsState()
    val team1Games by viewModel.team1Games.collectAsState()
    val team2Games by viewModel.team2Games.collectAsState()
    val isDeuce by viewModel.isDeuce.collectAsState()
    val advantage by viewModel.advantage.collectAsState()
    val matchWinner by viewModel.matchWinner.collectAsState()
    val isMatchStarted by viewModel.isMatchStarted.collectAsState()
    val isTiebreak by viewModel.isTiebreak.collectAsState()
    val elapsedTime by viewModel.elapsedTime.collectAsState()
    
    var showResetDialog by remember { mutableStateOf(false) }
    var lastGames by remember { mutableStateOf(0) }
    var lastSets by remember { mutableStateOf(0) }
    
    LaunchedEffect(team1Games + team2Games) {
        val currentGames = team1Games + team2Games
        if (currentGames > lastGames && lastGames > 0) {
            vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        }
        lastGames = currentGames
    }
    
    LaunchedEffect(team1Sets + team2Sets) {
        val currentSets = team1Sets + team2Sets
        if (currentSets > lastSets && lastSets > 0) {
            vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        }
        lastSets = currentSets
    }
    
    LaunchedEffect(matchWinner) {
        if (matchWinner != null) {
            vibrator?.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
        matchWinner?.let { winner ->
            Text(
                text = "Team $winner Wins!",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
        }
        
        if (isMatchStarted) {
            Text(
                text = viewModel.timeDisplay,
                fontSize = 10.sp,
                color = Color.Gray
            )
        } else {
            Button(
                onClick = { viewModel.startMatch() },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(24.dp)
            ) {
                Text(
                    text = "Start Match",
                    fontSize = 10.sp
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ScoreDisplay(label = "Sets", score = "$team1Sets:$team2Sets")
            ScoreDisplay(label = "Games", score = "$team1Games:$team2Games")
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val score1 by remember(team1Score, team2Score, isDeuce, advantage, isTiebreak) {
                derivedStateOf { viewModel.scoreDisplay(1) }
            }
            
            val score2 by remember(team1Score, team2Score, isDeuce, advantage, isTiebreak) {
                derivedStateOf { viewModel.scoreDisplay(2) }
            }
            
            TeamScoreButton(
                teamName = "Team 1",
                score = score1,
                isServing = viewModel.currentServer == 1,
                backgroundColor = Color(0xFF2196F3).copy(alpha = 0.3f),
                onTap = {
                    if (matchWinner == null) {
                        vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                        viewModel.pointWon(1)
                    }
                },
                onLongPress = {
                    vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                    viewModel.removePoint(1)
                },
                modifier = Modifier.weight(1f)
            )
            
            TeamScoreButton(
                teamName = "Team 2",
                score = score2,
                isServing = viewModel.currentServer == 2,
                backgroundColor = Color(0xFFFF9800).copy(alpha = 0.3f),
                onTap = {
                    if (matchWinner == null) {
                        vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                        viewModel.pointWon(2)
                    }
                },
                onLongPress = {
                    vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                    viewModel.removePoint(2)
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        when {
            isTiebreak -> {
                Text(
                    text = "TIEBREAK",
                    fontSize = 10.sp,
                    color = Color(0xFF9C27B0)
                )
            }
            isDeuce -> {
                Text(
                    text = "DEUCE",
                    fontSize = 10.sp,
                    color = Color(0xFFFFEB3B)
                )
            }
            advantage != null -> {
                Text(
                    text = "ADV Team $advantage",
                    fontSize = 10.sp,
                    color = Color(0xFFFFEB3B)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Button(
            onClick = { showResetDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
        ) {
            Text(
                text = "Reset",
                fontSize = 11.sp
            )
        }
        }
        
        if (showResetDialog) {
            ResetConfirmationDialog(
                onConfirm = {
                    viewModel.reset()
                    showResetDialog = false
                },
                onDismiss = {
                    showResetDialog = false
                }
            )
        }
    }
}

@Composable
fun ScoreDisplay(
    label: String,
    score: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 8.sp,
            color = Color.Gray
        )
        Text(
            text = score,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun TeamScoreButton(
    teamName: String,
    score: String,
    isServing: Boolean,
    backgroundColor: Color,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onTap() }
            .padding(vertical = 12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isServing) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF4CAF50), CircleShape)
                    )
                }
                Text(
                    text = teamName,
                    fontSize = 9.sp
                )
            }
            Text(
                text = score,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ResetConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000).copy(alpha = 0.9f))
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color(0xFF1C1C1C), RoundedCornerShape(12.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Reset Game?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = "This will reset all scores. Are you sure?",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Text("Cancel", fontSize = 13.sp)
                }
                
                Button(
                    onClick = {
                        onConfirm()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Text("Reset", fontSize = 13.sp)
                }
            }
        }
    }
}
