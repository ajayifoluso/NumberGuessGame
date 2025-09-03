<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Team 3 · Number Guessing Game</title>
  <style>
    body { margin:0; min-height:100vh; display:grid; place-items:center;
           background:linear-gradient(135deg,#ff0080,#7928ca,#00f0ff); background-size:300% 300%;
           animation:grad 12s ease infinite; font-family:system-ui, -apple-system, Segoe UI, Roboto, sans-serif; color:#fff;}
    @keyframes grad {0%{background-position:0% 50%}50%{background-position:100% 50%}100%{background-position:0% 50%}}
    .card{ width:min(720px,92vw); background:rgba(0,0,0,.75); border-radius:20px; padding:28px 32px;
           box-shadow:0 12px 30px rgba(0,0,0,.45); }
    .logo{ width:min(680px,92vw); display:block; margin:0 auto 18px; border-radius:16px;
           animation:floatIn .6s ease-out both, glowPulse 3s ease-in-out infinite; }
    @keyframes floatIn{from{transform:translateY(12px);opacity:0}to{transform:translateY(0);opacity:1}}
    @keyframes glowPulse{0%,100%{filter:drop-shadow(0 0 6px #22d3ee)}50%{filter:drop-shadow(0 0 14px #ff0080)}}
    h1{margin:.2rem 0 0;font-size:clamp(24px,3.6vw,34px);color:#00f0ff;text-shadow:2px 2px #ff0080}
    p.muted{color:#cbd5e1}
    form{display:flex;gap:10px;align-items:center;flex-wrap:wrap;margin-top:8px}
    input[type=number]{width:160px;padding:10px 12px;border-radius:12px;border:1px solid #64748b;background:#0b1220;color:#e5e7eb}
    input[type=number]:focus{outline:none;border-color:#22d3ee;box-shadow:0 0 0 3px rgba(34,211,238,.25)}
    .btn{padding:10px 16px;border:0;border-radius:12px;color:#0b1220;font-weight:700;cursor:pointer;
         background:linear-gradient(45deg,#ff0080,#00f0ff)}
    #result{margin-top:14px;color:#e5e7eb}
  </style>
</head>
<body>
  <div class="card">
    <!-- animated team 3 logo -->
    <img class="logo" src="images/team3-logo.svg" alt="Team 3 animated logo"/>

    <h1>Team 3 Number Guessing Game</h1>
    <p class="muted">Guess a number between 1 and 100:</p>

    <form id="guessForm" action="guess" method="get">
      <input type="number" name="number" id="number" min="1" max="100" required placeholder="Enter your guess"/>
      <button class="btn" type="submit">Submit 🎯</button>
    </form>

    <div id="result"></div>
  </div>

<script>
  // --- tiny synth so we don't need audio files ---
  const AudioCtx = window.AudioContext || window.webkitAudioContext;
  let ctx;
  function ensureCtx(){ if(!ctx) ctx = new AudioCtx(); }

  function beep({freq=440, dur=140, type='square', vol=0.04}={}){
    ensureCtx();
    const o = ctx.createOscillator();
    const g = ctx.createGain();
    o.type = type; o.frequency.value = freq;
    o.connect(g); g.connect(ctx.destination);
    const now = ctx.currentTime;
    g.gain.setValueAtTime(vol, now);
    // quick decay so it feels like a "clicky" arcade beep
    g.gain.exponentialRampToValueAtTime(0.001, now + dur/1000);
    o.start(now); o.stop(now + dur/1000 + 0.02);
  }

  // different sounds for too high / too low / correct
  function wrongHigh(){ beep({freq:220, type:'sawtooth'}); }  // low note
  function wrongLow(){  beep({freq:880, type:'sawtooth'}); }  // high note
  function correctJingle(){
    // little triad arpeggio
    [523,659,784,988].forEach((f,i)=> setTimeout(()=>beep({freq:f,type:'triangle',dur:120,vol:0.05}), i*120));
  }

  // --- AJAX submit so we can analyze the server's response text ---
  const form = document.getElementById('guessForm');
  const resultEl = document.getElementById('result');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const num = document.getElementById('number').value;
    try{
      const res = await fetch('guess?number=' + encodeURIComponent(num), { method:'GET', headers:{'X-Requested-With':'fetch'}});
      const text = await res.text();  // your servlet likely returns small HTML; we show it as-is
      resultEl.innerHTML = text;

      // heuristics: adjust keywords to whatever your servlet prints
      if (/too\s*high/i.test(text)) wrongHigh();
      else if (/too\s*low/i.test(text)) wrongLow();
      else if (/correct|you\s*win|congrat/i.test(text)) correctJingle();
      else { /* neutral */ }
    } catch(err){
      resultEl.textContent = 'Error contacting server.';
    }
  });
</script>
</body>
</html>
