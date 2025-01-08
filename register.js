const crypto = require('crypto');

// Fetch the server's public key
async function fetchPublicKey() {
    const response = await fetch("http://localhost:8000/getPublicKey");
    if (!response.ok) {
        throw new Error(`Failed to fetch public key: ${response.status}`);
    }
    return await response.text(); // The public key as a Base64-encoded string
}

// Encrypt data using the server's public key
function encryptData(data, publicKeyBase64) {
    const publicKey = crypto.createPublicKey({
        key: Buffer.from(publicKeyBase64, 'base64'),
        format: 'der',
        type: 'spki',
    });

    const encrypted = crypto.publicEncrypt(
        {
            key: publicKey,
            padding: crypto.constants.RSA_PKCS1_PADDING,
        },
        Buffer.from(JSON.stringify(data))
    );

    return encrypted.toString('base64');
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
        // Fetch the server's public key
        const publicKey = await fetchPublicKey();

        // Encrypt the registration data
        const data = { fname, lname, email, password };
        const encryptedData = encryptData(data, publicKey);

        // Send encrypted data to the server
        const response = await fetch("http://localhost:8000/register", {
            method: 'POST',
            headers: { 'Content-Type': 'text/plain' },
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
