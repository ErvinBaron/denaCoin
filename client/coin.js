function showErrorMessage(message) {
    const errorMessageElement = document.getElementById('errorMessage');
    errorMessageElement.querySelector('.error__title').textContent = message;
    errorMessageElement.style.display = 'flex';
    
    // Add show class for animation
    errorMessageElement.classList.add('show');

    // Auto-hide after 3 seconds
    setTimeout(() => {
        closeErrorMessage();
    }, 3000);
}

function closeErrorMessage() {
    const errorMessageElement = document.getElementById('errorMessage');
    
    // Add animation
    errorMessageElement.style.animation = 'slideOut 0.3s ease';
    
    // Wait for animation to complete before hiding
    setTimeout(() => {
        errorMessageElement.style.display = 'none';
        errorMessageElement.classList.remove('show');
        errorMessageElement.style.animation = '';
    }, 300);
}

document.addEventListener('DOMContentLoaded', () => {
    // Setup error message close button
    const errorCloseBtn = document.querySelector('.error__close');
    if (errorCloseBtn) {
        errorCloseBtn.addEventListener('click', closeErrorMessage);
    }

    const labels = ['09:00', '10:00', '11:00', '12:00', '13:00'];
    const data = {
        labels: labels,
        datasets: [
            {
                label: 'coin value',
                data: [0.15, 0.30, 0.45, 1.00, 1.15],
                borderColor: 'rgba(75, 192, 192, 1)',
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                tension: 0.4,
            },
        ]
    };

    const options = {
        responsive: true,
        plugins: {
            legend: {
                position: 'top',
            },
            title: {
                display: true,
                text: 'Coin Value',
            },
        },
    };

    const chartElement = document.getElementById('cryptoChart');
    if (!chartElement) {
        showErrorMessage('Chart canvas element not found!');
        return;
    }

    const ctx = chartElement.getContext('2d');
    const cryptoChart = new Chart(ctx, {
        type: 'line',
        data: data,
        options: options,
    });

    // Modal elements
    const modal = document.getElementById('transactionModal');
    const closeBtn = document.querySelector('.close-modal');
    const confirmBtn = document.getElementById('confirmTransaction');
    const coinAmountInput = document.getElementById('coinAmount');
    const currentPriceSpan = document.getElementById('currentPrice');
    const totalCostSpan = document.getElementById('totalCost');
    const buyBtn = document.querySelector('.buy-btn');
    const sellBtn = document.querySelector('.sell-btn');

    // Check if all elements exist
    if (!modal || !closeBtn || !confirmBtn || !coinAmountInput ||
        !currentPriceSpan || !totalCostSpan || !buyBtn || !sellBtn) {
        showErrorMessage('One or more required elements are missing!');
        return;
    }

    let currentAction = '';

    // Chart update interval
    setInterval(() => {
        const newValue = (Math.random() * 1) + 0.15; 
        const currentTime = new Date().toLocaleTimeString();
    
        cryptoChart.data.labels.push(currentTime);
        cryptoChart.data.datasets[0].data.push(newValue.toFixed(2));     
        if (cryptoChart.data.labels.length > 10) {
            cryptoChart.data.labels.shift();
            cryptoChart.data.datasets[0].data.shift();
        }
    
        cryptoChart.update();
    }, 5000);
    // Event Listeners
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
        if (event.target === modal) {
            closeModal();
        }
    });

    coinAmountInput.addEventListener('input', updateTotalCost);

    confirmBtn.addEventListener('click', () => {
        const userId = sessionStorage.getItem('userId');
        const amount = parseInt(coinAmountInput.value);

        if (!userId) {
            showErrorMessage('Please log in to perform transactions');
            closeModal();
            return;
        }

        if (!amount || isNaN(amount) || amount <= 0) {
            showErrorMessage('Please enter a valid amount');
            return;
        }

        postTransaction(currentAction, userId, amount);
    });

    // Functions
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

    function postTransaction(action, userId, amount) {
        const url = '/coin';
        const data = {
            userId: userId,
            amount: amount,
            action: action
        };

        fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(responseJson => {
            handleTransactionResponse(responseJson, action);
            closeModal();
        })
        .catch(error => {
            showErrorMessage(`Transaction failed: ${error.message}`);
        });
    }

    function handleTransactionResponse(response, action) {
        if (response.error) {
            showErrorMessage(response.error);
        } else {
            alert(response.message);
            updateCoinChart();
        }
    }

    function updateCoinChart() {
        const currentTime = new Date().toLocaleTimeString();
        const newValue = Math.floor(Math.random() * 10) + 100;

        cryptoChart.data.labels.push(currentTime);
        cryptoChart.data.datasets[0].data.push(newValue);

        if (cryptoChart.data.labels.length > 10) {
            cryptoChart.data.labels.shift();
            cryptoChart.data.datasets[0].data.shift();
        }

        cryptoChart.update();
    }
});