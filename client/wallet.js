function initializeWalletPage() {
  displayUserGreeting();
  displayUserBalance();

  const transferButton = document.getElementById("transferButton");
  const transferForm = document.getElementById("transferForm");
  const transferFormElement = document.getElementById("transferFormElement");
  const balanceAmount = document.getElementById("balanceAmount");
  const transactionHistory = document.getElementById("transactionHistory");
  const historyContainer = document.querySelector(".history_container");

  // Hide transaction history initially
  historyContainer.style.display = "none";

  // Toggle transfer form visibility
  transferButton.addEventListener("click", function () {
    if (transferForm.style.display === "none") {
      transferForm.style.display = "block";
      historyContainer.style.display = "none";
    } else {
      transferForm.style.display = "none";
      historyContainer.style.display = "block";
    }
  });

  // Handle form submission
  transferFormElement.addEventListener("submit", function (e) {
    e.preventDefault();
    const recipientAddress = document.getElementById("recipientAddress").value;
    const transferAmount = parseFloat(
      document.getElementById("transferAmount").value
    );

    // Update balance (in a real app, you'd wait for server confirmation)
    let currentBalance = parseFloat(balanceAmount.textContent);
    if (currentBalance >= transferAmount) {
      currentBalance -= transferAmount;
      balanceAmount.textContent = `${currentBalance.toFixed(2)} DENA`;

      // Add new transaction to history
      const newRow = document.createElement("tr");
      newRow.innerHTML = `
                <td>${new Date().toISOString().split("T")[0]}</td>
                <td>Sent</td>
                <td>${transferAmount.toFixed(2)} DENA</td>
                <td>${recipientAddress}</td>
            `;
      transactionHistory.insertBefore(newRow, transactionHistory.firstChild);

      // Reset and hide form, show history
      this.reset();
      transferForm.style.display = "none";
      historyContainer.style.display = "block";
    } else {
      alert("Insufficient balance");
    }
  });

  // Initialize with some example transaction history
  addExampleTransactions();
}
function initializeWalletPage() {
    displayUserGreeting();
    displayUserBalance();}


    const transferButton = document.getElementById('transferButton');
    const transferForm = document.getElementById('transferForm');
    const transferFormElement = document.getElementById('transferFormElement');
    const balanceAmount = document.getElementById('balanceAmount');
    const transactionHistory = document.getElementById('transactionHistory');
    const historyContainer = document.querySelector('.history_container');

    // Hide transaction history initially
    historyContainer.style.display = 'none';

    // Toggle transfer form visibility
    transferButton.addEventListener('click', function() {
        if (transferForm.style.display === 'none') {
            transferForm.style.display = 'block';
            historyContainer.style.display = 'none';
        } else {
            transferForm.style.display = 'none';
            historyContainer.style.display = 'block';
        }
    });
    function fetchTransactionHistory() {
        const userDataString = sessionStorage.getItem('userData');
        if (userDataString) {
            const userData = JSON.parse(userDataString);
            const userId = userData.id;
    
            // Make a POST request to the server to get transaction history
            fetch('/coin/transaction', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ userId: userId }),
            })
            .then(response => response.json())
            .then(data => {
                if (data && Array.isArray(data)) {
                    addTransactions(data);
                } else {
                    console.error('Invalid transaction data received');
                }
            })
            .catch(error => {
                console.error('Error fetching transaction history:', error);
            });
        }
    }
    function addTransactions(transactions) {
        const transactionHistory = document.getElementById('transactionHistory');
        transactionHistory.innerHTML = ''; // Clear existing transactions
    
        transactions.forEach(transaction => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${transaction.date}</td>
                <td>${transaction.type}</td>
                <td>${parseFloat(transaction.amount).toFixed(2)} DENA</td>
                <td>${transaction.address || '-'}</td>
            `;
            transactionHistory.appendChild(row);
        });
    }



// Run the initialization function when the DOM is fully loaded
document.addEventListener("DOMContentLoaded", initializeWalletPage);

document
  .getElementById("registrationForm")
  .addEventListener("submit", async (event) => {
    event.preventDefault();

    const fname = document.getElementById("fname").value;
    const lname = document.getElementById("lname").value;
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {
      const response = await fetch("http://localhost:8000/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ fname, lname, email, password }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const result = await response.json();
      console.log("DEBUG: " + result);
      if (result.user_id) {
        // Save the user_id in localStorage
        localStorage.setItem("user_id", result.user_id);

        alert("Registration successful!");
        window.location.href = "/wallet.html"; // Redirect to wallet page
      } else {
        alert(result.message || "Registration failed.");
      }
    } catch (error) {
      console.error("Registration failed:", error);
    }
  });
const userId = localStorage.getItem("user_id");
if (userId) {
  console.log("User ID:", userId);
} else {
  console.log("No user ID found in localStorage.");
}

async function fetchUserName() {
  const userId = localStorage.getItem("user_id"); // Retrieve user_id from localStorage

  if (!userId) {
    console.error("User ID not found in localStorage");
    return "Guest";
  }

  try {
    const response = await fetch("http://localhost:8000/getUserName", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ user_id: userId }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const result = await response.json();
    return result.name || "Guest"; // Default to "Guest" if name not found
  } catch (error) {
    console.error("Failed to fetch user name:", error);
    return "Guest";
  }
}

function displayUserGreeting() {
  const helloDiv = document.querySelector(".hello_div");

  // Read the userData from sessionStorage
  const userDataString = sessionStorage.getItem("userData");

  if (userDataString) {
    try {
      const userData = JSON.parse(userDataString);
      const userName = userData.name;

      if (userName) {
        helloDiv.textContent = `Hello, ${userName}!`;
      } else {
        helloDiv.textContent = "Hello, User!";
      }
    } catch (error) {
      console.error("Error parsing user data from sessionStorage:", error);
      helloDiv.textContent = "Hello, User!";
    }
  } else {
    helloDiv.textContent = "Hello, Guest!";
  }
}

function displayUserBalance() {
  const balanceElement = document.querySelector(".balance_amount");

  // Read the userData from sessionStorage
  const userDataString = sessionStorage.getItem("userData");

  if (userDataString) {
    try {
      const userData = JSON.parse(userDataString);
      const balance = userData.balance;

      if (balance !== undefined) {
        balanceElement.textContent = `${balance} DENA`;
      } else {
        balanceElement.textContent = "0 DENA";
      }
    } catch (error) {
      console.error("Error parsing user data from sessionStorage:", error);
      balanceElement.textContent = "0 DENA";
    }
  } else {
    balanceElement.textContent = "0 DENA";
  }
}
