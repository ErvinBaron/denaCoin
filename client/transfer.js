import { fetchAndDisplayAccountBalance } from "./wallet.js";

document
  .getElementById("transferFormElement")
  .addEventListener("submit", async (event) => {
    event.preventDefault();

    console.log("hiii");

    const fname = sessionStorage.getItem("fname");
    const recipientAddress = document.getElementById("recipientAddress").value;
    const transferAmount = parseFloat(
      document.getElementById("transferAmount").value
    );

    try {
      const response = await fetch("http://localhost:8000/transfer", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          senderName: fname,
          receiverID: recipientAddress,
          amount: transferAmount,
        }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const result = await response.json();
      document.getElementById("transferMessage").textContent = result.message;

      if (result.message === "Transaction successful!") {
        alert("Transaction successful!");
        fetchAndDisplayAccountBalance();
        location.reload();
      }
    } catch (error) {
      console.error("Transaction failed:", error);
      document.getElementById("transferMessage").textContent =
        "Transaction failed! Please try again.";
    }
  });
