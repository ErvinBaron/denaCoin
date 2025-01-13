document
  .getElementById("registrationForm")
  .addEventListener("submit", async (event) => {
    event.preventDefault();

    const fname = document.getElementById("fname").value;
    const lname = document.getElementById("lname").value;
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {
      // Step 1: Request encryption key from the server
      const encryptionKey = await requestEncryptionKey();
      const data = { fname, lname, email, password };
      const jsonData = JSON.stringify(data);

      console.log(jsonData);
      const iv = CryptoJS.enc.Hex.parse("00000000000000000000000000000000"); // Fixed IV
      const key = CryptoJS.enc.Utf8.parse(encryptionKey); // Use key from server
      const encrypted = CryptoJS.AES.encrypt(jsonData, key, {
        iv: iv,
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7,
      }).toString();

      // Step 3: Send encrypted data to the server
      const result = await sendRegistraionData(encrypted);
      sessionStorage.setItem("fname", result.fname);
      sessionStorage.setItem("user_id", result.userId);

      // Handle server response
      if (result.user_id) {
        alert(result.message || "Registration successful!");
        window.location.href = "/client/wallet.html";
      } else {
        alert(result.message || "register failed. Please try again.");
      }
    } catch (error) {
      console.error("register process failed:", error);
      alert("register failed! Please try again.");
    }
  });

async function sendRegistraionData(encryptedData) {
  try {
    const response = await fetch("http://localhost:8000/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ data: encryptedData }),
    });
    console.log(response);
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
      throw new Error(
        `Failed to fetch encryption key. Status: ${response.status}`
      );
    }
    const { encryptionKey } = await response.json();
    return encryptionKey;
  } catch (error) {
    console.error("Error fetching encryption key:", error);
    throw error;
  }
}
