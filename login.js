document.getElementById("loginForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {
        const result = await sendLoginData(email, password);
        console.log(result.message);
        alert("Registration successful!");
    } catch (error) {
        console.error("Failed to send data to server", error);
        alert("Registration failed! Please try again.");
    }
});

async function sendLoginData(email, password) {
    try {
        const response = await fetch("http://localhost:8000/register", {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: email,
                password: password,
            }),
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.json(); 
    } catch (error) {
        console.error("Error in sendregistrationData:", error);
        throw error;
    }
}
