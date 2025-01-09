import CryptoJS from "crypto-js";

const SECRET_KEY = "1234567890123456"; // Must match the backend key

// Encrypt data using AES
function encryptData(data) {
    const jsonData = JSON.stringify(data);
    const encrypted = CryptoJS.AES.encrypt(jsonData, SECRET_KEY).toString();
    return encrypted;
}

// Submit registration data
document.getElementById("registrationForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    // Get form inputs
    const fname = document.getElementById("fname").value;
    const lname = document.getElementById("lname").value;
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {
        // Prepare the JSON payload
        const data = { fname, lname, email, password };

        // Encrypt the registration data
        const encryptedData = encryptData(data);

        // Send encrypted data to the server
        const response = await fetch("http://localhost:8000/register", {
            method: "POST",
            headers: { "Content-Type": "text/plain" },
            body: encryptedData,
        });

        if (!response.ok) {
            throw new Error(`Registration failed: ${response.status}`);
        }

        const result = await response.text();
        alert(result);
    } catch (error) {
        console.error("Error:", error);
        alert("Registration failed! Please try again.");
    }
});
