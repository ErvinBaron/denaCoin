document.getElementById("registrationForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    // Get values from the form inputs
    const fname = document.getElementById("fname").value;
    const lname = document.getElementById("lname").value;
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {
        // Send registration data
        const result = await sendregistrationData(fname, lname, email, password);
        console.log(result.message);
        alert("Registration successful!");
    } catch (error) {
        console.error("Failed to send data to server", error);
        alert("Registration failed! Please try again.");
    }
});

async function sendregistrationData(fname, lname, email, password) {
    try {
        const response = await fetch("http://localhost:8000/register", {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                firstName: fname,
                lastName: lname,
                email: email,
                password: password,
            }),
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.json(); // Parse and return the server response
    } catch (error) {
        console.error("Error in sendregistrationData:", error);
        throw error;
    }
}
