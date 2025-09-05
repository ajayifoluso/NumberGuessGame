/**
 * GAME-CONTROLLER.JS - Number Guessing Game V2 (Visual Only)
 * Main game logic and UI interactions - No sound effects
 */

// Game State Variables
let gameState = {
    targetNumber: 0,
    currentAttempts: 0,
    maxRange: 100,
    minRange: 1,
    difficulty: 'medium',
    gameActive: true,
    hintsUsed: 0,
    startTime: null,
    bestScore: localStorage.getItem('bestScore') || null,
    gamesPlayed: parseInt(localStorage.getItem('gamesPlayed')) || 0,
    totalGuesses: parseInt(localStorage.getItem('totalGuesses')) || 0,
    perfectGames: parseInt(localStorage.getItem('perfectGames')) || 0,
    hintsUsedTotal: parseInt(localStorage.getItem('hintsUsedTotal')) || 0
};

// Difficulty Settings
const difficultySettings = {
    easy: { min: 1, max: 50, maxAttempts: 8 },
    medium: { min: 1, max: 100, maxAttempts: 10 },
    hard: { min: 1, max: 500, maxAttempts: 15 },
    expert: { min: 1, max: 1000, maxAttempts: 20 }
};

// DOM Elements
const elements = {
    guessInput: null,
    feedbackArea: null,
    hintBox: null,
    hintText: null,
    progressFill: null,
    currentAttempts: null,
    bestScore: null,
    gamesPlayed: null,
    winRate: null,
    totalGuesses: null,
    averageAttempts: null,
    hintsUsed: null,
    perfectGames: null
};

/**
 * Initialize the game when page loads
 */
function initializeGame() {
    // Cache DOM elements
    elements.guessInput = document.getElementById('guessInput');
    elements.feedbackArea = document.getElementById('feedbackArea');
    elements.hintBox = document.getElementById('hintBox');
    elements.hintText = document.getElementById('hintText');
    elements.progressFill = document.getElementById('progressFill');
    elements.currentAttempts = document.getElementById('currentAttempts');
    elements.bestScore = document.getElementById('bestScore');
    elements.gamesPlayed = document.getElementById('gamesPlayed');
    elements.winRate = document.getElementById('winRate');
    elements.totalGuesses = document.getElementById('totalGuesses');
    elements.averageAttempts = document.getElementById('averageAttempts');
    elements.hintsUsed = document.getElementById('hintsUsed');
    elements.perfectGames = document.getElementById('perfectGames');

    // Start new game
    startNewGame();
    
    // Update UI
    updateStatsDisplay();
    
    console.log('🎮 Game initialized successfully!');
}

/**
 * Start a new game
 */
function startNewGame() {
    const settings = difficultySettings[gameState.difficulty];
    
    gameState.targetNumber = Math.floor(Math.random() * (settings.max - settings.min + 1)) + settings.min;
    gameState.currentAttempts = 0;
    gameState.maxRange = settings.max;
    gameState.minRange = settings.min;
    gameState.gameActive = true;
    gameState.hintsUsed = 0;
    gameState.startTime = new Date();
    
    // Update UI
    elements.guessInput.value = '';
    elements.guessInput.min = settings.min;
    elements.guessInput.max = settings.max;
    elements.guessInput.placeholder = `Enter number (${settings.min}-${settings.max})`;
    elements.feedbackArea.innerHTML = `🎯 Guess the number between ${settings.min} and ${settings.max}!`;
    elements.feedbackArea.className = 'feedback-area';
    elements.hintBox.style.display = 'none';
    elements.progressFill.style.width = '0%';
    
    updateCurrentAttempts();
    
    console.log(`🎲 New game started! Target: ${gameState.targetNumber} (${gameState.difficulty} mode)`);
}

/**
 * Handle guess submission
 */
function submitGuess(event) {
    event.preventDefault();
    
    if (!gameState.gameActive) {
        showFeedback('Game is over! Start a new game to continue.', 'error');
        return;
    }
    
    const guess = parseInt(elements.guessInput.value);
    
    // Validate input
    if (isNaN(guess) || guess < gameState.minRange || guess > gameState.maxRange) {
        showFeedback(`Please enter a valid number between ${gameState.minRange} and ${gameState.maxRange}!`, 'error');
        triggerAnimation(elements.guessInput, 'animate-wrong');
        showVisualFeedback('❌');
        return;
    }
    
    gameState.currentAttempts++;
    gameState.totalGuesses++;
    updateCurrentAttempts();
    updateProgress();
    
    // Process the guess
    handleGuessClient(guess);
}

/**
 * Handle guess processing (client-side)
 */
function handleGuessClient(guess) {
    if (guess === gameState.targetNumber) {
        handleCorrectGuess();
    } else {
        const hint = generateHint(guess);
        handleWrongGuess(hint);
    }
}

/**
 * Handle correct guess
 */
function handleCorrectGuess() {
    gameState.gameActive = false;
    gameState.gamesPlayed++;
    
    // Check for perfect game (first try)
    if (gameState.currentAttempts === 1) {
        gameState.perfectGames++;
    }
    
    // Update best score
    if (!gameState.bestScore || gameState.currentAttempts < parseInt(gameState.bestScore)) {
        gameState.bestScore = gameState.currentAttempts;
        localStorage.setItem('bestScore', gameState.bestScore);
    }
    
    // Save stats
    saveGameStats();
    
    // Show success feedback
    const message = gameState.currentAttempts === 1 ? 
        '🎉 PERFECT! First try! You\'re amazing! 🎉' :
        `🎉 Correct! You got it in ${gameState.currentAttempts} attempt${gameState.currentAttempts === 1 ? '' : 's'}! 🎉`;
    
    showFeedback(message, 'success');
    triggerAnimation(elements.feedbackArea, 'animate-correct');
    
    // Visual celebration effects
    if (gameState.currentAttempts === 1) {
        showVisualFeedback('🎉');
        celebrateWithConfetti();
        triggerAnimation(document.querySelector('.game-container'), 'animate-win');
    } else {
        showVisualFeedback('✅');
    }
    
    // Update progress to 100%
    elements.progressFill.style.width = '100%';
    
    // Update stats display
    updateStatsDisplay();
    
    // Auto-start new game after delay
    setTimeout(() => {
        if (confirm('🎮 Great job! Ready for another round?')) {
            resetGame();
        }
    }, 3000);
}

/**
 * Handle wrong guess
 */
function handleWrongGuess(hint) {
    const maxAttempts = difficultySettings[gameState.difficulty].maxAttempts;
    
    if (gameState.currentAttempts >= maxAttempts) {
        // Game over
        gameState.gameActive = false;
        gameState.gamesPlayed++;
        saveGameStats();
        
        showFeedback(`💀 Game Over! The number was ${gameState.targetNumber}. Try again!`, 'error');
        triggerAnimation(elements.feedbackArea, 'animate-wrong');
        showVisualFeedback('❌');
        
        // Update stats display
        updateStatsDisplay();
        
        // Auto-suggest new game
        setTimeout(() => {
            if (confirm('💀 Game Over! Want to try again?')) {
                resetGame();
            }
        }, 2000);
    } else {
        // Show hint
        showFeedback(hint, 'error');
        triggerAnimation(elements.feedbackArea, 'animate-wrong');
        showVisualFeedback('❌');
    }
    
    // Clear input
    elements.guessInput.value = '';
    elements.guessInput.focus();
}

/**
 * Generate hint based on guess
 */
function generateHint(guess) {
    const difference = Math.abs(guess - gameState.targetNumber);
    const target = gameState.targetNumber;
    
    let hint = '';
    
    if (guess < target) {
        hint = '📈 Too low! ';
    } else {
        hint = '📉 Too high! ';
    }
    
    // Add proximity hint
    if (difference <= 5) {
        hint += 'You\'re very close! 🔥';
    } else if (difference <= 10) {
        hint += 'Getting warmer! 😊';
    } else if (difference <= 20) {
        hint += 'You\'re in the right neighborhood! 🏠';
    } else {
        hint += 'Keep trying! 💪';
    }
    
    return hint;
}

/**
 * Get hint for current game
 */
function getHint() {
    if (!gameState.gameActive) {
        showFeedback('Start a new game to get hints!', 'error');
        return;
    }
    
    if (gameState.hintsUsed >= 3) {
        showFeedback('No more hints available for this game!', 'error');
        return;
    }
    
    gameState.hintsUsed++;
    gameState.hintsUsedTotal++;
    
    const target = gameState.targetNumber;
    let hintText = '';
    
    switch (gameState.hintsUsed) {
        case 1:
            // Even/Odd hint
            hintText = target % 2 === 0 ? 'The number is even! 🎯' : 'The number is odd! 🎯';
            break;
        case 2:
            // Range hint
            const midpoint = Math.floor((gameState.maxRange + gameState.minRange) / 2);
            if (target <= midpoint) {
                hintText = `The number is in the lower half (${gameState.minRange}-${midpoint})! 📍`;
            } else {
                hintText = `The number is in the upper half (${midpoint + 1}-${gameState.maxRange})! 📍`;
            }
            break;
        case 3:
            // Digit sum hint
            const digitSum = target.toString().split('').reduce((sum, digit) => sum + parseInt(digit), 0);
            hintText = `The sum of digits is ${digitSum}! 🧮`;
            break;
    }
    
    elements.hintText.innerHTML = hintText;
    elements.hintBox.style.display = 'block';
    triggerAnimation(elements.hintBox, 'animate-hint');
    
    // Save hint usage
    localStorage.setItem('hintsUsedTotal', gameState.hintsUsedTotal);
    updateStatsDisplay();
}

/**
 * Reset game
 */
function resetGame() {
    startNewGame();
    elements.hintBox.style.display = 'none';
    triggerAnimation(document.querySelector('.game-container'), 'animate-fade-in');
}

/**
 * Set difficulty
 */
function setDifficulty(difficulty) {
    gameState.difficulty = difficulty;
    
    // Update UI
    document.querySelectorAll('.difficulty-button').forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.classList.add('active');
    
    // Start new game with new difficulty
    startNewGame();
    
    console.log(`🎚️ Difficulty set to: ${difficulty}`);
}

/**
 * Show feedback message
 */
function showFeedback(message, type = '') {
    elements.feedbackArea.innerHTML = message;
    elements.feedbackArea.className = `feedback-area ${type}`;
}

/**
 * Show visual feedback indicator
 */
function showVisualFeedback(icon) {
    const indicator = document.createElement('div');
    indicator.innerHTML = icon;
    indicator.style.cssText = `
        position: fixed;
        top: 70px;
        right: 20px;
        background: rgba(0,0,0,0.8);
        color: white;
        padding: 15px;
        border-radius: 50%;
        z-index: 1001;
        font-size: 1.5rem;
        backdrop-filter: blur(10px);
        border: 1px solid rgba(255,255,255,0.2);
        animation: feedbackAnim 2s ease-out forwards;
    `;

    // Add animation if not present
    if (!document.querySelector('#feedbackAnimations')) {
        const style = document.createElement('style');
        style.id = 'feedbackAnimations';
        style.textContent = `
            @keyframes feedbackAnim {
                0% { opacity: 0; transform: translateX(50px) scale(0.8); }
                20% { opacity: 1; transform: translateX(0) scale(1); }
                80% { opacity: 1; transform: translateX(0) scale(1); }
                100% { opacity: 0; transform: translateX(-20px) scale(0.9); }
            }
        `;
        document.head.appendChild(style);
    }

    document.body.appendChild(indicator);
    setTimeout(() => indicator.remove(), 2000);
}

/**
 * Trigger animation on element
 */
function triggerAnimation(element, animationClass) {
    element.classList.remove(animationClass);
    setTimeout(() => {
        element.classList.add(animationClass);
    }, 10);
    
    // Remove animation class after animation completes
    setTimeout(() => {
        element.classList.remove(animationClass);
    }, 2000);
}

/**
 * Update current attempts display
 */
function updateCurrentAttempts() {
    elements.currentAttempts.textContent = gameState.currentAttempts;
}

/**
 * Update progress bar
 */
function updateProgress() {
    const maxAttempts = difficultySettings[gameState.difficulty].maxAttempts;
    const progress = (gameState.currentAttempts / maxAttempts) * 100;
    elements.progressFill.style.width = Math.min(progress, 100) + '%';
    
    // Change color based on progress
    if (progress >= 80) {
        elements.progressFill.style.background = 'linear-gradient(90deg, #dc3545, #c82333)';
    } else if (progress >= 60) {
        elements.progressFill.style.background = 'linear-gradient(90deg, #ffc107, #e0a800)';
    } else {
        elements.progressFill.style.background = 'linear-gradient(90deg, #4facfe, #00f2fe)';
    }
}

/**
 * Update statistics display
 */
function updateStatsDisplay() {
    elements.bestScore.textContent = gameState.bestScore || '--';
    elements.gamesPlayed.textContent = gameState.gamesPlayed;
    elements.totalGuesses.textContent = gameState.totalGuesses;
    elements.hintsUsed.textContent = gameState.hintsUsedTotal;
    elements.perfectGames.textContent = gameState.perfectGames;
    
    // Calculate win rate
    const winRate = gameState.gamesPlayed > 0 ? 
        Math.round((gameState.perfectGames / gameState.gamesPlayed) * 100) : 0;
    elements.winRate.textContent = winRate + '%';
    
    // Calculate average attempts
    const avgAttempts = gameState.gamesPlayed > 0 ? 
        Math.round(gameState.totalGuesses / gameState.gamesPlayed * 10) / 10 : 0;
    elements.averageAttempts.textContent = avgAttempts;
}

/**
 * Save game statistics to localStorage
 */
function saveGameStats() {
    localStorage.setItem('gamesPlayed', gameState.gamesPlayed);
    localStorage.setItem('totalGuesses', gameState.totalGuesses);
    localStorage.setItem('perfectGames', gameState.perfectGames);
    localStorage.setItem('hintsUsedTotal', gameState.hintsUsedTotal);
}

/**
 * Load game statistics from localStorage
 */
function loadGameStats() {
    gameState.gamesPlayed = parseInt(localStorage.getItem('gamesPlayed')) || 0;
    gameState.totalGuesses = parseInt(localStorage.getItem('totalGuesses')) || 0;
    gameState.perfectGames = parseInt(localStorage.getItem('perfectGames')) || 0;
    gameState.hintsUsedTotal = parseInt(localStorage.getItem('hintsUsedTotal')) || 0;
    gameState.bestScore = localStorage.getItem('bestScore');
}

/**
 * Celebrate with confetti animation
 */
function celebrateWithConfetti() {
    const colors = ['#FFD700', '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FECA57', '#FF9FF3'];
    
    for (let i = 0; i < 50; i++) {
        setTimeout(() => {
            const confetti = document.createElement('div');
            confetti.style.cssText = `
                position: fixed;
                left: ${Math.random() * 100}vw;
                top: -10px;
                width: 10px;
                height: 10px;
                background: ${colors[Math.floor(Math.random() * colors.length)]};
                animation: confetti 3s linear forwards;
                z-index: 1000;
                pointer-events: none;
            `;
            
            document.body.appendChild(confetti);
            setTimeout(() => confetti.remove(), 3000);
        }, i * 100);
    }
}

/**
 * Export functions for global access
 */
window.submitGuess = submitGuess;
window.getHint = getHint;
window.resetGame = resetGame;
window.setDifficulty = setDifficulty;
window.initializeGame = initializeGame;
window.loadGameStats = loadGameStats;
