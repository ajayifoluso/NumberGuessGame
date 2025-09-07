package com.studentapp;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@WebServlet(name = "NumberGuessServlet", urlPatterns = {"/guess"})
public class NumberGuessServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession session = req.getSession(true);

    // --- Session-scoped game state ---
    Integer target = (Integer) session.getAttribute("target");
    Integer attempts = (Integer) session.getAttribute("attempts");
    Integer low = (Integer) session.getAttribute("low");
    Integer high = (Integer) session.getAttribute("high");
    @SuppressWarnings("unchecked")
    List<Integer> history = (List<Integer>) session.getAttribute("history");

    if (target == null) {
      target = ThreadLocalRandom.current().nextInt(1, 101); // default 1–100
      attempts = 0;
      low = 1; high = 100;
      history = new ArrayList<>();
    }

    // Optional "New Game" button
    if ("1".equals(req.getParameter("newGame"))) {
      target = ThreadLocalRandom.current().nextInt(1, 101);
      attempts = 0; low = 1; high = 100; history = new ArrayList<>();
      session.setAttribute("target", target);
      session.setAttribute("attempts", attempts);
      session.setAttribute("low", low);
      session.setAttribute("high", high);
      session.setAttribute("history", history);
      req.setAttribute("message", "New game started. Guess a number between 1 and 100!");
      req.getRequestDispatcher("/index.jsp").forward(req, resp);
      return;
    }

    String msg;
    try {
      int guess = Integer.parseInt(req.getParameter("guess"));

      if (guess < 1 || guess > 100) {
        msg = "Please enter a number between 1 and 100.";
      } else {
        attempts++;
        history.add(guess);

        if (guess < target) {
          low = Math.max(low, guess + 1);
          msg = "Too low! 🔻 Try a higher number (" + low + "–" + high + ").";
        } else if (guess > target) {
          high = Math.min(high, guess - 1);
          msg = "Too high! 🔺 Try a lower number (" + low + "–" + high + ").";
        } else {
          // ✅ Your preferred win message
          msg = "Bang on! " + target + " it is. Nailed it in " + attempts +
                ". Start a new round when you're ready.";
          // Keep state so user can click "New Game" to reset
        }
      }
    } catch (Exception e) {
      msg = "Enter a valid whole number.";
    }

    // Persist state
    session.setAttribute("target", target);
    session.setAttribute("attempts", attempts);
    session.setAttribute("low", low);
    session.setAttribute("high", high);
    session.setAttribute("history", history);

    // Expose to JSP
    req.setAttribute("message", msg);
    req.setAttribute("attempts", attempts);
    req.setAttribute("low", low);
    req.setAttribute("high", high);
    req.setAttribute("history", history);

    req.getRequestDispatcher("/index.jsp").forward(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // Route GET → JSP so first load shows styled page
    req.getRequestDispatcher("/index.jsp").forward(req, resp);
  }
}
