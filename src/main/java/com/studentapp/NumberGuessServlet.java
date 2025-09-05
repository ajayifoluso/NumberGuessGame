package com.studentapp;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Enhanced Number Guessing Game Servlet for V2
 * Supports animations, JSON responses, and advanced game features
 * DevOps CI/CD Project - Team Implementation
 */
@WebServlet("/NumberGuessServlet")
public class NumberGuessServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private static final Gson gson = new Gson();
    
    // Game difficulty settings
    private static final Map<String, GameDifficulty> DIFFICULTY_SETTINGS = new HashMap<>();
    
    static {
        DIFFICULTY_SETTINGS.put("easy", new GameDifficulty(1, 50, 8));
        DIFFICULTY_SETTINGS.put("medium", new GameDifficulty(1, 100, 10));
        DIFFICULTY_SETTINGS.put("hard", new GameDifficulty(1, 500, 15));
        DIFFICULTY_SETTINGS.put("expert", new GameDifficulty(1, 1000, 20));
    }
    
    /**
     * Inner class to represent game difficulty settings
     */
    private static class GameDifficulty {
        final int minRange;
        final int maxRange;
        final int maxAttempts;
        
        GameDifficulty(int minRange, int maxRange, int maxAttempts) {
            this.minRange = minRange;
            this.maxRange = maxRange;
            this.maxAttempts = maxAttempts;
        }
    }
    
    /**
     * Inner class to represent game result
     */
    private static class GameResult {
        boolean correct;
        String message;
        String hint;
        String animationType;
        boolean gameOver;
        boolean won;
        int attempts;
        int target;
        String difficulty;
        long timestamp;
        
        GameResult() {
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Handle POST requests for game guesses
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set response type to JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        
        try {
            // Get request parameters
            String guessStr = request.getParameter("guess");
            String targetStr = request.getParameter("target");
            String attemptsStr = request.getParameter("attempts");
            String difficulty = request.getParameter("difficulty");
            
            // Validate parameters
            if (guessStr == null || targetStr == null || attemptsStr == null) {
                sendErrorResponse(out, "Missing required parameters", 400);
                return;
            }
            
            int guess = Integer.parseInt(guessStr);
            int target = Integer.parseInt(targetStr);
            int attempts = Integer.parseInt(attemptsStr);
            
            // Validate difficulty
            if (difficulty == null || !DIFFICULTY_SETTINGS.containsKey(difficulty)) {
                difficulty = "medium"; // Default fallback
            }
            
            GameDifficulty gameDiff = DIFFICULTY_SETTINGS.get(difficulty);
            
            // Validate guess range
            if (guess < gameDiff.minRange || guess > gameDiff.maxRange) {
                sendErrorResponse(out, "Guess out of valid range (" + 
                    gameDiff.minRange + "-" + gameDiff.maxRange + ")", 400);
                return;
            }
            
            // Process the guess
            GameResult result = processGuess(guess, target, attempts, difficulty, session);
            
            // Send JSON response
            sendGameResponse(out, result);
            
            // Log game activity
            logGameActivity(request, result);
            
        } catch (NumberFormatException e) {
            sendErrorResponse(out, "Invalid number format", 400);
        } catch (Exception e) {
            sendErrorResponse(out, "Server error occurred", 500);
            e.printStackTrace();
        } finally {
            out.close();
        }
    }
    
    /**
     * Handle GET requests for game status and configuration
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            JsonObject gameConfig = new JsonObject();
            gameConfig.addProperty("status", "ready");
            gameConfig.addProperty("version", "2.0");
            gameConfig.addProperty("message", "Number Guessing Game V2 - Enhanced with animations!");
            
            // Add difficulty configurations
            JsonObject difficulties = new JsonObject();
            for (Map.Entry<String, GameDifficulty> entry : DIFFICULTY_SETTINGS.entrySet()) {
                JsonObject diffConfig = new JsonObject();
                GameDifficulty diff = entry.getValue();
                diffConfig.addProperty("min", diff.minRange);
                diffConfig.addProperty("max", diff.maxRange);
                diffConfig.addProperty("maxAttempts", diff.maxAttempts);
                difficulties.add(entry.getKey(), diffConfig);
            }
            gameConfig.add("difficulties", difficulties);
            
            // Add feature list
            gameConfig.addProperty("features", "animations,visual-effects,hints,statistics,multiple-difficulties");
            gameConfig.addProperty("timestamp", System.currentTimeMillis());
            
            out.println(gson.toJson(gameConfig));
            
        } catch (Exception e) {
            sendErrorResponse(out, "Error getting game configuration", 500);
            e.printStackTrace();
        } finally {
            out.close();
        }
    }
    
    /**
     * Process a guess and return game result
     */
    private GameResult processGuess(int guess, int target, int attempts, 
                                   String difficulty, HttpSession session) {
        
        GameResult result = new GameResult();
        result.attempts = attempts;
        result.target = target;
        result.difficulty = difficulty;
        result.correct = (guess == target);
        
        if (result.correct) {
            // Correct guess
            result.message = generateSuccessMessage(attempts);
            result.animationType = attempts == 1 ? "perfectWin" : "correctGuess";
            result.gameOver = true;
            result.won = true;
            result.hint = "Congratulations! 🎉";
            
            // Update session statistics
            updateSessionStats(session, attempts, true, difficulty);
            
        } else {
            // Wrong guess
            GameDifficulty gameDiff = DIFFICULTY_SETTINGS.get(difficulty);
            
            if (attempts >= gameDiff.maxAttempts) {
                // Game over
                result.message = "💀 Game Over! The number was " + target + ". Better luck next time!";
                result.animationType = "gameOver";
                result.gameOver = true;
                result.won = false;
                result.hint = "Don't give up! Try again with a new game.";
                
                updateSessionStats(session, attempts, false, difficulty);
                
            } else {
                // Continue playing
                result.message = generateHintMessage(guess, target);
                result.hint = generateSmartHint(guess, target, attempts, difficulty);
                result.animationType = "wrongGuess";
                result.gameOver = false;
                result.won = false;
            }
        }
        
        return result;
    }
    
    /**
     * Generate success message based on attempts
     */
    private String generateSuccessMessage(int attempts) {
        String[] perfectMessages = {
            "🎉 INCREDIBLE! Perfect first try! You're a genius! 🎉",
            "🎯 AMAZING! Bull's eye on the first shot! 🎯",
            "⭐ PHENOMENAL! First try mastery! ⭐"
        };
        
        String[] goodMessages = {
            "🎉 Excellent! You got it in " + attempts + " attempts! 🎉",
            "🏆 Well done! " + attempts + " attempts - great job! 🏆",
            "✨ Fantastic! Only " + attempts + " tries needed! ✨"
        };
        
        String[] okMessages = {
            "🎉 Good job! You got it in " + attempts + " attempts! 🎉",
            "👍 Nice work! " + attempts + " attempts to victory! 👍",
            "🎯 Success! Found it in " + attempts + " tries! 🎯"
        };
        
        Random random = new Random();
        
        if (attempts == 1) {
            return perfectMessages[random.nextInt(perfectMessages.length)];
        } else if (attempts <= 3) {
            return goodMessages[random.nextInt(goodMessages.length)];
        } else {
            return okMessages[random.nextInt(okMessages.length)];
        }
    }
    
    /**
     * Generate basic hint message
     */
    private String generateHintMessage(int guess, int target) {
        if (guess < target) {
            return "📈 Too low! Aim higher!";
        } else {
            return "📉 Too high! Go lower!";
        }
    }
    
    /**
     * Generate smart hint with proximity and context
     */
    private String generateSmartHint(int guess, int target, int attempts, String difficulty) {
        int difference = Math.abs(guess - target);
        StringBuilder hint = new StringBuilder();
        
        // Basic direction
        if (guess < target) {
            hint.append("📈 Go higher! ");
        } else {
            hint.append("📉 Go lower! ");
        }
        
        // Proximity hints based on difficulty
        GameDifficulty gameDiff = DIFFICULTY_SETTINGS.get(difficulty);
        int range = gameDiff.maxRange - gameDiff.minRange + 1;
        double proximityPercent = (double) difference / range * 100;
        
        if (proximityPercent <= 2) {
            hint.append("🔥 EXTREMELY close! Almost there!");
        } else if (proximityPercent <= 5) {
            hint.append("🔥 Very close! You're burning hot!");
        } else if (proximityPercent <= 10) {
            hint.append("😊 Getting warmer! Keep going!");
        } else if (proximityPercent <= 20) {
            hint.append("🏠 In the right neighborhood!");
        } else if (proximityPercent <= 40) {
            hint.append("🚶 On the right track!");
        } else {
            hint.append("💪 Keep trying! You can do this!");
        }
        
        // Add encouragement based on attempts
        int maxAttempts = gameDiff.maxAttempts;
        int remaining = maxAttempts - attempts;
        
        if (remaining <= 2) {
            hint.append(" ⚠️ Only ").append(remaining).append(" attempt(s) left!");
        } else if (remaining <= 4) {
            hint.append(" ⏰ ").append(remaining).append(" attempts remaining.");
        }
        
        return hint.toString();
    }
    
    /**
     * Update session statistics
     */
    private void updateSessionStats(HttpSession session, int attempts, boolean won, String difficulty) {
        // Get or initialize stats
        Integer totalGames = (Integer) session.getAttribute("totalGames");
        Integer totalWins = (Integer) session.getAttribute("totalWins");
        Integer totalAttempts = (Integer) session.getAttribute("totalAttempts");
        Integer bestScore = (Integer) session.getAttribute("bestScore");
        
        if (totalGames == null) totalGames = 0;
        if (totalWins == null) totalWins = 0;
        if (totalAttempts == null) totalAttempts = 0;
        
        // Update stats
        totalGames++;
        totalAttempts += attempts;
        
        if (won) {
            totalWins++;
            if (bestScore == null || attempts < bestScore) {
                bestScore = attempts;
                session.setAttribute("bestScore", bestScore);
            }
        }
        
        // Save updated stats
        session.setAttribute("totalGames", totalGames);
        session.setAttribute("totalWins", totalWins);
        session.setAttribute("totalAttempts", totalAttempts);
        session.setAttribute("lastDifficulty", difficulty);
        session.setAttribute("lastPlayTime", new Date());
    }
    
    /**
     * Send game response as JSON
     */
    private void sendGameResponse(PrintWriter out, GameResult result) {
        JsonObject response = new JsonObject();
        response.addProperty("correct", result.correct);
        response.addProperty("message", result.message);
        response.addProperty("hint", result.hint);
        response.addProperty("animationType", result.animationType);
        response.addProperty("gameOver", result.gameOver);
        response.addProperty("won", result.won);
        response.addProperty("attempts", result.attempts);
        response.addProperty("difficulty", result.difficulty);
        response.addProperty("timestamp", result.timestamp);
        response.addProperty("status", "success");
        
        out.println(gson.toJson(response));
    }
    
    /**
     * Send error response as JSON
     */
    private void sendErrorResponse(PrintWriter out, String message, int statusCode) {
        JsonObject error = new JsonObject();
        error.addProperty("status", "error");
        error.addProperty("message", message);
        error.addProperty("statusCode", statusCode);
        error.addProperty("timestamp", System.currentTimeMillis());
        
        out.println(gson.toJson(error));
    }
    
    /**
     * Log game activity for monitoring
     */
    private void logGameActivity(HttpServletRequest request, GameResult result) {
        String clientIP = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        
        System.out.println("GAME_LOG: " + 
            "IP=" + clientIP + 
            ", Correct=" + result.correct + 
            ", Attempts=" + result.attempts + 
            ", Difficulty=" + result.difficulty + 
            ", Timestamp=" + result.timestamp);
    }
}
