package com.example.padelscorewear

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Timer
import java.util.TimerTask

class PadelGameViewModel : ViewModel() {
    
    private val _team1Score = MutableStateFlow(0)
    val team1Score: StateFlow<Int> = _team1Score.asStateFlow()
    
    private val _team2Score = MutableStateFlow(0)
    val team2Score: StateFlow<Int> = _team2Score.asStateFlow()
    
    private val _team1Sets = MutableStateFlow(0)
    val team1Sets: StateFlow<Int> = _team1Sets.asStateFlow()
    
    private val _team2Sets = MutableStateFlow(0)
    val team2Sets: StateFlow<Int> = _team2Sets.asStateFlow()
    
    private val _team1Games = MutableStateFlow(0)
    val team1Games: StateFlow<Int> = _team1Games.asStateFlow()
    
    private val _team2Games = MutableStateFlow(0)
    val team2Games: StateFlow<Int> = _team2Games.asStateFlow()
    
    private val _isDeuce = MutableStateFlow(false)
    val isDeuce: StateFlow<Boolean> = _isDeuce.asStateFlow()
    
    private val _advantage = MutableStateFlow<Int?>(null)
    val advantage: StateFlow<Int?> = _advantage.asStateFlow()
    
    private val _matchWinner = MutableStateFlow<Int?>(null)
    val matchWinner: StateFlow<Int?> = _matchWinner.asStateFlow()
    
    private val _elapsedTime = MutableStateFlow(0)
    val elapsedTime: StateFlow<Int> = _elapsedTime.asStateFlow()
    
    private val _isMatchStarted = MutableStateFlow(false)
    val isMatchStarted: StateFlow<Boolean> = _isMatchStarted.asStateFlow()
    
    private val _isTiebreak = MutableStateFlow(false)
    val isTiebreak: StateFlow<Boolean> = _isTiebreak.asStateFlow()
    
    private val _servingTeam = MutableStateFlow(1)
    val servingTeam: StateFlow<Int> = _servingTeam.asStateFlow()
    
    private var timer: Timer? = null
    private var startTime: Long = 0
    private var lastGameServer: Int = 1
    
    val currentServer: Int
        get() {
            if (_isTiebreak.value) {
                val totalPoints = _team1Score.value + _team2Score.value
                if (totalPoints == 0) {
                    return lastGameServer
                }
                val serverGroup = (totalPoints + 1) / 2
                return if (serverGroup % 2 == 1) {
                    if (lastGameServer == 1) 2 else 1
                } else {
                    lastGameServer
                }
            }
            return _servingTeam.value
        }
    
    val timeDisplay: String
        get() {
            val minutes = _elapsedTime.value / 60
            val seconds = _elapsedTime.value % 60
            return String.format("%d:%02d", minutes, seconds)
        }
    
    fun startMatch() {
        if (_isMatchStarted.value) return
        _isMatchStarted.value = true
        startTime = System.currentTimeMillis()
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                _elapsedTime.value = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            }
        }, 1000, 1000)
    }
    
    fun pointWon(team: Int) {
        if (_matchWinner.value != null) return
        
        if (team == 1) {
            _team1Score.value += 1
        } else {
            _team2Score.value += 1
        }
        
        checkGameEnd()
    }
    
    fun removePoint(team: Int) {
        _matchWinner.value = null
        if (team == 1 && _team1Score.value > 0) {
            _team1Score.value -= 1
        } else if (team == 2 && _team2Score.value > 0) {
            _team2Score.value -= 1
        }
        checkGameEnd()
    }
    
    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }
    
    private fun checkGameEnd() {
        if (_isTiebreak.value) {
            if (_team1Score.value >= 7 && _team1Score.value >= _team2Score.value + 2) {
                gameWon(1)
            } else if (_team2Score.value >= 7 && _team2Score.value >= _team1Score.value + 2) {
                gameWon(2)
            }
        } else {
            if (_team1Score.value >= 4 && _team1Score.value >= _team2Score.value + 2) {
                gameWon(1)
            } else if (_team2Score.value >= 4 && _team2Score.value >= _team1Score.value + 2) {
                gameWon(2)
            } else if (_team1Score.value >= 3 && _team2Score.value >= 3) {
                if (_team1Score.value == _team2Score.value) {
                    _isDeuce.value = true
                    _advantage.value = null
                } else if (_team1Score.value > _team2Score.value) {
                    _isDeuce.value = false
                    _advantage.value = 1
                } else {
                    _isDeuce.value = false
                    _advantage.value = 2
                }
            } else {
                _isDeuce.value = false
                _advantage.value = null
            }
        }
    }
    
    private fun gameWon(team: Int) {
        val wasTiebreak = _isTiebreak.value
        
        if (team == 1) {
            _team1Games.value += 1
        } else {
            _team2Games.value += 1
        }
        
        _team1Score.value = 0
        _team2Score.value = 0
        _isDeuce.value = false
        _advantage.value = null
        _isTiebreak.value = false
        
        _servingTeam.value = if (_servingTeam.value == 1) 2 else 1
        
        if (wasTiebreak) {
            setWon(team)
        } else {
            checkSetEnd()
        }
    }
    
    private fun checkSetEnd() {
        if (_team1Games.value >= 6 && _team1Games.value >= _team2Games.value + 2) {
            setWon(1)
        } else if (_team2Games.value >= 6 && _team2Games.value >= _team1Games.value + 2) {
            setWon(2)
        } else if (_team1Games.value == 6 && _team2Games.value == 6 && !_isTiebreak.value) {
            _isTiebreak.value = true
            lastGameServer = _servingTeam.value
        }
    }
    
    private fun setWon(team: Int) {
        if (team == 1) {
            _team1Sets.value += 1
        } else {
            _team2Sets.value += 1
        }
        
        _team1Games.value = 0
        _team2Games.value = 0
        _isTiebreak.value = false
        
        checkMatchEnd()
    }
    
    private fun checkMatchEnd() {
        if (_team1Sets.value == 2) {
            _matchWinner.value = 1
            stopTimer()
        } else if (_team2Sets.value == 2) {
            _matchWinner.value = 2
            stopTimer()
        }
    }
    
    fun reset() {
        _team1Score.value = 0
        _team2Score.value = 0
        _team1Sets.value = 0
        _team2Sets.value = 0
        _team1Games.value = 0
        _team2Games.value = 0
        _isDeuce.value = false
        _advantage.value = null
        _matchWinner.value = null
        stopTimer()
        startTime = 0
        _elapsedTime.value = 0
        _isMatchStarted.value = false
        _isTiebreak.value = false
        _servingTeam.value = 1
    }
    
    fun scoreDisplay(team: Int): String {
        val score = if (team == 1) team1Score.value else team2Score.value
        
        if (isTiebreak.value) {
            return score.toString()
        }
        
        if (team1Score.value >= 3 && team2Score.value >= 3) {
            if (isDeuce.value) {
                return "40"
            } else if (advantage.value == team) {
                return "AD"
            } else {
                return "40"
            }
        }
        
        return when (score) {
            0 -> "0"
            1 -> "15"
            2 -> "30"
            3 -> "40"
            else -> "W"
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
