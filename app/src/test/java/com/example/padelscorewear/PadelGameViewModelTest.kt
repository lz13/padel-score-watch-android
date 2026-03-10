package com.example.padelscorewear

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PadelGameViewModelTest {
    
    private lateinit var viewModel: PadelGameViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PadelGameViewModel()
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state is correct`() = runTest {
        assertEquals(0, viewModel.team1Score.value)
        assertEquals(0, viewModel.team2Score.value)
        assertEquals(0, viewModel.team1Sets.value)
        assertEquals(0, viewModel.team2Sets.value)
        assertEquals(0, viewModel.team1Games.value)
        assertEquals(0, viewModel.team2Games.value)
        assertFalse(viewModel.isMatchStarted.value)
        assertFalse(viewModel.isDeuce.value)
        assertNull(viewModel.advantage.value)
        assertNull(viewModel.matchWinner.value)
        assertFalse(viewModel.isTiebreak.value)
    }
    
    @Test
    fun `pointWon increases score`() = runTest {
        viewModel.startMatch()
        viewModel.pointWon(1)
        assertEquals(1, viewModel.team1Score.value)
        
        viewModel.pointWon(2)
        assertEquals(1, viewModel.team2Score.value)
    }
    
    @Test
    fun `scoreDisplay returns correct values for standard game`() = runTest {
        assertEquals("0", viewModel.scoreDisplay(1))
        
        viewModel.pointWon(1)
        assertEquals("15", viewModel.scoreDisplay(1))
        
        viewModel.pointWon(1)
        assertEquals("30", viewModel.scoreDisplay(1))
        
        viewModel.pointWon(1)
        assertEquals("40", viewModel.scoreDisplay(1))
    }
    
    @Test
    fun `deuce is set when both teams reach 40`() = runTest {
        viewModel.startMatch()
        repeat(3) { viewModel.pointWon(1) }
        repeat(3) { viewModel.pointWon(2) }
        
        assertTrue(viewModel.isDeuce.value)
        assertEquals("40", viewModel.scoreDisplay(1))
        assertEquals("40", viewModel.scoreDisplay(2))
    }
    
    @Test
    fun `advantage is set after deuce`() = runTest {
        viewModel.startMatch()
        repeat(3) { viewModel.pointWon(1) }
        repeat(3) { viewModel.pointWon(2) }
        
        viewModel.pointWon(1)
        
        assertFalse(viewModel.isDeuce.value)
        assertEquals(1, viewModel.advantage.value)
        assertEquals("AD", viewModel.scoreDisplay(1))
        assertEquals("40", viewModel.scoreDisplay(2))
    }
    
    @Test
    fun `deuce returns when advantage team loses point`() = runTest {
        viewModel.startMatch()
        repeat(3) { viewModel.pointWon(1) }
        repeat(3) { viewModel.pointWon(2) }
        viewModel.pointWon(1)
        
        viewModel.pointWon(2)
        
        assertTrue(viewModel.isDeuce.value)
        assertNull(viewModel.advantage.value)
    }
    
    @Test
    fun `game is won with 4 points and 2 point lead`() = runTest {
        viewModel.startMatch()
        repeat(4) { viewModel.pointWon(1) }
        
        assertEquals(0, viewModel.team1Score.value)
        assertEquals(1, viewModel.team1Games.value)
    }
    
    @Test
    fun `game continues with 4 points but only 1 point lead`() = runTest {
        viewModel.startMatch()
        repeat(3) { viewModel.pointWon(1) }
        repeat(3) { viewModel.pointWon(2) }
        viewModel.pointWon(2)
        
        assertEquals(3, viewModel.team1Score.value)
        assertEquals(4, viewModel.team2Score.value)
        assertEquals(0, viewModel.team2Games.value)
    }
    
    @Test
    fun `set is won with 6 games and 2 game lead`() = runTest {
        viewModel.startMatch()
        repeat(6) {
            repeat(4) { viewModel.pointWon(1) }
        }
        
        assertEquals(1, viewModel.team1Sets.value)
        assertEquals(0, viewModel.team1Games.value)
        assertEquals(0, viewModel.team2Games.value)
    }
    
    @Test
    fun `tiebreak is triggered at 6-6`() = runTest {
        viewModel.startMatch()
        repeat(5) {
            repeat(4) { viewModel.pointWon(1) }
            repeat(4) { viewModel.pointWon(2) }
        }
        repeat(4) { viewModel.pointWon(1) }
        repeat(4) { viewModel.pointWon(2) }
        
        assertTrue(viewModel.isTiebreak.value)
        assertEquals(6, viewModel.team1Games.value)
        assertEquals(6, viewModel.team2Games.value)
    }
    
    @Test
    fun `tiebreak score display shows numeric values`() = runTest {
        viewModel.startMatch()
        repeat(5) {
            repeat(4) { viewModel.pointWon(1) }
            repeat(4) { viewModel.pointWon(2) }
        }
        repeat(4) { viewModel.pointWon(1) }
        repeat(4) { viewModel.pointWon(2) }
        
        viewModel.pointWon(1)
        
        assertTrue(viewModel.isTiebreak.value)
        assertEquals("1", viewModel.scoreDisplay(1))
        assertEquals("0", viewModel.scoreDisplay(2))
    }
    
    @Test
    fun `tiebreak is won at 7 points with 2 point lead`() = runTest {
        viewModel.startMatch()
        repeat(6) {
            repeat(4) { viewModel.pointWon(1) }
        }
        repeat(6) {
            repeat(4) { viewModel.pointWon(2) }
        }
        
        repeat(7) { viewModel.pointWon(1) }
        
        assertEquals(1, viewModel.team1Sets.value)
        assertFalse(viewModel.isTiebreak.value)
    }
    
    @Test
    fun `match is won with 2 sets`() = runTest {
        viewModel.startMatch()
        repeat(12) {
            repeat(4) { viewModel.pointWon(1) }
        }
        
        assertEquals(2, viewModel.team1Sets.value)
        assertEquals(1, viewModel.matchWinner.value)
    }
    
    @Test
    fun `pointWon does nothing after match won`() = runTest {
        viewModel.startMatch()
        repeat(12) {
            repeat(4) { viewModel.pointWon(1) }
        }
        
        viewModel.pointWon(2)
        
        assertEquals(0, viewModel.team2Score.value)
    }
    
    @Test
    fun `removePoint decreases score`() = runTest {
        viewModel.startMatch()
        viewModel.pointWon(1)
        viewModel.removePoint(1)
        
        assertEquals(0, viewModel.team1Score.value)
    }
    
    @Test
    fun `removePoint clears match winner`() = runTest {
        viewModel.startMatch()
        repeat(12) {
            repeat(4) { viewModel.pointWon(1) }
        }
        
        viewModel.removePoint(1)
        
        assertNull(viewModel.matchWinner.value)
    }
    
    @Test
    fun `reset clears all state`() = runTest {
        viewModel.startMatch()
        repeat(4) { viewModel.pointWon(1) }
        
        viewModel.reset()
        
        assertEquals(0, viewModel.team1Score.value)
        assertEquals(0, viewModel.team2Score.value)
        assertEquals(0, viewModel.team1Games.value)
        assertEquals(0, viewModel.team2Games.value)
        assertEquals(0, viewModel.team1Sets.value)
        assertEquals(0, viewModel.team2Sets.value)
        assertFalse(viewModel.isMatchStarted.value)
        assertNull(viewModel.matchWinner.value)
    }
    
    @Test
    fun `server alternates after each game`() = runTest {
        viewModel.startMatch()
        assertEquals(1, viewModel.currentServer)
        
        repeat(4) { viewModel.pointWon(1) }
        
        assertEquals(2, viewModel.currentServer)
        
        repeat(4) { viewModel.pointWon(1) }
        
        assertEquals(1, viewModel.currentServer)
    }
}
