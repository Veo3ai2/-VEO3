# Language Partner (Android)

A conversational language learning application built with modern **Android Jetpack Compose**, **Kotlin Coroutines & Flow**, **Room Local Database**, and the **Google Gemini API** (`gemini-3.5-flash`).

The app pairs users with culturally authentic native-speaking conversational partners across Spanish, French, Japanese, German, and Italian, featuring real-time conversational feedback, grammar correction, speech-to-text, text-to-speech synthesis, an interactive flashcard lexicon, and comprehensive fluency analytics.

---

## Key Features

- **Culturally Authentic AI Partners**:
  - **Elena** (Spanish, Seville): Local cultural guide, Andalusian gastronomy & flamenco.
  - **Marie** (French, Paris): Parisian gallerist & modern art curator.
  - **Yuki (結衣)** (Japanese, Kyoto): Traditional teahouse and café owner with phonetic transliteration hints.
  - **Lucas** (German, Berlin): Sustainable architecture and green technology specialist.
  - **Giulia** (Italian, Milan): Boutique fashion and textile designer.

- **Real-Time Conversational AI**:
  - Direct integration with Gemini 3.5 Flash using structured JSON generation.
  - Inline grammar correction and syntax suggestions for every spoken sentence.
  - Transliterations (Romaji, phonetic assistance) and expandable English translations.
  - Quick-reply suggestion shortcuts with translation previews.

- **Audio & Speech Recognition**:
  - Native Speech-to-Text input with localized language models.
  - Text-to-Speech audio pronunciation for all partner messages with auto-play toggle.

- **Interactive Flashcard Lexicon (Vocabulary Builder)**:
  - Tap any foreign word during active conversation to immediately save it to your lexicon.
  - 3D-animated flip cards with foreign word on front and English definition/notes on reverse.
  - Filter by language and manually add custom vocabulary terms.
  - Persisted locally with Android Room database.

- **Fluency Metrics & Achievements**:
  - Daily streak tracking across active sessions.
  - Dynamic linguistic accuracy meter with real-time feedback.
  - Gamified achievement badges (First Words, Grammar Master, Polyglot Apprentice, Streak Enthusiast).

---

## Tech Stack & Architecture

- **Platform**: Android (minSdk 26, targetSdk 36, compileSdk 36)
- **Language**: Kotlin 2.1.0 (JVM 21)
- **UI Framework**: Jetpack Compose with Material Design 3 (M3)
- **Persistence**: Android Room (`AppDatabase`, `VocabDao`, `ChatMessageDao`, `ProgressDao`)
- **Networking**: Retrofit 2.11 + OkHttp 4.12 + Kotlinx Serialization
- **AI Integration**: Gemini 3.5 Flash REST API (`generateContent`)
- **Secret Management**: Google Secrets Gradle Plugin reading from `.env`
