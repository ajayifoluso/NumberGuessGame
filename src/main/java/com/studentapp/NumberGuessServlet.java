package com.studentapp;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.RequestDispatcher;
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

    // Session-safe (tests may not provide one)
    HttpSession session = null;
    try { session = req.getSession(false); } catch (IllegalStateException ignored) {}
    if (session == null) {
      try { session = req.getSession(true); } catch (IllegalStateException ignored) {}
    }

    Integer target  = (session != null) ? (Integer) session.getAttribute("target")  : null;
    Integer attempts= (session != null) ? (Integer) session.getAttribute("attempts"): null;
    Integer low     = (session != null) ? (Integer) session.getAttribute("low")     : null;
    Integer high    = (session != null) ? (Integer) session.getAttribute("high")    : null;
    @SuppressWarnings("unchecked")
    List<Integer> history = (session != null) ? (List<Integer>) session.getAttribute("history") : null;

    if (target == null) {
      target = ThreadLocalRandom.current().nextInt(1, 101);
      attempts = 0; low = 1; high = 100; history = new ArrayList<>();
    }

    // Optional new game
    if ("1".equals(req.getParameter("newGame"))) {
      target = ThreadLocalRandom.current().nextInt(1, 101);
      attempts = 0; low = 1; high = 100; history = new ArrayList<>();
      if (session != null) {
        session.setAttribute("target", target);
        session.setAttribute("attempts", attempts);
        session.setAttribute("low", low);
        session.setAttribute("high", high);
        session.setAttribute("history", history);
      }
      req.setAttribute("message", "New game started. Guess a number between 1 and 100!");
      forwardOrWrite(req, resp);
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

    if (session != null) {
      session.setAttribute("target", target);
      session.setAttribute("attempts", attempts);
      session.setAttribute("low", low);
      session.setAttribute("high", high);
      session.setAttribute("history", history);
    }

    req.setAttribute("message", msg);
    req.setAttribute("attempts", attempts);
    req.setAttribute("low", low);
    req.setAttribute("high", high);
    req.setAttribute("history", history);

    forwardOrWrite(req, resp);
  }

  private void forwardOrWrite(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    RequestDispatcher rd = null;
    try { rd = req.getRequestDispatcher("/index.jsp"); } catch (Exception ignored) {}
    if (rd != null) {
      rd.forward(req, resp);
    } else {
      // Test fallback: no container → just write the message
      resp.setContentType("text/plain;charset=UTF-8");
      Object m = req.getAttribute("message");
      resp.getWriter().write(m == null ? "" : m.toString());
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    forwardOrWrite(req, resp);
  }
}
