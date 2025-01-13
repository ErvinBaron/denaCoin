// document
//   .getElementById("loginForm")
//   .addEventListener("submit", async (event) => {
//     event.preventDefault();

//     const email = document.getElementById("email").value;
//     const password = document.getElementById("password").value;

//     try {
//       const encryptionKey = await requestEncryptionKey();
//       const jsonData = JSON.stringify({ email, password }); //json object as string
//       const dataEncryptionAES = await aesEncryption(jsonData, encryptionKey);
//       const result = await sendLoginData(dataEncryptionAES);

//       // Check if the login is successful
//       if (result.message === "Login successful!") {
//         alert(result.message); // Display success message
//         window.location.href = "/client/wallet.html"; // Redirect to wallet page
//       } else {
//         alert(result.message || "Login failed. Please try again.");
//       }
//     } catch (error) {
//       console.error("Failed to send data to server:", error);
//       alert("Login failed! Please try again.");
//     }
//   });
document.getElementById("loginForm").addEventListener("submit", async (event) => {
    event.preventDefault();
  
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
  
    try {
      // Step 1: Request encryption key from the server
      const encryptionKey = await requestEncryptionKey();
  
      // Step 2: Encrypt data using the received key
      const jsonData = JSON.stringify({ email, password });
      const iv = CryptoJS.enc.Hex.parse("00000000000000000000000000000000"); // Fixed IV
      const key = CryptoJS.enc.Utf8.parse(encryptionKey); // Use key from server
      const encrypted = CryptoJS.AES.encrypt(jsonData, key, {
        iv: iv,
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7,
      }).toString();
  
      // Step 3: Send encrypted data to the server
      const result = await sendLoginData(encrypted);
      ////////////////////////////////////////////////////
      // if(result.ok){
      //   button.style.display = 'none';
      // }
      ////////////////////////////////////////////////////
      // Handle server response
      sessionStorage.setItem("fname", result.fname);
      sessionStorage.setItem("user_id", result.user_id);
      console.log(sessionStorage.getItem("user_id"));
      if (result.message === "Login successful!") {
        alert(result.message);
        window.location.href = "/client/wallet.html";
      } else {
        alert(result.message || "Login failed. Please try again.");
      }
    } catch (error) {
      console.error("Login process failed:", error);
      alert("Login failed! Please try again.");
    }
  });

async function sendLoginData(encryptedData) {
  try {
    const response = await fetch("http://localhost:8000/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ data: encryptedData }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json(); // Parse and return JSON response
  } catch (error) {
    console.error("Error in sendLoginData:", error);
    throw error;
  }
}

async function aesEncryption(data, encryptionKey) {
  const SECRET_KEY = encryptionKey; // Must be 16 bytes for AES-128
  const iv = CryptoJS.enc.Hex.parse("00000000000000000000000000000000"); // 16 bytes of zero IV
  const key = CryptoJS.enc.Utf8.parse(SECRET_KEY); // Treat key as raw bytes

  const encrypted = CryptoJS.AES.encrypt(data, key, {
    iv: iv,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.Pkcs7, // Default in CryptoJS
  });
  console.log("Encrypted Data:", encrypted.toString());

  return encrypted.toString(); // Base64 encoded ciphertext
}

async function requestEncryptionKey() {
    try {
      const response = await fetch("http://localhost:8000/get-key", {
        method: "GET",
      });
      if (!response.ok) {
        throw new Error(`Failed to fetch encryption key. Status: ${response.status}`);
      }
      const { encryptionKey } = await response.json();
      return encryptionKey;
    } catch (error) {
      console.error("Error fetching encryption key:", error);
      throw error;
    }
  }