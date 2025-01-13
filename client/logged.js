// const authButton = document.getElementById('auth-button');
// if (result.ok) {
//   authButton.style.display = 'none';
// }
document.addEventListener('DOMContentLoaded', () => {
    // Check if the user is logged in (session storage)
    const userLoggedIn = sessionStorage.getItem('user_id');  // Or any other data that indicates the user is logged in
  
    // Get references to the login and signup buttons
    const loginButton = document.querySelector('.auth-button.login');
    const signupButton = document.querySelector('.auth-button.signup');
  
    // If the user is logged in, hide the login and signup buttons
    if (userLoggedIn) {
      if (loginButton) loginButton.style.display = 'none';
      if (signupButton) signupButton.style.display = 'none';
    } else {
      if (loginButton) loginButton.style.display = 'inline-block';
      if (signupButton) signupButton.style.display = 'inline-block';
    }
  });
  