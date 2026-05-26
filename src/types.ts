export interface Partner {
  id: string;
  name: string;
  language: string;
  languageName: string;
  flag: string;
  avatarColor: string;
  voiceName: 'Kore' | 'Zephyr' | 'Puck' | 'Charon' | 'Fenrir';
  role: string;
  description: string;
  welcomeMessage: string;
  suggestedTopics: string[];
}

export interface Feedback {
  correctedText?: string;
  explanation?: string;
  grammarCorrect: boolean;
  alternativeSuggestions?: string[];
  vocabularyNotes?: Array<{ word: string; translation: string; note: string }>;
}

export interface Message {
  id: string;
  role: 'user' | 'model';
  text: string;
  translation?: string;
  feedback?: Feedback; // feedback for user messages, or generated insights
  pronunciationHint?: string; // transliteration (e.g., romaji) if applicable
  audioUrl?: string;
  isPlaying?: boolean;
  suggestedReplies?: string[];
}

export interface VocabWord {
  id: string;
  word: string;
  translation: string;
  languageName: string;
  contextSentence: string;
  notes?: string;
  createdAt: number;
}

export interface UserProgress {
  streak: number;
  totalMessagesSent: number;
  grammarAccuracyScore: number; // percentage of correct inputs
  lastActiveDate?: string;
}
