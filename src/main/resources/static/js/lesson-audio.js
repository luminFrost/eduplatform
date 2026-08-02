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

document.addEventListener('click', (event) => {
    const button = event.target.closest('[data-check-text]');
    if (!button) {
        return;
    }
    const Recognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    const resultEl = button.parentElement.querySelector('[data-speech-result]');
    if (!Recognition) {
        resultEl.textContent = '이 브라우저는 음성 인식을 지원하지 않아요.';
        resultEl.className = 'speech-result';
        return;
    }

    const recognition = new Recognition();
    recognition.lang = 'en-US';
    recognition.maxAlternatives = 1;

    resultEl.textContent = '듣고 있어요… 🎙️';
    resultEl.className = 'speech-result';

    recognition.onresult = (event) => {
        const heard = event.results[0][0].transcript;
        const normalize = (s) => s.toLowerCase().replace(/[.,!?]/g, '').replace(/\s+/g, ' ').trim();
        if (normalize(heard) === normalize(button.dataset.checkText)) {
            resultEl.textContent = `✅ 잘 말했어요! ("${heard}")`;
            resultEl.className = 'speech-result correct';
        } else {
            resultEl.textContent = `다시 말해볼까요? 인식된 말: "${heard}"`;
            resultEl.className = 'speech-result incorrect';
        }
    };
    recognition.onerror = () => {
        resultEl.textContent = '마이크를 인식하지 못했어요. 권한을 확인해주세요.';
        resultEl.className = 'speech-result incorrect';
    };

    recognition.start();
});
