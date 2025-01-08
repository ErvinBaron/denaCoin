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

// Submit transaction form
document.getElementById("transactionForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    // Get values from the form
    const sender = document.getElementById("sender").value;
    const recipient = document.getElementById("to").value;
    const amount = document.getElementById("amount").value;

    try {
        // Fetch the server's public key
        const publicKey = await fetchPublicKey();

        // Encrypt the transaction data
        const data = { sender, recipient, amount: Number(amount) };
        const encryptedData = encryptData(data, publicKey);

        // Send encrypted data to the server
        const response = await fetch("http://localhost:8000/addTransaction", {
            method: 'POST',
            headers: { 'Content-Type': 'text/plain' },
            body: encryptedData,
        });

        if (!response.ok) {
            throw new Error(`Transaction failed: ${response.status}`);
        }

        const result = await response.json();
        console.log(result)
        } catch (error) {
        console.error("Transaction failed:", error);
        alert("Transaction failed to complete!");
    }finally{
        location.reload();
    }
});


function displayUserGreeting() {
    const helloDiv = document.querySelector('.hello_div');
    
    // Read the userData from sessionStorage
    const userDataString = sessionStorage.getItem('userData');
    
    if (userDataString) {
        try {
            const userData = JSON.parse(userDataString);
            const userName = userData.name;
            
            if (userName) {
                helloDiv.textContent = `Hello, ${userName}!`;
            } else {
                helloDiv.textContent = 'Hello, User!';
            }
        } catch (error) {
            console.error('Error parsing user data from sessionStorage:', error);
            helloDiv.textContent = 'Hello, User!';
        }
    } else {
        helloDiv.textContent = 'Hello, Guest!';
    }
}
function displayUserBalance() {
    const balanceElement = document.querySelector('.balance_amount');
    
    // Read the userData from sessionStorage
    const userDataString = sessionStorage.getItem('userData');
    
    if (userDataString) {
        try {
            const userData = JSON.parse(userDataString);
            const balance = userData.balance;
            
            if (balance !== undefined) {
                balanceElement.textContent = `${balance} DENA`;
            } else {
                balanceElement.textContent = '0 DENA';
            }
        } catch (error) {
            console.error('Error parsing user data from sessionStorage:', error);
            balanceElement.textContent = '0 DENA';
        }
    } else {
        balanceElement.textContent = '0 DENA';
    }
}

// Function to initialize the wallet page
function initializeWalletPage() {
    displayUserGreeting();
    displayUserBalance();
}

// Run the initialization function when the DOM is fully loaded
document.addEventListener('DOMContentLoaded', initializeWalletPage);
// Run the function when the DOM is fully loaded
document.addEventListener('DOMContentLoaded', displayUserGreeting);

