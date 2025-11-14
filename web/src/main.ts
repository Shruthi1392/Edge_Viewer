const img = document.getElementById('frame') as HTMLImageElement;
const stats = document.getElementById('stats') as HTMLDivElement;

// Example base64 PNG (short sample — replace with real base64 exported from the Android app)
const sampleBase64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAABH0lEQVR4nO3RMQ0AAAgDIN8/9K3hHBkQYb2mM2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNmzZs2bNnz7A3tqF0dGqVvgAAAABJRU5ErkJggg==";

let frameCount = 0;
let start = Date.now();

function update() {
  img.src = sampleBase64;
  frameCount++;
  const now = Date.now();
  const elapsed = (now - start) / 1000;
  const fps = Math.round(frameCount / (elapsed || 1));
  stats.innerText = `FPS: ${fps} • Resolution: 100x100`;
}

// initial load
img.onload = () => update();
setInterval(update, 1000);