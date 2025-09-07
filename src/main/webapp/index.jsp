<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Number Guessing Game</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="preload" href="assets/styles.css" as="style">
  <link rel="stylesheet" href="assets/styles.css">
</head>
<body>
  <div class="wrapper">
    <div class="header">
      <span class="logo"></span>
      <div>
        <div class="h1">Number Guessing Game V1</div>
        <div class="sub">Same engine — fresh coat of paint</div>
      </div>
    </div>

    <div class="card">
      <div class="stats">
        <div class="stat"><span>Current Attempts</span><b><%= request.getAttribute("attempts")==null?0:request.getAttribute("attempts") %></b></div>
        <div class="stat"><span>Best Score</span><b>--</b></div>
        <div class="stat"><span>Games Played</span><b>0</b></div>
        <div class="stat"><span>Win Rate</span><b>0%</b></div>
      </div>
    </div>

    <div class="card">
      <div class="pillbar">
        <div class="pill active">Easy (1–50)</div>
        <div class="pill">Medium (1–100)</div>
        <div class="pill">Hard (1–500)</div>
        <div class="pill">Expert (1–1000)</div>
      </div>
    </div>

    <div class="card">
      <h3 style="margin:0 0 6px 0;">🧠 Make Your Guess</h3>
      <form class="form" action="guess" method="post">
        <input class="input" type="number" name="guess" placeholder="Enter your guess" required>
        <div class="row">
          <button class="btn" type="submit">🍀 Submit Guess</button>
          <button class="btn muted" type="submit" name="newGame" value="1">🔁 New Game</button>
        </div>
      </form>

      <div class="hint" id="msg">
        <%
          String message = (String) request.getAttribute("message");
          if (message == null) {
            out.print("Enter a number and click Submit to start playing!");
          } else {
            out.print(message);
          }
        %>
      </div>

      <p style="opacity:.8;margin:.5rem 0 0;">
        Range: <%= request.getAttribute("low")==null?1:request.getAttribute("low") %> –
               <%= request.getAttribute("high")==null?100:request.getAttribute("high") %>
      </p>
      <p style="opacity:.8;margin:.25rem 0 0;">
        Guesses:
        <%
          java.util.List history = (java.util.List) request.getAttribute("history");
          out.print(history==null ? "—" : history.toString());
        %>
      </p>
    </div>

    <div class="footer">Tip: numbers love confidence 😉</div>
  </div>

  <!-- Tiny non-blocking JS: pop the message once -->
  <script defer>
    (function(){
      const el = document.getElementById('msg');
      if (!el) return;
      el.classList.add('flash');
      setTimeout(()=>el.classList.remove('flash'), 900);
    })();
  </script>
</body>
</html>
