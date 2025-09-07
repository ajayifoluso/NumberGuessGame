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
  private static final long serialVersionUID = 1L;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // Be robust for tests/mocks that don't provide a session
    HttpSession session = null;
    try { session = req.getSession(false); } catch (IllegalStateException ignored) {}
    if (session == null) {
      try { session = req.getSession(true); } catch (IllegalStateException ignored) {}
    }

    // Read state (null-safe)
    Integer target  = (session != null) ? (Integer) session.getAttribute("target")  : null;
    Integer attempts= (session != null) ? (Integer) session.getAttribute("attempts"): null;
    Integer low     = (session != null) ? (Integer) session.getAttribute("low")     : null;
    Integer high    = (session != null) ? (Integer) session.getAttribute("high")    : null;
    @SuppressWarnings("unchecked")
    List<Integer> history = (session != null) ? (List<Integer>) session.getAttribute("history") : null;

    if (target == null) {
      target = ThreadLocalRandom.current().nextInt(1, 101); // 1–100 default
      attempts = 0;
      low = 1; high = 100;
      history = new ArrayList<>();
    }

    // Optional: new game reset
    if ("1".equals(req.getParameter("newGame"))) {
      target = ThreadLocalRandom.current().nextInt(1, 101);
      attempts = 0; low = 1; high = 100; history = new ArrayList<>();
      // only persist if we actually have a session
      if (session != null) {
        session.setAttribute("target", target);
        session.setAttribute("attempts", attempts);
        session.setAttribute("low", low);
        session.setAttribute("high", high);
        session.setAttribute("history", history);
      }
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
          msg = "Bang on! " + target + " it is. Nailed it in " + attempts +
                ". Start a new round when you're ready.";
        }
      }
    } catch (Exception e) {
      msg = "Enter a valid whole number.";
    }

    // Persist only if a session is available (tests may not provide one)
    if (session != null) {
      session.setAttribute("target", target);
      session.setAttribute("attempts", attempts);
      session.setAttribute("low", low);
      session.setAttribute("high", high);
      session.setAttribute("history", history);
    }

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
    req.getRequestDispatcher("/index.jsp").forward(req, resp);
  }
}
