document.getElementById("loginForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {
        const result = await sendLoginData(email, password);

        // Check if the login is successful
        if (result.message === "Login successful!") {
            alert(result.message); // Display success message
            window.location.href = "/client/wallet.html"; // Redirect to wallet page
        } else {
            alert(result.message || "Login failed. Please try again.");
        }
    } catch (error) {
        console.error("Failed to send data to server:", error);
        alert("Login failed! Please try again.");
    }
});

async function sendLoginData(email, password) {
    try {
        const response = await fetch("http://localhost:8000/login", {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password }),
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json(); // Parse and return JSON response
    } catch (error) {
        console.error("Error in sendLoginData:", error);
        throw error;
    }
}
