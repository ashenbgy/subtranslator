# 🎬 Subtitle Translator & 💬 Chatbot Assistant

A powerful web-based subtitle translator and chatbot built using **Spring Boot**, supporting multiple translation engines like **Google Translate**, **Gemini**, and **LibreTranslate**. This application allows users to upload `.srt` subtitle files, choose the translation engine, and download translated subtitles. A smart AI chatbot is also embedded for instant user assistance.

---

## 🚀 Features

- 🎯 Translate `.srt` files between languages
- 🔍 Auto-detect source language
- 🌐 Choose from multiple translation engines:
  - Google Translate
  - Gemini API
  - LibreTranslate (self-hosted)
- 🧠 Chatbot interface for general queries
- 📦 Web UI with drag-and-drop upload
- 🌙 Clean and modern UI with separate JS and CSS

---

## 🐳 Setting Up LibreTranslate via Docker (from source)

LibreTranslate is a self-hosted, open-source machine translation API.  
You can download, build, and run it locally using Docker.

### 🔽 Step 1: Download LibreTranslate

You can either:

- 📦 [Download ZIP](https://github.com/LibreTranslate/LibreTranslate/archive/refs/heads/main.zip) and extract it  
  or  
- Clone the repository:

```bash
git clone https://github.com/LibreTranslate/LibreTranslate.git
cd LibreTranslate

🐳 Run LibreTranslate Using Docker Compose
sudo docker compose up -d

🧪 Open in Browser and Verify LibreTranslate is Running:
http://localhost:5000

- How to stop LibreTranslate:
  ```bash
  sudo docker compose down
