const labels = ['09:00', '10:00', '11:00', '12:00', '13:00'];
const data = {
    labels: labels,
    datasets: [
        {
            label: 'coin value',
            data: [1.00, 1.05, 1.02, 1.10, 1.08],
            borderColor: 'rgba(75, 192, 192, 1)',
            backgroundColor: 'rgba(75, 192, 192, 0.2)',
            tension: 0.4, // קימור הקו
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

const ctx = document.getElementById('cryptoChart').getContext('2d');
const cryptoChart = new Chart(ctx, { // כאן היה השינוי
    type: 'line', 
    data: data,
    options: options,
});

setInterval(() => {
    const newValue = Math.floor(Math.random() * 10) + 100; // דוגמת ערך חדש אקראי
    const currentTime = new Date().toLocaleTimeString(); // הזמן הנוכחי

    // הוספת ערכים חדשים לגרף
    cryptoChart.data.labels.push(currentTime);
    cryptoChart.data.datasets[0].data.push(newValue);

    // שמירה על תצוגת גרף בגודל קבוע (למשל, רק 10 ערכים אחרונים)
    if (cryptoChart.data.labels.length > 10) {
        cryptoChart.data.labels.shift(); // הסרת הערך הראשון
        cryptoChart.data.datasets[0].data.shift(); // הסרת הנתון הראשון
    }

    cryptoChart.update(); // עדכון הגרף
}, 5000); // כל 5 שניות
