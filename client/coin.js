document.addEventListener('DOMContentLoaded', () => {
    // פונקציה להודעות
    function showMessage(message, type = 'error') {
        try {
            if (typeof message === 'string') {
                console[type](message); // ידפיס את ההודעה עם סוג מתאים ב-console
            } else {
                console[type](JSON.stringify(message)); // ידפיס את ההודעה כמבנה JSON
            }
        } catch (e) {
            console.error('An error occurred'); // במקרה של שגיאה
        }
    }
    // הגדרת גרף
    const labels = ['09:00', '10:00', '11:00', '12:00', '13:00'];
    const data = {
        labels: labels,
        datasets: [{
            label: 'Coin Value',
            data: [0.15, 0.30, 0.45, 1.00, 1.15],
            borderColor: 'rgba(75, 192, 192, 1)',
            backgroundColor: 'rgba(75, 192, 192, 0.2)',
            tension: 0.4,
        }]
    };

    const options = {
        responsive: true,
        plugins: {
            legend: { position: 'top' },
            title: {
                display: true,
                text: 'Coin Value Chart',
            },
        },
    };

    const chartElement = document.getElementById('cryptoChart');
    if (!chartElement) {
        showMessage('Chart canvas element not found!');
        return;
    }

    const ctx = chartElement.getContext('2d');
    const cryptoChart = new Chart(ctx, {
        type: 'line',
        data: data,
        options: {
            responsive: true,
            maintainAspectRatio: false,
        }
    });

    // אתחול משתנים UI
    const modal = document.getElementById('transactionModal');
    const closeBtn = document.querySelector('.close-modal');
    const confirmBtn = document.getElementById('confirmTransaction');
    const coinAmountInput = document.getElementById('coinAmount');
    const currentPriceSpan = document.getElementById('currentPrice');
    const totalCostSpan = document.getElementById('totalCost');
    const buyBtn = document.querySelector('.buy-btn');
    const sellBtn = document.querySelector('.sell-btn');
    const balanceElement = document.getElementById('userBalance');
    const userNameElement = document.getElementById('userName');
    
    if (!modal || !closeBtn || !confirmBtn || !coinAmountInput || 
        !currentPriceSpan || !totalCostSpan || !buyBtn || !sellBtn) {
        showMessage('One or more required elements are missing!');
        return;
    }

    let currentAction = '';
    let userBalance = 0;

    // קבלת יתרת המשתמש
    async function fetchUserBalance() {
        const userId = sessionStorage.getItem('user_id');
        if (!userId) {
            showMessage('Please log in first');
            return;
        }
    
        try {
            const response = await fetch(`http://localhost:8000/user-balance/${userId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                },
                mode: 'cors' 
            });
    
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
    
            const contentType = response.headers.get('content-type');
            if (!contentType || !contentType.includes('application/json')) {
                throw new Error('Server returned invalid JSON format');
            }
    
            const data = await response.json();
            if (!data.coin_balance && data.coin_balance !== 0) {
                throw new Error('Invalid balance data received');
            }
    
            userBalance = data.coin_balance;
            if (balanceElement) {
                balanceElement.textContent = userBalance.toFixed(2);
            }
            if (userNameElement && data.user_name) {
                userNameElement.textContent = data.user_name;
            }
        } catch (error) {
            showMessage('Error fetching balance: ' + error.message);
        }
    }
        // אירועים
    buyBtn.addEventListener('click', () => {
        currentAction = 'buy';
        document.getElementById('modalTitle').textContent = 'Buy DENA Coins';
        openModal();
    });

    sellBtn.addEventListener('click', () => {
        currentAction = 'sell';
        document.getElementById('modalTitle').textContent = 'Sell DENA Coins';
        openModal();
    });

    closeBtn.addEventListener('click', closeModal);
    window.addEventListener('click', (event) => {
        if (event.target === modal) closeModal();
    });

    coinAmountInput.addEventListener('input', updateTotalCost);

    confirmBtn.addEventListener('click', async () => {
        const userId = sessionStorage.getItem('user_id');
        const amount = parseInt(coinAmountInput.value);

        if (!userId) {
            showMessage('Please log in to perform transactions');
            closeModal();
            return;
        }

        if (!amount || isNaN(amount) || amount <= 0) {
            alert('Please enter a valid amount');
            return;
        }

        if (currentAction === 'sell' && amount > userBalance) {
            alert('Insufficient coin balance');
            return;
        }

        await postTransaction(currentAction, userId, amount);
    });

    // פונקציות למודלים
    function openModal() {
        modal.style.display = 'block';
        coinAmountInput.value = '';
        updateTotalCost();
    }

    function closeModal() {
        modal.style.display = 'none';
    }

    function updateTotalCost() {
        const amount = parseFloat(coinAmountInput.value) || 0;
        const price = parseFloat(currentPriceSpan.textContent);
        totalCostSpan.textContent = (amount * price).toFixed(2);
    }

    // טיפול בעסקאות
    async function postTransaction(action, userId, amount) {
        try {
            const response = await fetch('http://localhost:8000/transactions', {
                method: 'POST',
                headers: {
                  'Content-Type': 'application/json',
                  'Accept': 'application/json'
                },
                body: JSON.stringify({
                  userId,
                  amount,
                  action
                })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Transaction failed');
            }

            const data = await response.json();
            alert(`Successfully ${action}ed ${amount} coins! New balance: ${data.new_balance}`, 'success');
            userBalance = data.new_balance;

            if (balanceElement) {
                balanceElement.textContent = userBalance.toFixed(2);
            }
            closeModal();
            updateChart();
        } catch (error) {
            showMessage('Transaction error: ' + error.message);
        }
    }

    // עדכון גרף
    function updateChart() {
        const newValue = (Math.random() * 1 + 0.15).toFixed(2);
        const currentTime = new Date().toLocaleTimeString();
        
        cryptoChart.data.labels.push(currentTime);
        cryptoChart.data.datasets[0].data.push(newValue);
        
        if (cryptoChart.data.labels.length > 10) {
            cryptoChart.data.labels.shift();
            cryptoChart.data.datasets[0].data.shift();
        }

        cryptoChart.update();
        if (currentPriceSpan) {
            currentPriceSpan.textContent = newValue;
        }
        updateTotalCost();
    }

    // עדכון אוטומטי של הגרף כל 5 שניות
    setInterval(updateChart, 5000);

    // אתחול
    fetchUserBalance();
});
