document.getElementById("transactionForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    // Get values from the form
    const sender = document.getElementById("sender").value;
    const recipient = document.getElementById("to").value;
    const amount = document.getElementById("amount").value;

    try {
        // Call the sendTransaction function
        const result = await sendTransaction(sender, recipient, amount);
        console.log(result.message);
    } catch (error) {
        console.error("Transaction failed:", error);
        alert("Transaction failed to complete!");
    }
});

async function sendTransaction(sender, recipient, amount) {
    try {
        const response = await fetch("http://localhost:8000/addTransaction", {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                from: sender,
                to: recipient,
                amount: Number(amount), // Ensure amount is properly formatted
            }),
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        // Return the result of the transaction
        return await response.json();
    } catch (error) {
        console.error("Error in sendTransaction:", error);
        throw error;
    }
}
