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

function initializeWalletPage() {
    displayUserGreeting();
    displayUserBalance();

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

    // Handle form submission
    transferFormElement.addEventListener('submit', function(e) {
        e.preventDefault();
        const recipientAddress = document.getElementById('recipientAddress').value;
        const transferAmount = parseFloat(document.getElementById('transferAmount').value);

        // Update balance (in a real app, you'd wait for server confirmation)
        let currentBalance = parseFloat(balanceAmount.textContent);
        if (currentBalance >= transferAmount) {
            currentBalance -= transferAmount;
            balanceAmount.textContent = `${currentBalance.toFixed(2)} DENA`;

            // Add new transaction to history
            const newRow = document.createElement('tr');
            newRow.innerHTML = `
                <td>${new Date().toISOString().split('T')[0]}</td>
                <td>Sent</td>
                <td>${transferAmount.toFixed(2)} DENA</td>
                <td>${recipientAddress}</td>
            `;
            transactionHistory.insertBefore(newRow, transactionHistory.firstChild);

            // Reset and hide form, show history
            this.reset();
            transferForm.style.display = 'none';
            historyContainer.style.display = 'block';
        } else {
            alert('Insufficient balance');
        }
    });

    // Initialize with some example transaction history
    addExampleTransactions();
}

function addExampleTransactions() {
    const transactionHistory = document.getElementById('transactionHistory');
    const exampleTransactions = [
        { date: '2023-05-01', type: 'Received', amount: 500, address: '0x123...'},
        { date: '2023-05-02', type: 'Sent', amount: 200, address: '0x456...'},
    ];

    exampleTransactions.forEach(transaction => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${transaction.date}</td>
            <td>${transaction.type}</td>
            <td>${transaction.amount.toFixed(2)} DENA</td>
            <td>${transaction.address}</td>
        `;
        transactionHistory.appendChild(row);
    });
}

// Run the initialization function when the DOM is fully loaded
document.addEventListener('DOMContentLoaded', initializeWalletPage);