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