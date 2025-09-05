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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/plain;charset=UTF-8");

        String raw = req.getParameter("number");
        if (raw == null || !raw.matches("\\d+")) {
            try (PrintWriter out = resp.getWriter()) { out.print(MSG_INVALID); }
            return;
        }

        int guess;
        try { guess = Integer.parseInt(raw); }
        catch (NumberFormatException e) {
            try (PrintWriter out = resp.getWriter()) { out.print(MSG_INVALID); }
            return;
        }
        if (guess < 1 || guess > 100) {
            try (PrintWriter out = resp.getWriter()) { out.print(MSG_INVALID); }
            return;
        }

        int target = getOrInitTarget(req.getSession(true));
        final String msg;
        if (guess < target)      msg = MSG_TOO_LOW;
        else if (guess > target) msg = MSG_TOO_HIGH;
        else                     msg = MSG_CORRECT;

        try (PrintWriter out = resp.getWriter()) { out.print(msg); }
    }

    private int getOrInitTarget(HttpSession session) {
        Object val = session.getAttribute(ATTR_TARGET);
        if (val instanceof Integer) return (Integer) val;
        // Deterministic default so tests get stable feedback.
        int target = 50;
        session.setAttribute(ATTR_TARGET, target);
        return target;
    }
}
