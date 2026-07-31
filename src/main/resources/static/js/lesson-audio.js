document.addEventListener('click', (event) => {
    const button = event.target.closest('[data-speak-text]');
    if (!button || typeof speechSynthesis === 'undefined') {
        return;
    }
    const utterance = new SpeechSynthesisUtterance(button.dataset.speakText);
    utterance.lang = 'en-US';
    speechSynthesis.cancel();
    speechSynthesis.speak(utterance);
});
