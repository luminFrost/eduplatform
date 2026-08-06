document.addEventListener('click', (event) => {
    const button = event.target.closest('[data-print-certificate]');
    if (!button) {
        return;
    }
    window.print();
});
