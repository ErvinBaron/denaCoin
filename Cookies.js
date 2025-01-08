
// Function to save placeholder data to cookie
function savePlaceholderData() {
    const placeholderData = {
        walletId: '123456',
        name: 'John Doe',
        balance: 1000,
        isLoggedIn: true
    };

    sessionStorage.setItem('userData', JSON.stringify(placeholderData));
    console.log('Placeholder data saved to sessionStorage:', placeholderData);
}

// Function to check if user is logged in
function isLoggedIn() {
    const userData = sessionStorage.getItem('userData');
    if (userData) {
        const parsedUserData = JSON.parse(userData);
        return parsedUserData.isLoggedIn === true;
    }
    return false;
}

// Function to update links
function updateLinks() {
    const homeLinks = document.querySelectorAll('a[href="./index.html"], a[href="index.html"], a[href="/index.html"]');
    homeLinks.forEach(link => {
        const oldHref = link.href;
        link.href = link.href.replace('index.html', 'indexloggedin.html');
        console.log(`Updated link: ${oldHref} -> ${link.href}`);
    });
}

// Function to check login and update links
function checkLoginAndUpdateLinks() {
    if (isLoggedIn()) {
        console.log('User is logged in. Updating links...');
        updateLinks();
    } else {
        console.log('User is not logged in. Links remain unchanged.');
    }
}

// Main function to orchestrate the process
function initializeAndCheckLogin() {
    savePlaceholderData();
    checkLoginAndUpdateLinks();
}
if (typeof window !== 'undefined') {
    window.addEventListener('DOMContentLoaded', initializeAndCheckLogin);
} else {
    // If running in a non-browser environment (e.g., Node.js)
    console.log('Running in a non-browser environment');
}

// Example of how to use these functions individually
console.log('Is user logged in?', isLoggedIn());

// Export functions if needed
export { savePlaceholderData, isLoggedIn, updateLinks, checkLoginAndUpdateLinks, initializeAndCheckLogin };