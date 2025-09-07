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

    HttpSession session = safeSession(req);

    Integer target  = session == null ? null : (Integer) session.getAttribute("target");
    Integer attempts= session == null ? null : (Integer) session.getAttribute("attempts");
    Integer low     = session == null ? null : (Integer) session.getAttribute("low");
    Integer high    = session == null ? null : (Integer) session.getAttribute("high");
    @SuppressWarnings("unchecked")
    List<Integer> history = session == null ? null : (List<Integer>) session.getAttribute("history");

    if (target == null) {
      target = ThreadLocalRandom.current().nextInt(1, 101);
      attempts = 0; low = 1; high = 100; history = new ArrayList<>();
    }

    // Optional reset
    if ("1".equals(req.getParameter("newGame"))) {
      target = ThreadLocalRandom.current().nextInt(1, 101);
      attempts = 0; low = 1; high = 100; history = new ArrayList<>();
      persist(session, target, attempts, low, high, history);
      req.setAttribute("message", "New game started. Guess a number between 1 and 100!");
      render(req, resp);  // forward for browsers, plain text for tests
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

    persist(session, target, attempts, low, high, history);
    req.setAttribute("message", msg);
    req.setAttribute("attempts", attempts);
    req.setAttribute("low", low);
    req.setAttribute("high", high);
    req.setAttribute("history", history);

    render(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    render(req, resp);
  }

  /* ---------- helpers ---------- */

  private HttpSession safeSession(HttpServletRequest req){
    try {
      HttpSession s = req.getSession(false);
      return (s != null) ? s : req.getSession(true);
    } catch (IllegalStateException e) {
      return null;
    }
  }

  private void persist(HttpSession s, Integer t, Integer a, Integer lo, Integer hi, List<Integer> h){
    if (s == null) return;
    s.setAttribute("target", t);
    s.setAttribute("attempts", a);
    s.setAttribute("low", lo);
    s.setAttribute("high", hi);
    s.setAttribute("history", h);
  }

  /** Forward for browsers (Accept: text/html), else write plain text for tests/CLI. */
  private void render(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {

    String accept = req.getHeader("Accept");
    boolean wantsHtml = accept != null && accept.contains("text/html");

    RequestDispatcher rd = wantsHtml ? safeDispatcher(req, "/index.jsp") : null;
    if (rd != null) {
      rd.forward(req, resp);
      return;
    }

    // Test-friendly plain text
    resp.setContentType("text/plain;charset=UTF-8");
    Object m = req.getAttribute("message");
    resp.getWriter().write(m == null ? "" : m.toString());
  }

  private RequestDispatcher safeDispatcher(HttpServletRequest req, String path){
    try { return req.getRequestDispatcher(path); }
    catch (Exception e) { return null; }
  }
}
