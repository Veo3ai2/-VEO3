import express from "express";
import path from "path";
import dotenv from "dotenv";
import { createServer as createViteServer } from "vite";
import { GoogleGenAI, Modality, Type } from "@google/genai";

dotenv.config();

const app = express();
const PORT = 3000;

// Enable JSON bodies with higher limits for audio/images
app.use(express.json({ limit: "20mb" }));

// Lazy initializer for Gemini Client as recommended
let aiClient: GoogleGenAI | null = null;
function getGeminiClient(): GoogleGenAI {
  if (!aiClient) {
    const key = process.env.GEMINI_API_KEY;
    if (!key) {
      console.warn("WARNING: GEMINI_API_KEY environment variable is not set. All AI operations will fail until set.");
    }
    aiClient = new GoogleGenAI({
      apiKey: key || "PLACEHOLDER", // Fallback to avoid outright crash, prompt will fail cleanly
      httpOptions: {
        headers: {
          "User-Agent": "aistudio-build",
        },
      },
    });
  }
  return aiClient;
}

// 1. CHAT PROXY: Multi-functional structured translation, feedback, and discussion generator
app.post("/api/chat", async (req, res) => {
  try {
    const { 
      messages, 
      partnerName, 
      partnerLanguage, 
      partnerRole, 
      partnerDescription, 
      userLevel = "Beginner",
      currentTopic 
    } = req.body;

    if (!messages || !Array.isArray(messages) || messages.length === 0) {
      return res.status(400).json({ error: "Messages array is required." });
    }

    const ai = getGeminiClient();

    // System instruction directing the AI to act as the specific natural dialogue partner
    // & immediately perform corrective grammar and conversation analysis
    const systemInstruction = `You are ${partnerName}, a conversational partner practicing ${partnerLanguage} with a language learner whose skill level is ${userLevel}.
Your persona profile: ${partnerDescription}
Working role: ${partnerRole}
Current conversation topic of focus: ${currentTopic || 'General discussion'}

CRITICAL GUIDELINES:
1. Stay in character as ${partnerName}. Respond casually and naturally as a native speaker of ${partnerLanguage}. Keep your responses simple and matches the learner's expertise level (${userLevel}).
2. Provide a polite and authentic native language response.
3. Translate your own native response into clear English.
4. For Asian/Cyrillic languages or languages with different scripts, provide a pronunciation hint or transliteration (e.g., Romaji for Japanese, Pinyin for Chinese).
5. Crucially, analyze the user's last message:
   - Check if their message fits typical patterns and grammar rules in ${partnerLanguage}.
   - If they made typos, grammar, or word-choice errors, set "grammarCorrect" to false, give a corrected version in "correctedText", and a friendly explanation in "explanation" in English.
   - If they wrote correctly, set "grammarCorrect" to true.
   - List 2-3 vocabulary words used in this turnaround with brief definition cards in "vocabularyNotes".
6. Formulate 3 relevant suggestion shortcuts in ${partnerLanguage} that the user could click to reply easily (e.g., positive answer, question back, divert topic). Include an English translation for each suggestion inside brackets.

You MUST respond strictly using the JSON format schema provided.`;

    // Construct contents structure from historical inputs
    // The user's input messages should be formatted clearly for the chat structure.
    const lastUserMessage = [...messages].reverse().find(m => m.role === 'user')?.text || '';

    // Convert message list to Gemini chat format
    const contents = messages.map((m: any) => ({
      role: m.role,
      parts: [{ text: m.text }]
    }));

    const response = await ai.models.generateContent({
      model: "gemini-3.5-flash",
      contents: contents,
      config: {
        systemInstruction,
        responseMimeType: "application/json",
        responseSchema: {
          type: Type.OBJECT,
          properties: {
            replyText: {
              type: Type.STRING,
              description: "The AI partner's natural response in the target language."
            },
            translation: {
              type: Type.STRING,
              description: "The English translation of your reply."
            },
            pronunciationHint: {
              type: Type.STRING,
              description: "Transliteration/pronunciation helper (e.g., Romaji or Pinyin). Leave empty if not needed."
            },
            feedback: {
              type: Type.OBJECT,
              properties: {
                grammarCorrect: {
                  type: Type.BOOLEAN,
                  description: "True if the user's last message was grammatically correct and fluent."
                },
                correctedText: {
                  type: Type.STRING,
                  description: "The grammatically correct version of the user's last message. Only supply if grammarCorrect is false."
                },
                explanation: {
                  type: Type.STRING,
                  description: "Helpful, high-vibe strategic tip explaining why the correction was made (in English)."
                },
                vocabularyNotes: {
                  type: Type.ARRAY,
                  items: {
                    type: Type.OBJECT,
                    properties: {
                      word: { type: Type.STRING, description: "Key vocabulary word or phrase used." },
                      translation: { type: Type.STRING, description: "Translation of the word." },
                      note: { type: Type.STRING, description: "Contextual usage or cultural note." }
                    },
                    required: ["word", "translation"]
                  }
                }
              },
              required: ["grammarCorrect"]
            },
            suggestedReplies: {
              type: Type.ARRAY,
              items: {
                type: Type.STRING,
                description: "Short, ready-to-click reply in target language followed by English translation in [brackets]."
              },
              description: "3 clickable sample response templates for the user that move the dialog forward dynamically."
            }
          },
          required: ["replyText", "translation", "feedback", "suggestedReplies"]
        }
      }
    });

    const parsedData = JSON.parse(response.text || "{}");
    res.json(parsedData);
  } catch (error: any) {
    console.error("Gemini Chat endpoint error:", error);
    res.status(500).json({ error: error.message || "Failed to process the chat conversational step" });
  }
});

// 2. SPEECH SYNTHESIS (TTS) PROXY using gemini-3.1-flash-tts-preview
app.post("/api/tts", async (req, res) => {
  try {
    const { text, voiceName = "Zephyr" } = req.body;
    if (!text) {
      return res.status(400).json({ error: "Text is required for speech synthesis." });
    }

    const ai = getGeminiClient();

    const response = await ai.models.generateContent({
      model: "gemini-3.1-flash-tts-preview",
      contents: [{ parts: [{ text: `Say clearly and naturally: ${text}` }] }],
      config: {
        responseModalities: [Modality.AUDIO],
        speechConfig: {
          voiceConfig: {
            prebuiltVoiceConfig: { voiceName: voiceName as any },
          },
        },
      },
    });

    const base64Audio = response.candidates?.[0]?.content?.parts?.[0]?.inlineData?.data;
    if (!base64Audio) {
      return res.status(500).json({ error: "Could not generate speech output from audio model." });
    }

    res.json({ audio: base64Audio });
  } catch (error: any) {
    console.error("Gemini TTS endpoint error:", error);
    res.status(500).json({ error: error.message || "Failed to synthesize speech audio" });
  }
});

// 3. VITE MIDDLEWARE SETUP
async function startServer() {
  if (process.env.NODE_ENV !== "production") {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa",
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(process.cwd(), "dist");
    app.use(express.static(distPath));
    app.get("*", (req, res) => {
      res.sendFile(path.join(distPath, "index.html"));
    });
  }

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`Server successfully executing on http://localhost:${PORT}`);
  });
}

startServer();
