# Padel Score Watch App

A minimal WearOS (Android Watch) app for keeping score during padel matches.

## Features

- Standard padel scoring (0, 15, 30, 40, deuce, advantage)
- Game tracking
- Set tracking (best of 3)
- Match winner detection
- Tiebreak support at 6-6 (first to 7, win by 2)
- Match timer with start button
- Serve indicator (alternates correctly during tiebreak)
- Haptic feedback (vibration) on scoring, game/set/match wins
- Long press to remove points
- Reset confirmation dialog

## Requirements

- Android Studio Ladybug or later
- Android SDK 34
- WearOS device or emulator (API 30+)

## How to Run

1. Open project in Android Studio
2. Select a WearOS emulator or device
3. Click Run or press Control+R (Cmd+R on Mac)

## Usage

- Tap **Start Match** to begin the timer
- Tap **Team 1** or **Team 2** buttons to award points
- Long press a team button to remove a point (in case of mistake)
- Green dot indicates which team is serving
- Score follows standard tennis/padel rules
- At 6-6 games, tiebreak begins (points shown as 1, 2, 3... instead of 15, 30, 40)
- Match ends when one team wins 2 sets
- Tap **Reset** to start a new match (confirmation required)

## Project Structure

- `app/src/main/java/com/example/padelscorewear/`
  - `MainActivity.kt` - App entry point
  - `PadelScoreApp.kt` - Main UI (Jetpack Compose)
  - `PadelGameViewModel.kt` - Game logic and scoring rules

## Notes

- Built for WearOS (API 30+)
- Uses Jetpack Compose
- No companion phone app needed

## Original Version

This is a WearOS port of the original Apple Watch app. The original watchOS version is kept in the `PadelScoreWatch/` directory for reference.

Built by lz13 (https://github.com/lz13), 2026. Enjoy keeping score on the court!
