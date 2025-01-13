document.addEventListener('DOMContentLoaded', function() {
  const transferButton = document.querySelector('.transfer_button');
  const transferForm = document.querySelector('.transfer_form');
  const maincontainer = document.querySelector('.main_container');

  transferButton.addEventListener('click', function() {
      transferForm.classList.toggle('active');
      maincontainer.classList.toggle('expanded');

      if (transferForm.classList.contains('active')) {
          transferButton.textContent = 'Hide Transfer Form';
      } else {
          transferButton.textContent = 'Transfer';
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
});

// Update the user's name in the hello_div
function updateUserName() {
  const helloDiv = document.querySelector(".hello_div");
  const fname = sessionStorage.getItem("fname");
  const id = sessionStorage.getItem("user_id");
  if (fname) {
    helloDiv.textContent = `Hi, ${fname}! id: ${id}`;
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
