/**
 * Number Guessing Game V2.0 - Game Controller
 * Main game logic and UI controller
 */

class GameController {
    constructor() {
        this.secretNumber = 0;
        this.attempts = 0;
        this.maxAttempts = 10;
        this.minRange = 1;
        this.maxRange = 100;
        this.gameActive = false;
        this.score = 0;
        this.highScore = localStorage.getItem('highScore') || 0;
        this.guessHistory = [];
        this.difficulty = 'medium';
        
        this.initializeGame();
        this.bindEventListeners();
    }

    initializeGame() {
        this.secretNumber = this.generateRandomNumber();
        this.attempts = 0;
        this.gameActive = true;
        this.guessHistory = [];
        
        // Update UI
        this.updateDisplay();
        this.enableInput();
        this.showMessage(`Guess a number between ${this.minRange} and ${this.maxRange}!`, 'info');
        
        console.log('Game initialized. Secret number:', this.secretNumber); // Debug - remove in production
    }

    generateRandomNumber() {
        return Math.floor(Math.random() * (this.maxRange - this.minRange + 1)) + this.minRange;
    }

    bindEventListeners() {
        // Guess button click
        const guessBtn = document.getElementById('guessBtn');
        if (guessBtn) {
            guessBtn.addEventListener('click', () => this.makeGuess());
        }

        // Enter key press
        const guessInput = document.getElementById('guessInput');
        if (guessInput) {
            guessInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    this.makeGuess();
                }
            });
        }

        // New game button
        const newGameBtn = document.getElementById('newGameBtn');
        if (newGameBtn) {
            newGameBtn.addEventListener('click', () => this.resetGame());
        }

        // Difficulty selector
        const difficultySelect = document.getElementById('difficulty');
        if (difficultySelect) {
            difficultySelect.addEventListener('change', (e) => this.setDifficulty(e.target.value));
        }

        // Hint button (V2 feature)
        const hintBtn = document.getElementById('hintBtn');
        if (hintBtn) {
            hintBtn.addEventListener('click', () => this.getHint());
        }
    }

    makeGuess() {
        if (!this.gameActive) {
            this.showMessage('Game over! Start a new game.', 'error');
            return;
        }

        const guessInput = document.getElementById('guessInput');
        const guess = parseInt(guessInput.value);

        // Validate input
        if (isNaN(guess) || guess < this.minRange || guess > this.maxRange) {
            this.showMessage(`Please enter a number between ${this.minRange} and ${this.maxRange}`, 'error');
            return;
        }

        // Check for duplicate guess
        if (this.guessHistory.includes(guess)) {
            this.showMessage('You already guessed that number!', 'warning');
            return;
        }

        this.attempts++;
        this.guessHistory.push(guess);
        
        // Check the guess
        if (guess === this.secretNumber) {
            this.handleWin();
        } else if (this.attempts >= this.maxAttempts) {
            this.handleLoss();
        } else {
            this.provideHint(guess);
            this.updateDisplay();
        }

        // Clear input
        guessInput.value = '';
        guessInput.focus();
    }

    provideHint(guess) {
        let message = '';
        const difference = Math.abs(guess - this.secretNumber);

        if (guess < this.secretNumber) {
            message = 'Too low! ';
        } else {
            message = 'Too high! ';
        }

        // Temperature hints (V2 feature)
        if (difference <= 5) {
            message += '🔥 Very hot!';
            this.showMessage(message, 'hot');
        } else if (difference <= 10) {
            message += '🌡️ Hot!';
            this.showMessage(message, 'warm');
        } else if (difference <= 20) {
            message += '😐 Warm';
            this.showMessage(message, 'neutral');
        } else {
            message += '❄️ Cold!';
            this.showMessage(message, 'cold');
        }

        // Animate guess feedback (V2 feature)
        this.animateGuess(guess);
    }

    handleWin() {
        this.gameActive = false;
        this.calculateScore();
        this.updateHighScore();
        
        // Victory animation (V2 feature)
        this.playVictoryAnimation();
        
        this.showMessage(`🎉 Congratulations! You got it in ${this.attempts} attempts! Score: ${this.score}`, 'success');
        this.disableInput();
        
        // Show stats (V2 feature)
        this.displayGameStats();
    }

    handleLoss() {
        this.gameActive = false;
        this.showMessage(`😢 Game Over! The number was ${this.secretNumber}`, 'error');
        this.disableInput();
        
        // Loss animation (V2 feature)
        this.playLossAnimation();
    }

    calculateScore() {
        // Score calculation based on attempts and difficulty (V2 feature)
        const baseScore = 1000;
        const attemptPenalty = this.attempts * 50;
        const difficultyBonus = this.getDifficultyBonus();
        
        this.score = Math.max(0, baseScore - attemptPenalty + difficultyBonus);
    }

    getDifficultyBonus() {
        const bonuses = {
            easy: 0,
            medium: 200,
            hard: 500,
            expert: 1000
        };
        return bonuses[this.difficulty] || 0;
    }

    updateHighScore() {
        if (this.score > this.highScore) {
            this.highScore = this.score;
            localStorage.setItem('highScore', this.highScore);
            this.showMessage('🏆 New High Score!', 'success');
        }
    }

    setDifficulty(level) {
        this.difficulty = level;
        
        switch(level) {
            case 'easy':
                this.maxAttempts = 15;
                this.maxRange = 50;
                break;
            case 'medium':
                this.maxAttempts = 10;
                this.maxRange = 100;
                break;
            case 'hard':
                this.maxAttempts = 7;
                this.maxRange = 200;
                break;
            case 'expert':
                this.maxAttempts = 5;
                this.maxRange = 500;
                break;
        }
        
        this.resetGame();
    }

    getHint() {
        if (!this.gameActive || this.attempts === 0) {
            this.showMessage('Make at least one guess first!', 'info');
            return;
        }

        // Provide mathematical hint (V2 feature)
        const hints = [
            `The number is ${this.secretNumber % 2 === 0 ? 'even' : 'odd'}`,
            `The number is ${this.secretNumber > this.maxRange/2 ? 'greater' : 'less'} than ${this.maxRange/2}`,
            `The sum of digits is ${this.getDigitSum(this.secretNumber)}`,
            `The number is ${this.isPrime(this.secretNumber) ? '' : 'not '}prime`
        ];

        const randomHint = hints[Math.floor(Math.random() * hints.length)];
        this.showMessage(`💡 Hint: ${randomHint}`, 'hint');
    }

    getDigitSum(num) {
        return num.toString().split('').reduce((sum, digit) => sum + parseInt(digit), 0);
    }

    isPrime(num) {
        if (num <= 1) return false;
        for (let i = 2; i <= Math.sqrt(num); i++) {
            if (num % i === 0) return false;
        }
        return true;
    }

    resetGame() {
        this.initializeGame();
        this.clearMessages();
        this.updateDisplay();
    }

    // UI Update Methods (V2 features)
    updateDisplay() {
        // Update attempts counter
        const attemptsElement = document.getElementById('attempts');
        if (attemptsElement) {
            attemptsElement.textContent = `Attempts: ${this.attempts}/${this.maxAttempts}`;
        }

        // Update progress bar (V2 feature)
        const progressBar = document.getElementById('progressBar');
        if (progressBar) {
            const percentage = (this.attempts / this.maxAttempts) * 100;
            progressBar.style.width = `${percentage}%`;
            progressBar.className = percentage > 75 ? 'progress-bar danger' : 
                                   percentage > 50 ? 'progress-bar warning' : 'progress-bar';
        }

        // Update guess history (V2 feature)
        const historyElement = document.getElementById('guessHistory');
        if (historyElement) {
            historyElement.innerHTML = this.guessHistory.map(guess => 
                `<span class="guess-badge ${guess < this.secretNumber ? 'low' : 'high'}">${guess}</span>`
            ).join(' ');
        }

        // Update high score display
        const highScoreElement = document.getElementById('highScore');
        if (highScoreElement) {
            highScoreElement.textContent = `High Score: ${this.highScore}`;
        }
    }

    showMessage(message, type) {
        const messageElement = document.getElementById('message');
        if (messageElement) {
            messageElement.textContent = message;
            messageElement.className = `message ${type}`;
            
            // Add fade-in animation (V2 feature)
            messageElement.style.animation = 'fadeIn 0.5s';
        }
    }

    clearMessages() {
        const messageElement = document.getElementById('message');
        if (messageElement) {
            messageElement.textContent = '';
            messageElement.className = 'message';
        }
    }

    enableInput() {
        const guessInput = document.getElementById('guessInput');
        const guessBtn = document.getElementById('guessBtn');
        if (guessInput) guessInput.disabled = false;
        if (guessBtn) guessBtn.disabled = false;
    }

    disableInput() {
        const guessInput = document.getElementById('guessInput');
        const guessBtn = document.getElementById('guessBtn');
        if (guessInput) guessInput.disabled = true;
        if (guessBtn) guessBtn.disabled = true;
    }

    // Animation methods (V2 features)
    animateGuess(guess) {
        const guessElement = document.createElement('div');
        guessElement.className = 'floating-guess';
        guessElement.textContent = guess;
        guessElement.style.left = `${Math.random() * 80 + 10}%`;
        
        const gameContainer = document.getElementById('gameContainer');
        if (gameContainer) {
            gameContainer.appendChild(guessElement);
            setTimeout(() => guessElement.remove(), 2000);
        }
    }

    playVictoryAnimation() {
        const gameContainer = document.getElementById('gameContainer');
        if (gameContainer) {
            gameContainer.classList.add('victory-animation');
            this.createConfetti();
        }
    }

    playLossAnimation() {
        const gameContainer = document.getElementById('gameContainer');
        if (gameContainer) {
            gameContainer.classList.add('loss-animation');
            setTimeout(() => gameContainer.classList.remove('loss-animation'), 1000);
        }
    }

    createConfetti() {
        // Simple confetti effect (V2 feature)
        for (let i = 0; i < 50; i++) {
            setTimeout(() => {
                const confetti = document.createElement('div');
                confetti.className = 'confetti';
                confetti.style.left = `${Math.random() * 100}%`;
                confetti.style.backgroundColor = `hsl(${Math.random() * 360}, 100%, 50%)`;
                confetti.style.animationDelay = `${Math.random() * 2}s`;
                
                document.body.appendChild(confetti);
                setTimeout(() => confetti.remove(), 3000);
            }, i * 30);
        }
    }

    displayGameStats() {
        // Show game statistics (V2 feature)
        const stats = {
            attempts: this.attempts,
            score: this.score,
            highScore: this.highScore,
            difficulty: this.difficulty,
            timeElapsed: this.getTimeElapsed()
        };

        console.log('Game Stats:', stats);
        
        const statsElement = document.getElementById('gameStats');
        if (statsElement) {
            statsElement.style.display = 'block';
            statsElement.innerHTML = `
                <h3>Game Statistics</h3>
                <p>Final Score: ${stats.score}</p>
                <p>Attempts Used: ${stats.attempts}</p>
                <p>Difficulty: ${stats.difficulty}</p>
                <p>High Score: ${stats.highScore}</p>
            `;
        }
    }

    getTimeElapsed() {
        // Placeholder for time tracking feature
        return '00:00';
    }
}

// Initialize game when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.gameController = new GameController();
});

// Export for testing (if using modules)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = GameController;
}
