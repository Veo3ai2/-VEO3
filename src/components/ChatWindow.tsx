import React, { useState, useRef, useEffect } from "react";
import { motion, AnimatePresence } from "motion/react";
import { Message, Partner, Feedback, VocabWord } from "../types";
import { 
  Send, 
  Volume2, 
  VolumeX, 
  Sparkles, 
  Globe, 
  CheckCircle2, 
  AlertCircle, 
  PlusCircle, 
  CornerDownRight, 
  Loader2,
  Mic,
  MicOff,
  ChevronDown,
  BookOpen,
  Info
} from "lucide-react";

interface ChatWindowProps {
  partner: Partner;
  messages: Message[];
  onSendMessage: (text: string) => Promise<void>;
  onAddVocab: (word: string, translation: string, context: string, note?: string) => void;
  isGenerating: boolean;
  vocabWords: VocabWord[];
}

export const ChatWindow = ({ 
  partner, 
  messages, 
  onSendMessage, 
  onAddVocab,
  isGenerating,
  vocabWords
}: ChatWindowProps) => {
  const [inputText, setInputText] = useState("");
  const [showTranslation, setShowTranslation] = useState<{ [key: string]: boolean }>({});
  const [autoPlayAudio, setAutoPlayAudio] = useState(false);
  const [vocabAddedMap, setVocabAddedMap] = useState<{ [key: string]: boolean }>({});
  const [playingMessageId, setPlayingMessageId] = useState<string | null>(null);
  
  // Real-time Mic recognition state (HTML5 Web Speech API helper)
  const [isRecording, setIsRecording] = useState(false);
  const [micError, setMicError] = useState<string | null>(null);
  const recognitionRef = useRef<any>(null);

  const messagesEndRef = useRef<HTMLDivElement>(null);
  const activeAudioSourceRef = useRef<AudioBufferSourceNode | null>(null);
  const activeAudioCtxRef = useRef<AudioContext | null>(null);

  // Initialize Mic recognition
  useEffect(() => {
    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (SpeechRecognition) {
      const rec = new SpeechRecognition();
      rec.continuous = false;
      rec.interimResults = false;
      rec.lang = partner.language; // Set recognition matching current partner lang!

      rec.onstart = () => {
        setIsRecording(true);
        setMicError(null);
      };

      rec.onresult = (event: any) => {
        const transcript = event.results[0][0].transcript;
        if (transcript) {
          setInputText(prev => prev ? prev + " " + transcript : transcript);
        }
      };

      rec.onerror = (e: any) => {
        console.error("Speech recognition error:", e);
        if (e.error === "not-allowed") {
          setMicError("Microphone permissions denied.");
        } else {
          setMicError(`Recognition issue: ${e.error}`);
        }
        setIsRecording(false);
      };

      rec.onend = () => {
        setIsRecording(false);
      };

      recognitionRef.current = rec;
    }

    return () => {
      stopSpeechSynthesis();
    };
  }, [partner]);

  // Handle language switch updates on active speech config
  useEffect(() => {
    if (recognitionRef.current) {
      recognitionRef.current.lang = partner.language;
    }
  }, [partner.language]);

  // Scroll to bottom on updates
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, isGenerating]);

  // Clean-up active audio playing states
  const stopSpeechSynthesis = () => {
    if (activeAudioSourceRef.current) {
      try {
        activeAudioSourceRef.current.stop();
      } catch (e) {}
      activeAudioSourceRef.current.disconnect();
      activeAudioSourceRef.current = null;
    }
    if (activeAudioCtxRef.current) {
      try {
        activeAudioCtxRef.current.close();
      } catch (e) {}
      activeAudioCtxRef.current = null;
    }
    setPlayingMessageId(null);
  };

  // Speak AI partner text using `/api/tts`
  const playSpeech = async (messageId: string, text: string) => {
    stopSpeechSynthesis();
    setPlayingMessageId(messageId);

    try {
      const response = await fetch("/api/tts", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ 
          text, 
          voiceName: partner.voiceName 
        })
      });

      if (!response.ok) {
        throw new Error("Failed to synthesize TTS audio.");
      }

      const { audio: base64Audio } = await response.json();
      if (!base64Audio) throw new Error("Audio content is empty");

      // Decode the raw base64 string
      const binaryString = window.atob(base64Audio);
      const len = binaryString.length;
      const bytes = new Uint8Array(len);
      for (let i = 0; i < len; i++) {
        bytes[i] = binaryString.charCodeAt(i);
      }

      const AudioContextClass = window.AudioContext || (window as any).webkitAudioContext;
      const audioCtx = new AudioContextClass();
      activeAudioCtxRef.current = audioCtx;

      // Decode buffer source
      const buffer = await audioCtx.decodeAudioData(bytes.buffer);
      const source = audioCtx.createBufferSource();
      source.buffer = buffer;
      source.connect(audioCtx.destination);
      
      source.onended = () => {
        setPlayingMessageId(null);
      };

      activeAudioSourceRef.current = source;
      source.start(0);
    } catch (e) {
      console.error("Audio playback error:", e);
      setPlayingMessageId(null);
    }
  };

  // Monitor autoPlay trigger when new messages land from Model
  useEffect(() => {
    if (messages.length > 0 && autoPlayAudio) {
      const lastMsg = messages[messages.length - 1];
      if (lastMsg.role === "model" && lastMsg.id && lastMsg.text) {
        // Trigger speech play automatically if not already playing
        playSpeech(lastMsg.id, lastMsg.text);
      }
    }
  }, [messages.length, autoPlayAudio]);

  const toggleTranslation = (id: string) => {
    setShowTranslation(prev => ({ ...prev, [id]: !prev[id] }));
  };

  const handleSend = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    const textToSend = inputText.trim();
    if (!textToSend || isGenerating) return;

    setInputText("");
    stopSpeechSynthesis();
    await onSendMessage(textToSend);
  };

  const selectSuggestion = async (suggestion: string) => {
    // Extract actual target words (removes brackets english text if any)
    const rawTarget = suggestion.split("[")[0].trim();
    setInputText("");
    await onSendMessage(rawTarget);
  };

  const handleSpeechInputToggle = () => {
    if (isRecording) {
      recognitionRef.current?.stop();
    } else {
      setMicError(null);
      try {
        recognitionRef.current?.start();
      } catch (e) {
        setMicError("Speech recognition failed to bootstrap.");
      }
    }
  };

  // Highlight or split words inside AI bubbles to let users click easily to build vocab!
  const renderInteractiveText = (text: string, contextSentence: string) => {
    // Clean string from punctuation so we can lookup individual words
    const words = text.split(/(\s+)/); // Keep whitespace separated
    
    return words.map((chunk, idx) => {
      const cleanWord = chunk.replace(/[.,\/#!$%\^&\*;:{}=\-_`~()?"'¡¿]/g, "").trim();
      if (!cleanWord || cleanWord.length < 2) {
        return <span key={idx}>{chunk}</span>;
      }

      const lowerWord = cleanWord.toLowerCase();
      const isAlreadySaved = vocabWords.some(v => v.word.toLowerCase() === lowerWord);
      const isAddedNow = vocabAddedMap[lowerWord];

      return (
        <span 
          key={idx}
          onClick={() => {
            if (isAlreadySaved || isAddedNow) return;
            // Lookup via prompt helper on vocabulary
            triggerVocabularyAdd(cleanWord, contextSentence);
          }}
          className={`cursor-pointer underline decoration-dotted underline-offset-4 transition-all duration-150 ${
            isAlreadySaved 
              ? "text-indigo-600 font-medium decoration-indigo-400 decoration-solid" 
              : isAddedNow 
                ? "text-emerald-600 decoration-emerald-500 font-medium"
                : "hover:text-indigo-600 hover:bg-slate-50 rounded-xs hover:decoration-solid"
          }`}
          title={isAlreadySaved ? "Saved in Vocab Builder!" : "Click to build vocab cards"}
        >
          {chunk}
        </span>
      );
    });
  };

  const triggerVocabularyAdd = async (word: string, contextSentence: string) => {
    const lower = word.toLowerCase();
    setVocabAddedMap(prev => ({ ...prev, [lower]: true }));

    // Generate quick high-quality dictionary helper on the fly for this word inside current language context
    try {
      const response = await fetch("/api/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          messages: [{ role: "user", text: `What is the simple english translation and grammatical function of the word "${word}" in the context of "${contextSentence}"? Respond with short descriptions.` }],
          partnerName: "Grammar Master",
          partnerLanguage: partner.languageName,
          partnerRole: "Dictionary Maker",
          partnerDescription: "Extract exact definition specs",
          userLevel: "Advanced",
          currentTopic: "Dictionary"
        })
      });

      if (response.ok) {
        const result = await response.json();
        const translation = result.translation || "Translation lookup result";
        const note = result.replyText || "Linguistic word breakdown";
        onAddVocab(word, translation, contextSentence, note);
      } else {
        onAddVocab(word, "Saved Translation", contextSentence, "Click to define");
      }
    } catch (e) {
      onAddVocab(word, "Saved Translation", contextSentence, "Vocabulary card");
    }
  };

  const lastMessage = messages[messages.length - 1];
  const suggestions = lastMessage?.role === "model" ? lastMessage.suggestedReplies : undefined;

  return (
    <div className="flex flex-col h-[650px] bg-brand-black border border-white/10 overflow-hidden relative" id="chat-window-wrapper">
      {/* Background Subtle Watermark */}
      <div className="absolute top-24 right-4 text-[130px] font-black leading-none text-white/[0.01] select-none pointer-events-none tracking-tighter uppercase font-display select-none">
        {partner.languageName.slice(0, 5)}
      </div>

      {/* Header bar */}
      <div className="flex items-center justify-between px-6 py-5 bg-white/[0.02] border-b border-white/10 shrink-0 relative z-10">
        <div className="flex items-center space-x-4">
          <div className="w-12 h-12 flex items-center justify-center rounded-none text-2xl font-bold bg-white/10 border border-white/20 shadow-none shrink-0">
            {partner.flag}
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <h3 className="font-black text-white font-display text-lg uppercase tracking-tight">{partner.name}</h3>
              <span className="h-2 w-2 rounded-full bg-brand-lime animate-pulse" />
            </div>
            <p className="text-[10px] text-white/50 font-mono uppercase tracking-wider mt-0.5">{partner.role}</p>
          </div>
        </div>

        <div className="flex items-center space-x-3">
          {/* Autoplay voice synthesizer toggler */}
          <button
            onClick={() => setAutoPlayAudio(!autoPlayAudio)}
            id="autoplay-voice-toggle"
            className={`flex items-center space-x-2 px-3 py-2 rounded-none border text-xs font-mono uppercase tracking-widest transition-all duration-200 outline-none cursor-pointer ${
              autoPlayAudio 
                ? "bg-brand-lime border-brand-lime text-black font-black" 
                : "bg-white/5 border-white/10 text-white/70 hover:bg-white/10 hover:text-white"
            }`}
            title="Auto-play AI replies instantly via speech synthesis"
          >
            <Volume2 className={`w-3.5 h-3.5 ${autoPlayAudio ? "text-black animate-bounce" : "text-white/40"}`} />
            <span>AUTO-SPEAK</span>
          </button>
        </div>
      </div>

      {/* Messages zone */}
      <div className="flex-grow p-6 overflow-y-auto space-y-6 bg-transparent relative z-10">
        <div className="max-w-2xl mx-auto space-y-6">
          
          {/* Default partner greetings card */}
          <div className="flex space-x-4 items-start">
            <div className="w-9 h-9 flex items-center justify-center rounded-none text-lg font-bold bg-white/10 border border-white/20 shrink-0">
              {partner.flag}
            </div>
            <div className="bg-white/5 border border-white/10 p-5 rounded-none max-w-[85%]">
              <p className="text-sm text-white/90 leading-relaxed font-sans">{partner.welcomeMessage}</p>
              <div className="mt-4 flex items-center space-x-2 text-[10px] text-brand-lime font-mono uppercase tracking-widest border-t pt-3 border-dashed border-white/10">
                <BookOpen className="w-3.5 h-3.5" />
                <span>Word Tap Practice Enabled — Tap words to capture vocabulary cards</span>
              </div>
            </div>
          </div>

          <AnimatePresence initial={false}>
            {messages.map((msg, i) => {
              const isAI = msg.role === "model";
              const hasFeedback = msg.feedback;
              const hasCorrection = msg.feedback && !msg.feedback.grammarCorrect;

              return (
                <motion.div
                  key={msg.id}
                  initial={{ opacity: 0, y: 15 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.28 }}
                  className="space-y-3"
                >
                  <div className={`flex space-x-4 items-start ${isAI ? "" : "flex-row-reverse space-x-reverse"}`}>
                    
                    {/* Character Avatar bubble */}
                    <div className={`w-9 h-9 flex items-center justify-center rounded-none text-lg font-bold border shrink-0 ${
                      isAI 
                        ? "bg-white/10 text-white border-white/10" 
                        : "bg-brand-lime text-black border-brand-lime font-black"
                    }`}>
                      {isAI ? partner.flag : "💬"}
                    </div>

                    {/* Chat dialog body */}
                    <div className="max-w-[80%] space-y-2">
                      <div className={`p-4 border rounded-none ${
                        isAI 
                          ? "bg-white/5 border-white/10 text-white/90" 
                          : "bg-brand-lime border-brand-lime text-black font-semibold shadow-md shadow-brand-lime/5"
                      }`}>
                        
                        {/* Word-Interactive Text for AI, Standard text for client */}
                        <p className={`text-sm leading-relaxed font-sans ${!isAI ? "font-bold text-black" : "text-white"}`}>
                          {isAI 
                            ? renderInteractiveText(msg.text, msg.text)
                            : msg.text
                          }
                        </p>

                        {/* Pronunciation helpful hints (Romaji/Pinyin/etc) */}
                        {isAI && msg.pronunciationHint && (
                          <p className="text-[10px] text-brand-lime font-mono tracking-wider uppercase bg-brand-lime/10 px-2 py-1 mt-2.5 inline-block">
                            Pronunciation: {msg.pronunciationHint}
                          </p>
                        )}

                        {/* Translation expandable overlay */}
                        {isAI && msg.translation && (
                          <div className="mt-3 pt-3 border-t border-white/10">
                            {showTranslation[msg.id] ? (
                              <p className="text-xs text-white/60 italic bg-white/[0.02] p-2.5 border border-white/5">
                                "{msg.translation}"
                              </p>
                            ) : (
                              <button 
                                onClick={() => toggleTranslation(msg.id)}
                                className="text-[10px] text-brand-lime hover:text-white font-mono uppercase tracking-widest flex items-center space-x-1.5 mt-0.5 cursor-pointer"
                              >
                                <Globe className="w-3 h-3" />
                                <span>See translation</span>
                              </button>
                            )}
                          </div>
                        )}
                        
                        {/* Audio text-to-speech button */}
                        {isAI && (
                          <div className="flex justify-end mt-2 border-t border-white/5 pt-2">
                            <button
                              onClick={() => playSpeech(msg.id, msg.text)}
                              className={`p-1.5 border hover:bg-white/15 transition-all outline-none cursor-pointer ${
                                playingMessageId === msg.id 
                                  ? "bg-brand-lime text-black border-brand-lime animate-pulse" 
                                  : "text-white/50 border-white/10"
                              }`}
                              title="Listen to native pronunciation"
                            >
                              <Volume2 className="w-3.5 h-3.5" />
                            </button>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Grammar Analysis feedback widget */}
                  {!isAI && hasFeedback && (
                    <motion.div
                      initial={{ opacity: 0, scale: 0.95 }}
                      animate={{ opacity: 1, scale: 1 }}
                      className="max-w-[75%] mr-12 ml-auto"
                    >
                      {hasCorrection ? (
                        <div className="bg-amber-950/20 border border-amber-500/30 p-4 rounded-none text-xs space-y-2">
                          <div className="flex items-center space-x-2 text-brand-lime font-bold uppercase tracking-wider font-mono">
                            <AlertCircle className="w-4 h-4 text-brand-lime shrink-0" />
                            <span>Grammar Corrective feedback</span>
                          </div>
                          <div className="space-y-1 pl-1">
                            <p className="text-white/40">Spoken: <span className="line-through text-amber-300">{msg.text}</span></p>
                            <p className="text-brand-lime">Recommended: <span className="font-bold underline">{msg.feedback?.correctedText}</span></p>
                            <p className="text-white/70 italic leading-relaxed pt-2 border-t border-white/5 mt-2 font-sans">
                              {msg.feedback?.explanation}
                            </p>
                          </div>
                        </div>
                      ) : (
                        <div className="bg-brand-lime/10 border border-brand-lime/20 p-3 rounded-none flex items-center space-x-2.5 text-xs text-brand-lime font-mono uppercase tracking-wider">
                          <CheckCircle2 className="w-4 h-4 text-brand-lime shrink-0" />
                          <span>Pristine syntax! Excellent grammatical fluency.</span>
                        </div>
                      )}
                    </motion.div>
                  )}
                </motion.div>
              );
            })}
          </AnimatePresence>

          {/* AI Partner thinking visualizer */}
          {isGenerating && (
            <motion.div 
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              className="flex space-x-4 items-start"
            >
              <div className="w-9 h-9 flex items-center justify-center rounded-none text-lg font-bold bg-white/15 border border-white/10 shrink-0">
                🤖
              </div>
              <div className="bg-white/5 border border-white/10 p-4 rounded-none shadow-none flex items-center space-x-3">
                <Loader2 className="w-4 h-4 text-brand-lime animate-spin" />
                <span className="text-xs text-white/60 font-mono uppercase tracking-wider">{partner.name} is formulating a reply...</span>
              </div>
            </motion.div>
          )}

          <div ref={messagesEndRef} />
        </div>
      </div>

      {/* Suggested replies selector panel */}
      {suggestions && suggestions.length > 0 && !isGenerating && (
        <div className="px-6 py-3 bg-white/5 border-t border-white/10 shrink-0 overflow-x-auto relative z-10">
          <div className="max-w-2xl mx-auto flex items-center space-x-3 py-1">
            <Sparkles className="w-4 h-4 text-brand-lime shrink-0 animate-pulse" />
            <span className="text-[10px] uppercase font-mono font-bold text-white/40 tracking-widest shrink-0">QUICK OPTIONS:</span>
            <div className="flex space-x-2 overflow-x-auto scrollbar-none pb-0.5">
              {suggestions.map((suggestion, idx) => (
                <button
                  key={idx}
                  onClick={() => selectSuggestion(suggestion)}
                  className="text-[11px] font-mono uppercase tracking-wider bg-transparent hover:bg-brand-lime border border-white/15 hover:border-brand-lime text-white hover:text-black px-4 py-1.5 transition-all duration-200 shrink-0 outline-none cursor-pointer rounded-none font-bold"
                >
                  {suggestion}
                </button>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Controller entry section */}
      <div className="px-6 py-5 border-t border-white/10 shrink-0 bg-white/[0.01] relative z-10">
        <div className="max-w-2xl mx-auto">
          <form onSubmit={handleSend} className="flex items-center space-x-3">
            {/* Dictation button (Speech To Text) */}
            <button
              type="button"
              onClick={handleSpeechInputToggle}
              className={`p-3.5 border transition-all duration-300 outline-none cursor-pointer rounded-none relative ${
                isRecording 
                  ? "bg-red-600 border-red-600 text-white scale-105" 
                  : "bg-white/5 border-white/10 text-white/70 hover:text-brand-lime hover:border-brand-lime"
              }`}
              title={isRecording ? "Stop dictating" : "Speak to write using voice dictation"}
            >
              {isRecording ? (
                <>
                  <Mic className="w-5 h-5 animate-pulse" />
                  <span className="absolute -top-1 -right-1 flex h-2.5 w-2.5">
                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"></span>
                    <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-red-500"></span>
                  </span>
                </>
              ) : (
                <Mic className="w-5 h-5" />
              )}
            </button>

            {/* Main Text input */}
            <input
              type="text"
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              placeholder={isRecording ? "Listening carefully..." : `Reply to ${partner.name} in ${partner.languageName}...`}
              disabled={isGenerating}
              className="flex-grow bg-white/5 border border-white/10 rounded-none px-4 py-3.5 text-sm focus:outline-none focus:bg-white/10 focus:border-brand-lime focus:ring-1 focus:ring-brand-lime transition-all text-white font-sans"
            />

            {/* Send trigger */}
            <button
              type="submit"
              disabled={!inputText.trim() || isGenerating}
              className={`p-3.5 border transition-all duration-200 flex items-center justify-center cursor-pointer rounded-none ${
                inputText.trim() && !isGenerating
                  ? "bg-brand-lime border-brand-lime text-black hover:bg-white"
                  : "bg-white/5 border-white/10 text-white/30 pointer-events-none"
              }`}
            >
              <Send className="w-5 h-5" />
            </button>
          </form>

          {/* Mic validation warnings */}
          {micError && (
            <div className="mt-2.5 text-[10px] text-red-400 font-mono uppercase tracking-widest flex items-center space-x-1.5 pl-1">
              <Info className="w-3.5 h-3.5 shrink-0" />
              <span>{micError}</span>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
