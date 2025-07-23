// Language data
const libreLanguages = [
  { code: "en", name: "English" },
  { code: "sq", name: "Albanian" },
  { code: "ar", name: "Arabic" },
  { code: "az", name: "Azerbaijani" },
  { code: "eu", name: "Basque" },
  { code: "bn", name: "Bengali" },
  { code: "bg", name: "Bulgarian" },
  { code: "ca", name: "Catalan" },
  { code: "zh-Hans", name: "Chinese (Simplified)" },
  { code: "zh-Hant", name: "Chinese (Traditional)" },
  { code: "cs", name: "Czech" },
  { code: "da", name: "Danish" },
  { code: "nl", name: "Dutch" }
];

const fullLanguages = [
  { code: "en", name: "English" },
  { code: "si", name: "Sinhala" },
  { code: "es", name: "Spanish" },
  { code: "fr", name: "French" },
  { code: "de", name: "German" },
  { code: "it", name: "Italian" },
  { code: "pt", name: "Portuguese" },
  { code: "ru", name: "Russian" },
  { code: "zh", name: "Chinese" },
  { code: "ja", name: "Japanese" },
  { code: "ko", name: "Korean" }
];

// Utility to populate language dropdowns
function populateLanguageDropdown(dropdown, languages, includeAuto = false) {
  dropdown.innerHTML = '';
  if (includeAuto) {
    const option = document.createElement('option');
    option.value = 'auto';
    option.textContent = 'Auto-detect';
    dropdown.appendChild(option);
  }
  languages.forEach(lang => {
    const option = document.createElement('option');
    option.value = lang.code;
    option.textContent = lang.name;
    dropdown.appendChild(option);
  });
}

document.addEventListener('DOMContentLoaded', function () {
  const translateBtn = document.getElementById('translateBtn');
  const downloadBtn = document.getElementById('downloadBtn');
  const statusMessage = document.getElementById('statusMessage');
  const subtitleFile = document.getElementById('subtitleFile');
  const sourceLanguage = document.getElementById('sourceLanguage');
  const targetLanguage = document.getElementById('targetLanguage');
  const engineSelect = document.getElementById('engine');

  let translatedContent = null;

  // Init dropdowns with full languages
  populateLanguageDropdown(sourceLanguage, fullLanguages, true);
  populateLanguageDropdown(targetLanguage, fullLanguages, false);

  // Update languages on engine change
  engineSelect.addEventListener('change', () => {
    if (engineSelect.value === 'libre') {
      populateLanguageDropdown(sourceLanguage, libreLanguages, true);
      populateLanguageDropdown(targetLanguage, libreLanguages, false);
    } else {
      populateLanguageDropdown(sourceLanguage, fullLanguages, true);
      populateLanguageDropdown(targetLanguage, fullLanguages, false);
    }
  });

  translateBtn.addEventListener('click', async function () {
    const file = subtitleFile.files[0];
    if (!file) {
      showStatus('Please select a subtitle file', 'error');
      return;
    }

    try {
      translateBtn.disabled = true;
      translateBtn.innerHTML = '<span class="loading"></span> Translating...';
      showStatus('Processing subtitle file...', 'progress');

      const fileContent = await readFileAsText(file);

      const response = await fetch('/api/translate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          fileContent,
          sourceLanguage: sourceLanguage.value,
          targetLanguage: targetLanguage.value,
          engine: engineSelect.value
        })
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'Translation failed');
      }

      const result = await response.json();
      translatedContent = result.translatedContent;

      showStatus('Translation completed successfully!', 'success');
      downloadBtn.style.display = 'block';
    } catch (error) {
      showStatus('Error: ' + error.message, 'error');
    } finally {
      translateBtn.disabled = false;
      translateBtn.textContent = 'Translate Subtitles';
    }
  });

  downloadBtn.addEventListener('click', function () {
    if (!translatedContent) {
      showStatus('No translated content available', 'error');
      return;
    }

    const blob = new Blob([translatedContent], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'translated_subtitles.srt';
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  });

  function showStatus(message, type) {
    statusMessage.textContent = message;
    statusMessage.className = 'status ' + type;
    statusMessage.style.display = 'block';
  }

  function readFileAsText(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result);
      reader.onerror = () => reject(new Error('Failed to read file'));
      reader.readAsText(file);
    });
  }
});

// Enhanced Chatbot Widget Logic
(function () {
  const chatToggle = document.getElementById('chatToggle');
  const chatbotWidget = document.getElementById('chatbotWidget');
  const chatbotHeader = document.getElementById('chatbotHeader');
  const chat = document.getElementById('chat');
  const inputBox = document.getElementById('inputBox');
  const sendBtn = document.getElementById('sendBtn');

  // Initialize with welcome message
  setTimeout(() => {
    addMessageToChat('bot', 'Hello! I\'m Gemii. How can I help you today?');
  }, 1000);

  // Toggle chatbot visibility
  function toggleChatbot() {
    const isVisible = chatbotWidget.style.display === 'flex';
    if (isVisible) {
      chatbotWidget.classList.remove('visible');
      setTimeout(() => {
        chatbotWidget.style.display = 'none';
        chatToggle.style.display = 'block';
      }, 300);
    } else {
      chatbotWidget.style.display = 'flex';
      setTimeout(() => {
        chatbotWidget.classList.add('visible');
        chatToggle.style.display = 'none';
        inputBox.focus();
      }, 10);
    }
  }

  chatToggle.addEventListener('click', toggleChatbot);
  chatbotHeader.addEventListener('click', toggleChatbot);

  // Handle sending messages
  sendBtn.addEventListener('click', sendMessage);
  inputBox.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') sendMessage();
  });

  // Add message to chat with timestamp
  function addMessageToChat(sender, message) {
    const chat = document.getElementById('chat');
    const messageDiv = document.createElement('div');
    messageDiv.classList.add('message', sender);
    
    const now = new Date();
    const timeString = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    
    messageDiv.innerHTML = `
      ${message}
      <div class="message-time">${timeString}</div>
    `;
    
    chat.appendChild(messageDiv);
    chat.scrollTop = chat.scrollHeight;
  }

  // Show typing indicator
  function showTypingIndicator() {
    const chat = document.getElementById('chat');
    const typingDiv = document.createElement('div');
    typingDiv.classList.add('message', 'bot');
    typingDiv.id = 'typingIndicator';
    
    const now = new Date();
    const timeString = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    
    typingDiv.innerHTML = `
      <div class="typing-indicator">
        <div class="typing-dot"></div>
        <div class="typing-dot"></div>
        <div class="typing-dot"></div>
      </div>
      <div class="message-time">${timeString}</div>
    `;
    
    chat.appendChild(typingDiv);
    chat.scrollTop = chat.scrollHeight;
  }

  // Hide typing indicator
  function hideTypingIndicator() {
    const typingIndicator = document.getElementById('typingIndicator');
    if (typingIndicator) {
      typingIndicator.remove();
    }
  }

  // Send message to server and handle response
  async function sendMessage() {
    const userInput = inputBox.value.trim();
    if (!userInput) return;

    addMessageToChat('user', userInput);
    inputBox.value = '';
    showTypingIndicator();

    try {
      const response = await fetch('/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: userInput })
      });
      
      if (!response.ok) throw new Error('Failed to get response');
      const data = await response.json();

      hideTypingIndicator();
      addMessageToChat('bot', data.reply || 'Sorry, I did not understand.');
    } catch (error) {
      hideTypingIndicator();
      addMessageToChat('bot', 'Error: ' + error.message);
    }
  }
})();