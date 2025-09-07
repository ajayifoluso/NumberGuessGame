package com.studentapp;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

@WebServlet("/NumberGuessServlet")
public class NumberGuessServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 100;
    
    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("NumberGuessServlet initialized");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        HttpSession session = request.getSession();
        
        // Initialize game if not already started
        if (session.getAttribute("targetNumber") == null) {
            Random random = new Random();
            int targetNumber = random.nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;
            session.setAttribute("targetNumber", targetNumber);
            session.setAttribute("attempts", 0);
            session.setAttribute("gameWon", false);
        }
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Number Guessing Game</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 50px; background-color: #f0f0f0; }");
        out.println(".container { max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }");
        out.println("h1 { color: #333; text-align: center; }");
        out.println("form { text-align: center; margin: 20px 0; }");
        out.println("input[type='number'] { padding: 10px; font-size: 16px; width: 200px; margin: 10px; }");
        out.println("input[type='submit'] { padding: 10px 20px; font-size: 16px; background-color: #4CAF50; color: white; border: none; border-radius: 5px; cursor: pointer; }");
        out.println("input[type='submit']:hover { background-color: #45a049; }");
        out.println(".message { text-align: center; margin: 20px 0; padding: 15px; border-radius: 5px; }");
        out.println(".success { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }");
        out.println(".info { background-color: #d1ecf1; color: #0c5460; border: 1px solid #bee5eb; }");
        out.println(".error { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        out.println("<h1>🎯 Number Guessing Game</h1>");
        out.println("<p style='text-align: center;'>I'm thinking of a number between " + MIN_NUMBER + " and " + MAX_NUMBER + ". Can you guess it?</p>");
        
        // Show attempts count
        Integer attempts = (Integer) session.getAttribute("attempts");
        if (attempts != null && attempts > 0) {
            out.println("<div class='info'>Attempts: " + attempts + "</div>");
        }
        
        out.println("<form method='post'>");
        out.println("<input type='number' name='guess' min='" + MIN_NUMBER + "' max='" + MAX_NUMBER + "' placeholder='Enter your guess' required>");
        out.println("<br>");
        out.println("<input type='submit' value='Guess'>");
        out.println("</form>");
        
        out.println("<form method='post' style='margin-top: 20px;'>");
        out.println("<input type='hidden' name='action' value='newGame'>");
        out.println("<input type='submit' value='New Game' style='background-color: #007bff;'>");
        out.println("</form>");
        
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        HttpSession session = request.getSession();
        
        // Handle new game request
        if ("newGame".equals(request.getParameter("action"))) {
            session.removeAttribute("targetNumber");
            session.removeAttribute("attempts");
            session.removeAttribute("gameWon");
            doGet(request, response);
            return;
        }
        
        // Get game state
        Integer targetNumber = (Integer) session.getAttribute("targetNumber");
        Integer attempts = (Integer) session.getAttribute("attempts");
        Boolean gameWon = (Boolean) session.getAttribute("gameWon");
        
        // Initialize game if needed
        if (targetNumber == null) {
            Random random = new Random();
            targetNumber = random.nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;
            session.setAttribute("targetNumber", targetNumber);
            attempts = 0;
            session.setAttribute("attempts", attempts);
            session.setAttribute("gameWon", false);
        }
        
        if (attempts == null) {
            attempts = 0;
        }
        
        if (gameWon == null) {
            gameWon = false;
        }
        
        // Process guess
        String guessStr = request.getParameter("guess");
        String message = "";
        String messageClass = "info";
        
        if (guessStr == null || guessStr.trim().isEmpty()) {
            message = "Please enter a valid number.";
            messageClass = "error";
        } else {
            try {
                int guess = Integer.parseInt(guessStr.trim());
                
                if (guess < MIN_NUMBER || guess > MAX_NUMBER) {
                    message = "Please enter a number between " + MIN_NUMBER + " and " + MAX_NUMBER + ".";
                    messageClass = "error";
                } else if (gameWon) {
                    message = "You already won! Start a new game to play again.";
                    messageClass = "success";
                } else {
                    attempts++;
                    session.setAttribute("attempts", attempts);
                    
                    if (guess == targetNumber) {
                        message = "🎉 Congratulations! You guessed it right in " + attempts + " attempt(s)!";
                        messageClass = "success";
                        session.setAttribute("gameWon", true);
                    } else if (guess < targetNumber) {
                        message = "📈 Too low! Try a higher number.";
                        messageClass = "info";
                    } else {
                        message = "📉 Too high! Try a lower number.";
                        messageClass = "info";
                    }
                }
            } catch (NumberFormatException e) {
                message = "Invalid input. Please enter a valid number.";
                messageClass = "error";
            }
        }
        
        // Display the game page with result
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Number Guessing Game</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 50px; background-color: #f0f0f0; }");
        out.println(".container { max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }");
        out.println("h1 { color: #333; text-align: center; }");
        out.println("form { text-align: center; margin: 20px 0; }");
        out.println("input[type='number'] { padding: 10px; font-size: 16px; width: 200px; margin: 10px; }");
        out.println("input[type='submit'] { padding: 10px 20px; font-size: 16px; background-color: #4CAF50; color: white; border: none; border-radius: 5px; cursor: pointer; }");
        out.println("input[type='submit']:hover { background-color: #45a049; }");
        out.println(".message { text-align: center; margin: 20px 0; padding: 15px; border-radius: 5px; }");
        out.println(".success { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }");
        out.println(".info { background-color: #d1ecf1; color: #0c5460; border: 1px solid #bee5eb; }");
        out.println(".error { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        out.println("<h1>🎯 Number Guessing Game</h1>");
        
        // Display message
        if (!message.isEmpty()) {
            out.println("<div class='message " + messageClass + "'>" + message + "</div>");
        }
        
        out.println("<p style='text-align: center;'>I'm thinking of a number between " + MIN_NUMBER + " and " + MAX_NUMBER + ". Can you guess it?</p>");
        
        // Show attempts count
        out.println("<div class='info'>Attempts: " + attempts + "</div>");
        
        if (!gameWon) {
            out.println("<form method='post'>");
            out.println("<input type='number' name='guess' min='" + MIN_NUMBER + "' max='" + MAX_NUMBER + "' placeholder='Enter your guess' required>");
            out.println("<br>");
            out.println("<input type='submit' value='Guess'>");
            out.println("</form>");
        }
        
        out.println("<form method='post' style='margin-top: 20px;'>");
        out.println("<input type='hidden' name='action' value='newGame'>");
        out.println("<input type='submit' value='New Game' style='background-color: #007bff;'>");
        out.println("</form>");
        
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
}