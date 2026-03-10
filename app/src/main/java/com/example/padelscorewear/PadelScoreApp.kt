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
            TeamScoreButton(
                teamName = "Team 1",
                score = viewModel.scoreDisplay(1),
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
                score = viewModel.scoreDisplay(2),
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
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { onLongPress() }
                )
            }
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
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .background(MaterialTheme.colors.surface, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Reset Game?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "This will reset all scores.",
                    fontSize = 12.sp
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontSize = 12.sp)
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
