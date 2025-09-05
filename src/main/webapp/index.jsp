<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Number Guessing Game V2 - DevOps Project</title>
    
    <!-- CSS Files -->
    <link rel="stylesheet" href="assets/css/game-styles.css">
    <link rel="stylesheet" href="assets/css/animations.css">
    
    <!-- Favicon -->
    <link rel="icon" href="data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><text y='.9em' font-size='90'>🎯</text></svg>">
    
    <!-- Meta tags for SEO -->
    <meta name="description" content="Number Guessing Game V2 - DevOps CI/CD Project with animations and visual effects">
    <meta name="keywords" content="number game, guessing game, devops, jenkins, ci/cd">
</head>
<body>
    <div class="game-container animate-fade-in">
        <!-- Game Header -->
        <h1 class="game-title">🎯 Number Guessing Game V2</h1>
        <p class="animate-fade-in" style="animation-delay: 0.3s; margin-bottom: 2rem; font-size: 1.1rem; opacity: 0.9;">
            Jenkins CI/CD DevOps Project - Enhanced with Animations & Visual Effects
        </p>

        <!-- Game Statistics -->
        <div class="game-section animate-fade-in" style="animation-delay: 0.6s;">
            <div class="score-display">
                <div class="score-item">
                    <strong>Current Attempts:</strong> <span id="currentAttempts">0</span>
                </div>
                <div class="score-item">
                    <strong>Best Score:</strong> <span id="bestScore">--</span>
                </div>
                <div class="score-item">
                    <strong>Games Played:</strong> <span id="gamesPlayed">0</span>
                </div>
                <div class="score-item">
                    <strong>Win Rate:</strong> <span id="winRate">0%</span>
                </div>
            </div>
        </div>

        <!-- Difficulty Selection -->
        <div class="game-section animate-slide-left" style="animation-delay: 0.9s;">
            <h3 style="margin-bottom: 1rem;">🎚️ Select Difficulty</h3>
            <div class="difficulty-selector">
                <button class="difficulty-button active" onclick="setDifficulty('easy')" data-range="1-50">
                    Easy (1-50)
                </button>
                <button class="difficulty-button" onclick="setDifficulty('medium')" data-range="1-100">
                    Medium (1-100)
                </button>
                <button class="difficulty-button" onclick="setDifficulty('hard')" data-range="1-500">
                    Hard (1-500)
                </button>
                <button class="difficulty-button" onclick="setDifficulty('expert')" data-range="1-1000">
                    Expert (1-1000)
                </button>
            </div>
        </div>

        <!-- Main Game Interface -->
        <div class="game-section animate-slide-right" style="animation-delay: 1.2s;">
            <h2 style="margin-bottom: 1rem;">🎮 Make Your Guess</h2>
            
            <form id="guessForm" onsubmit="submitGuess(event)">
                <input 
                    type="number" 
                    class="game-input" 
                    id="guessInput" 
                    placeholder="Enter your guess" 
                    min="1" 
                    max="100" 
                    required
                    autocomplete="off"
                >
                <br>
                
                <div class="game-controls">
                    <button type="submit" class="game-button primary">
                        🎯 Submit Guess
                    </button>
                    <button type="button" class="game-button secondary" onclick="getHint()">
                        💡 Get Hint
                    </button>
                    <button type="button" class="game-button danger" onclick="resetGame()">
                        🔄 New Game
                    </button>
                </div>
            </form>

            <!-- Feedback Area -->
            <div class="feedback-area" id="feedbackArea">
                🤔 Enter a number and click Submit to start playing!
            </div>

            <!-- Progress Bar -->
            <div class="progress-bar">
                <div class="progress-fill" id="progressFill" style="width: 0%;"></div>
            </div>

            <!-- Hint Box -->
            <div class="hint-box" id="hintBox" style="display: none;">
                💡 <span id="hintText">Click 'Get Hint' for a clue!</span>
            </div>
        </div>

        <!-- Game Statistics Detail -->
        <div class="game-section animate-zoom-in" style="animation-delay: 1.5s;">
            <h3 style="margin-bottom: 1rem;">📊 Detailed Statistics</h3>
            <div class="game-stats">
                <div class="stat-card">
                    <div class="stat-number" id="totalGuesses">0</div>
                    <div class="stat-label">Total Guesses</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number" id="averageAttempts">0</div>
                    <div class="stat-label">Avg Attempts</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number" id="hintsUsed">0</div>
                    <div class="stat-label">Hints Used</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number" id="perfectGames">0</div>
                    <div class="stat-label">Perfect Games</div>
                </div>
            </div>
        </div>

        <!-- Game Features -->
        <div class="game-section animate-fade-in" style="animation-delay: 1.8s;">
            <h3 style="margin-bottom: 1rem;">✨ V2 Features</h3>
            <div style="text-align: left; max-width: 600px; margin: 0 auto; line-height: 1.6;">
                <p>🎨 <strong>Smooth Animations:</strong> Visual feedback for all interactions</p>
                <p>🏆 <strong>Celebration Effects:</strong> Confetti and party animations</p>
                <p>💡 <strong>Smart Hints:</strong> Glowing hints to help players</p>
                <p>📊 <strong>Score Tracking:</strong> Best attempts and detailed statistics</p>
                <p>🎯 <strong>Enhanced UX:</strong> Responsive and engaging interface</p>
                <p>🎚️ <strong>Difficulty Levels:</strong> Easy, Medium, Hard, Expert modes</p>
                <p>📱 <strong>Mobile Friendly:</strong> Works perfectly on all devices</p>
                <p>🔄 <strong>Auto-Save:</strong> Progress saved automatically</p>
            </div>
        </div>

        <!-- Keyboard Shortcuts -->
        <div class="game-section animate-fade-in" style="animation-delay: 2.1s;">
            <h3 style="margin-bottom: 1rem;">⌨️ Keyboard Shortcuts</h3>
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; text-align: left;">
                <div style="background: rgba(255,255,255,0.1); padding: 0.5rem; border-radius: 5px;">
                    <strong>Enter:</strong> Submit guess
                </div>
                <div style="background: rgba(255,255,255,0.1); padding: 0.5rem; border-radius: 5px;">
                    <strong>Escape:</strong> New game
                </div>
                <div style="background: rgba(255,255,255,0.1); padding: 0.5rem; border-radius: 5px;">
                    <strong>Ctrl + H:</strong> Get hint
                </div>
                <div style="background: rgba(255,255,255,0.1); padding: 0.5rem; border-radius: 5px;">
                    <strong>Focus:</strong> Input auto-focus
                </div>
            </div>
        </div>

        <!-- Game Information -->
        <div class="game-footer animate-fade-in" style="animation-delay: 2.4s;">
            <p><strong>DevOps Project:</strong> Automated CI/CD Pipeline with Jenkins</p>
            <p><strong>Version:</strong> 2.0 Visual | <strong>Features:</strong> Animations, Visual Effects, Enhanced UI</p>
            <p><strong>Tech Stack:</strong> Java Servlet, JSP, CSS3 Animations, JavaScript ES6</p>
            <p><strong>Team:</strong> [Your Team Name] | <strong>Course:</strong> DevOps & CI/CD</p>
        </div>
    </div>

    <!-- JavaScript Files -->
    <script src="assets/js/game-controller.js"></script>

    <!-- Initialize Game -->
    <script>
        // Initialize game when page loads
        document.addEventListener('DOMContentLoaded', function() {
            console.log('🎮 Number Guessing Game V2 Loaded Successfully!');
            initializeGame();
            loadGameStats();
            
            // Add keyboard shortcuts
            document.addEventListener('keydown', function(e) {
                // Enter to submit (when input is focused)
                if (e.key === 'Enter' && e.target.id === 'guessInput') {
                    submitGuess(e);
                }
                
                // Escape to reset
                if (e.key === 'Escape') {
                    if (confirm('Are you sure you want to start a new game?')) {
                        resetGame();
                    }
                }
                
                // Ctrl + H for hint
                if (e.key.toLowerCase() === 'h' && e.ctrlKey) {
                    e.preventDefault();
                    getHint();
                }
            });

            // Auto-focus input after page loads
            setTimeout(() => {
                const input = document.getElementById('guessInput');
                if (input) {
                    input.focus();
                }
            }, 500);

            // Add visual loading effect
            document.body.style.opacity = '0';
            setTimeout(() => {
                document.body.style.transition = 'opacity 0.5s ease-in-out';
                document.body.style.opacity = '1';
            }, 100);
        });

        // Handle form submission properly
        document.getElementById('guessForm').addEventListener('submit', function(e) {
            e.preventDefault();
            submitGuess(e);
        });

        // Add smooth scrolling for better UX
        document.documentElement.style.scrollBehavior = 'smooth';

        // Console welcome message
        console.log(`
        🎯 Number Guessing Game V2
        ========================
        📊 Visual-only version with animations
        🎨 Enhanced UI/UX experience
        🚀 DevOps CI/CD Project
        
        Features:
        ✅ Smooth animations
        ✅ Visual feedback
        ✅ Statistics tracking
        ✅ Multiple difficulty levels
        ✅ Smart hints system
        ✅ Confetti celebrations
        ✅ Keyboard shortcuts
        ✅ Mobile responsive
        
        Good luck and have fun! 🎮
        `);
    </script>
</body>
</html>
