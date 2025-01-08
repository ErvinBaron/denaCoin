document.addEventListener("DOMContentLoaded", (event) => {
  const hamburger = document.querySelector(".hamburger");
  const hamburgerMenu = document.querySelector(".hamburger-menu");
  const hamburgerOverlay = document.querySelector(".hamburger-overlay");

  hamburger.addEventListener("click", () => {
    hamburgerMenu.classList.toggle("active");
    hamburgerOverlay.style.display = hamburgerMenu.classList.contains("active")
      ? "block"
      : "none";
  });

  hamburgerOverlay.addEventListener("click", () => {
    hamburgerMenu.classList.remove("active");
    hamburgerOverlay.style.display = "none";
  });

  // Close menu when a link is clicked
  const menuLinks = document.querySelectorAll(".hamburger-menu ul li a");
  menuLinks.forEach((link) => {
    link.addEventListener("click", () => {
      hamburgerMenu.classList.remove("active");
      hamburgerOverlay.style.display = "none";
    });
  });
});
// Create random stars
function createRandomStars() {
  const container = document.querySelector(".random_stars");
  const numberOfStars = 50;

  for (let i = 0; i < numberOfStars; i++) {
    const star = document.createElement("div");
    star.className = "random_star";
    star.style.left = `${Math.random() * 100}%`;
    star.style.top = `${Math.random() * 100}%`;
    star.style.animationDelay = `${Math.random() * 2}s`;
    container.appendChild(star);
  }
}
// Initialize stars
createRandomStars();