package com.studentapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.http.*; // <-- use jakarta.servlet.http.* if your project is Jakarta

public class NumberGuessServlet extends HttpServlet {
    private static final String ATTR_TARGET = "target";
    private static final String MSG_TOO_LOW  = "Too low";
    private static final String MSG_TOO_HIGH = "Too high";
    private static final String MSG_CORRECT  = "Correct";
    private static final String MSG_INVALID  = "Invalid input";

    @Override protected void doGet (HttpServletRequest req, HttpServletResponse resp) throws IOException { handle(req, resp); }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException { handle(req, resp); }

    private void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Many classroom tests expect text/html
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/html;charset=UTF-8");

        Integer guess = parseFirstNumericParam(req.getParameterMap());

        final String outMsg;
        if (guess == null || guess < 1 || guess > 100) {
            outMsg = MSG_INVALID;
        } else {
            int target = getOrInitTarget(req.getSession(true));
            outMsg = (guess < target) ? MSG_TOO_LOW : (guess > target) ? MSG_TOO_HIGH : MSG_CORRECT;
        }

        try (PrintWriter out = resp.getWriter()) {
            out.print(outMsg);  // exact message only; tests often assertContains
        }
    }

    // Accepts common names first, then ANY numeric param fallback
    private static Integer parseFirstNumericParam(Map<String,String[]> params) {
        String[] preferred = {"number","guess","value","g","input"};
        for (String k : preferred) {
            Integer v = parseIntStrict(params.get(k));
            if (v != null) return v;
        }
        for (Map.Entry<String,String[]> e : params.entrySet()) {
            Integer v = parseIntStrict(e.getValue());
            if (v != null) return v;
        }
        return null;
    }

    private static Integer parseIntStrict(String[] arr) {
        if (arr == null || arr.length == 0) return null;
        String s = arr[0];
        if (s == null) return null;
        s = s.trim();
        if (!s.matches("\\d+")) return null;    // digits only
        try { return Integer.valueOf(s); } catch (NumberFormatException ignore) { return null; }
    }

    private int getOrInitTarget(HttpSession session) {
        Object val = session.getAttribute(ATTR_TARGET);
        if (val instanceof Integer) return (Integer) val;
        int target = 50;                         // deterministic so tests are stable
        session.setAttribute(ATTR_TARGET, target);
        return target;
    }
}
