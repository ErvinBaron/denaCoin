document.addEventListener('DOMContentLoaded', (event) => {
    const hamburger = document.querySelector('.hamburger');
    const hamburgerMenu = document.querySelector('.hamburger-menu');
    const hamburgerOverlay = document.querySelector('.hamburger-overlay');

    hamburger.addEventListener('click', () => {
        hamburgerMenu.classList.toggle('active');
        hamburgerOverlay.style.display = hamburgerMenu.classList.contains('active') ? 'block' : 'none';
    });

    hamburgerOverlay.addEventListener('click', () => {
        hamburgerMenu.classList.remove('active');
        hamburgerOverlay.style.display = 'none';
    });

    // Close menu when a link is clicked
    const menuLinks = document.querySelectorAll('.hamburger-menu ul li a');
    menuLinks.forEach(link => {
        link.addEventListener('click', () => {
            hamburgerMenu.classList.remove('active');
            hamburgerOverlay.style.display = 'none';
        });
    });
});
        // Create random stars
        function createRandomStars() {
            const container = document.querySelector('.register_container');
            const numberOfStars = 50;

            for (let i = 0; i < numberOfStars; i++) {
                const star = document.createElement('div');
                star.className = 'random_star';
                star.style.left = `${Math.random() * 100}%`;
                star.style.top = `${Math.random() * 100}%`;
                star.style.animationDelay = `${Math.random() * 2}s`;
                container.appendChild(star);
            }
        }

        // Form validation
        document.getElementById('registrationForm').addEventListener('submit', function(e) {
            e.preventDefault();
            
            const username = document.getElementById('username').value;
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            if (password !== confirmPassword) {
                alert('Passwords do not match!');
                return;
            }

            if (password.length < 6) {
                alert('Password must be at least 6 characters long!');
                return;
            }

            // If validation passes
            document.getElementById('successMessage').style.display = 'block';
            this.reset();

            // Hide success message after 3 seconds
            setTimeout(() => {
                document.getElementById('successMessage').style.display = 'none';
            }, 3000);
        });

        // Initialize stars
        createRandomStars();