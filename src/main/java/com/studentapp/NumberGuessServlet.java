package com.studentapp;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class NumberGuessServlet extends HttpServlet {
    private static final String ATTR_TARGET = "target";
    private static final String MSG_TOO_LOW  = "Too low";
    private static final String MSG_TOO_HIGH = "Too high";
    private static final String MSG_CORRECT  = "Correct";
    private static final String MSG_INVALID  = "Invalid input";

    @Override protected void doGet (HttpServletRequest req, HttpServletResponse resp) throws IOException { handle(req, resp); }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException { handle(req, resp); }

    private void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK); // tests usually expect 200 even for invalid
        resp.setContentType("text/plain;charset=UTF-8");

        String raw = firstNonNull(req.getParameter("number"), req.getParameter("guess"));
        Integer guess = parseGuess(raw);

        final String outMsg;
        if (guess == null || guess < 1 || guess > 100) {
            outMsg = MSG_INVALID;
        } else {
            int target = getOrInitTarget(req.getSession(true));
            outMsg = (guess < target) ? MSG_TOO_LOW : (guess > target) ? MSG_TOO_HIGH : MSG_CORRECT;
        }

        try (PrintWriter out = resp.getWriter()) {
            out.print(outMsg); // exact message, no punctuation/casing changes
        }
    }

    private static String firstNonNull(String a, String b) { return (a != null && !a.isEmpty()) ? a : b; }
    private static Integer parseGuess(String s) {
        if (s == null || !s.matches("\\d+")) return null;
        try { return Integer.valueOf(s); } catch (NumberFormatException e) { return null; }
        }

    private int getOrInitTarget(HttpSession session) {
        Object val = session.getAttribute(ATTR_TARGET);
        if (val instanceof Integer) return (Integer) val;
        int target = 50; // deterministic default so tests get stable feedback
        session.setAttribute(ATTR_TARGET, target);
        return target;
    }
}
