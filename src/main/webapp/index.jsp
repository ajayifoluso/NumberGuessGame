<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Number Guessing Game</title>
  <style>
    body { font-family: system-ui, -apple-system, Segoe UI, Roboto, sans-serif; margin: 0; padding: 24px; background: #f6f7f9; color: #111827; }
    .wrap { max-width: 640px; margin: 8vh auto; background: #fff; border: 1px solid #e5e7eb; border-radius: 12px; padding: 24px 28px; }
    h1 { margin: 0 0 8px; font-size: 1.6rem; }
    p  { margin: 0 0 16px; color: #4b5563; }
    form { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
    input[type=number] { width: 160px; padding: 10px 12px; border-radius: 8px; border: 1px solid #cbd5e1; }
    button { padding: 10px 16px; border: 0; border-radius: 8px; background: #111827; color: #fff; cursor: pointer; }
    #result { margin-top: 14px; }
  </style>
</head>
<body>
  <div class="wrap">
    <h1>Number Guessing Game</h1>
    <p>Guess a number between <strong>1</strong> and <strong>100</strong>.</p>

    <!-- The servlet is mapped to /guess; it reads the "number" query parameter -->
    <form action="guess" method="get">
      <input type="number" name="number" min="1" max="100" required placeholder="Enter your guess"/>
      <button type="submit">Submit</button>
    </form>

    <!-- If your servlet sets a request attribute called "message", show it -->
    <div id="result">
      <%
        Object msg = request.getAttribute("message");
        if (msg != null) { out.print(msg); }
      %>
    </div>
  </div>
</body>
</html>
