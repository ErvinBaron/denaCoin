document.addEventListener("DOMContentLoaded", function () {
  const transferButton = document.querySelector(".transfer_button");
  const transferForm = document.querySelector(".transfer_form");
  const maincontainer = document.querySelector(".main_container");

  transferButton.addEventListener("click", function () {
    transferForm.classList.toggle("active");
    maincontainer.classList.toggle("expanded");

    if (transferForm.classList.contains("active")) {
      transferButton.textContent = "Hide Transfer Form";
    } else {
      transferButton.textContent = "Transfer";
    }
  });
});

document.addEventListener("DOMContentLoaded", () => {
  initializeWalletPage();
  displayStoredUserId();
});

document.addEventListener("DOMContentLoaded", () => {
  updateUserName();
  fetchAndDisplayAccountBalance();
  fetchAndDisplayAccountHistory();
});

// Update the user's name in the hello_div
function updateUserName() {
  const helloDiv = document.querySelector(".hello_div");
  const fname = sessionStorage.getItem("fname");
  const id = sessionStorage.getItem("user_id");

  if (fname) {
    helloDiv.innerHTML = `Hi, ${fname}!<br><span class="small-text">Wallet id: ${id}</span>`;
  } else {
    helloDiv.textContent = "Hi, Guest!";
    console.warn("First name not found in sessionStorage.");
  }
}

// Fetch and display the user's account balance
export async function fetchAndDisplayAccountBalance() {
  const fname = sessionStorage.getItem("fname");
  const balanceAmountDiv = document.getElementById("balanceAmount");

  if (!fname) {
    console.error("First name not found in sessionStorage.");
    balanceAmountDiv.textContent = "Error fetching balance.";
    return;
  }

  try {
    const response = await fetch(`http://localhost:8000/getBalance`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name: fname }),
    });
    if (!response.ok) {
      throw new Error(
        `Failed to fetch balance. HTTP status: ${response.status}`
      );
    }

    const result = await response.json();

    if (result && result.balance !== undefined) {
      balanceAmountDiv.textContent = `${result.balance.toFixed(2)} $DENA `;
    } else {
      console.error("Invalid balance data received:", result);
      balanceAmountDiv.textContent = "Error fetching balance.";
    }
  } catch (error) {
    console.error("Error fetching balance:", error);
    balanceAmountDiv.textContent = "Error fetching balance.";
  }
}
async function fetchAndDisplayAccountHistory() {
  const user_id = sessionStorage.getItem("user_id");

  try {
    const response = await fetch(`http://localhost:8000/getHistory`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ user_id: user_id }),
    });

    if (!response.ok) {
      throw new Error(
        `Failed to fetch history. HTTP status: ${response.status}`
      );
    }

    // Parse the response as JSON
    const historyResult = await response.json();

    // Select the table body where the history will be displayed
    const tableBody = document.getElementById("transactionHistory");

    // Clear any existing content in the table body
    tableBody.innerHTML = "";

    // Check if historyResult contains transactions
    if (historyResult && historyResult.length > 0) {
      // Iterate through the history array and create table rows for each transaction
      historyResult.forEach((transaction) => {
        const row = document.createElement("tr");

        row.innerHTML = `
          <td>${transaction.timestamp}</td>
          <td>${transaction.senderID === user_id ? "Sent" : "Received"}</td>
          <td>${transaction.amount}</td>
          <td>${
            transaction.senderID === user_id
              ? transaction.receiverID
              : transaction.senderID
          }</td>
        `;

        tableBody.appendChild(row);
      });
    } else {
      // If no history is found, display a message
      const row = document.createElement("tr");
      row.innerHTML = `<td colspan="4">No transaction history available.</td>`;
      tableBody.appendChild(row);
    }
  } catch (error) {
    console.error("Error fetching history:", error);

    // Display an error message to the user
    const tableBody = document.getElementById("transactionHistory");
    tableBody.innerHTML = `<tr><td colspan="4">Error fetching transaction history.</td></tr>`;
  }
}
