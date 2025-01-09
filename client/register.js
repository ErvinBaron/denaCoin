document.getElementById("registrationForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    const fname = document.getElementById("fname").value;
    const lname = document.getElementById("lname").value;
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {
        const data = { fname, lname, email, password };
        console.log(JSON.stringify(data));
        

        const response = await fetch("http://localhost:8000/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(data),
        });

        if (!response.ok) throw new Error(`Registration failed: ${response.status}`);

        const result = await response.text();
        alert(result);

        // Redirect to user page after successful registration
        window.location.href = "/client/wallet.html"; // Replace with the actual URL of your user page
    } catch (error) {
        console.error("Error:", error);
        alert("Registration failed! Please try again.");
    }
});
