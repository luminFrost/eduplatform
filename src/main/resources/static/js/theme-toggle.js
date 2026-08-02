function isDarkActive() {
    const explicit = document.documentElement.getAttribute('data-theme');
    if (explicit) {
        return explicit === 'dark';
    }
    return window.matchMedia('(prefers-color-scheme: dark)').matches;
}

function updateToggleLabel() {
    const button = document.getElementById('theme-toggle');
    if (button) {
        button.textContent = isDarkActive() ? '☀️' : '🌙';
    }
}

const stored = localStorage.getItem('theme');
if (stored) {
    document.documentElement.setAttribute('data-theme', stored);
}
updateToggleLabel();

document.addEventListener('click', (event) => {
    const button = event.target.closest('#theme-toggle');
    if (!button) {
        return;
    }
    const next = isDarkActive() ? 'light' : 'dark';
    localStorage.setItem('theme', next);
    document.documentElement.setAttribute('data-theme', next);
    updateToggleLabel();
});
