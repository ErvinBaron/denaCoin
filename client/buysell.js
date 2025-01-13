document.addEventListener('DOMContentLoaded', function() {
    const executeTrade = document.getElementById('executeTrade');
    const coinAmount = document.getElementById('coinAmount');
    const operationType = document.getElementById('operationType');
    const tradeResult = document.getElementById('tradeResult');

    executeTrade.addEventListener('click', function() {
        const amount = parseFloat(coinAmount.value);
        const operation = operationType.value;

        if (isNaN(amount) || amount <= 0) {
            tradeResult.textContent = 'Please enter a valid amount.';
            return;
        }

        // Send the trade request to the server
        fetch('/coin', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                operation: operation,
                amount: amount
            })
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                tradeResult.textContent = `Successfully ${operation}ed ${amount} DENA coins.`;
                updateBalance(data.newBalance);
            } else {
                tradeResult.textContent = data.message || 'Transaction failed. Please try again.';
            }
        })
        .catch(error => {
            console.error('Error:', error);
            tradeResult.textContent = 'An error occurred. Please try again.';
        });
    });

    function updateBalance(newBalance) {
        const balanceAmount = document.getElementById('balanceAmount');
        balanceAmount.textContent = `${newBalance} DC`;
    }
});