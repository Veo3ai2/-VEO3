import React, { useState, useEffect } from "react";
import { Partner, Message, VocabWord, UserProgress } from "./types";
import { PARTNERS, PartnerSelector } from "./components/PartnerSelector";
import { ChatWindow } from "./components/ChatWindow";
import { VocabularyBuilder } from "./components/VocabularyBuilder";
import { ProgressStats } from "./components/ProgressStats";
import { 
  Languages, 
  Flame, 
  Sparkles, 
  Award, 
  ArrowLeft,
  GraduationCap,
  MessageSquare,
  BookMarked,
  BarChart3,
  Globe2
} from "lucide-react";

export default function App() {
  const [selectedPartner, setSelectedPartner] = useState<Partner | null>(null);
  const [activeTab, setActiveTab] = useState<"chat" | "vocab" | "progress">("chat");
  const [userLevel, setUserLevel] = useState<"Beginner" | "Intermediate" | "Advanced">("Beginner");
  
  // Persisted state engines
  const [messages, setMessages] = useState<Message[]>([]);
  const [vocabulary, setVocabulary] = useState<VocabWord[]>([]);
  const [progress, setProgress] = useState<UserProgress>({
    streak: 1,
    totalMessagesSent: 0,
    grammarAccuracyScore: 100,
  });

  const [isGenerating, setIsGenerating] = useState(false);

  // Load state from localStorage on startup
  useEffect(() => {
    const savedVocab = localStorage.getItem("language_partner_vocab");
    const savedProgress = localStorage.getItem("language_partner_progress");
    
    if (savedVocab) setVocabulary(JSON.parse(savedVocab));
    if (savedProgress) {
      const parsedPrg = JSON.parse(savedProgress);
      // Validate streak calendar updates
      const lastActive = parsedPrg.lastActiveDate;
      const todayString = new Date().toISOString().split("T")[0];
      
      let nextStreak = parsedPrg.streak || 1;
      if (lastActive && lastActive !== todayString) {
        const lastActiveDate = new Date(lastActive);
        const todayDate = new Date(todayString);
        const diffTime = Math.abs(todayDate.getTime() - lastActiveDate.getTime());
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        
        if (diffDays === 1) {
          nextStreak += 1; // Increment streak if exactly next day
        } else if (diffDays > 1) {
          nextStreak = 1; // Reset streak if lapse occurred
        }
      }

      setProgress({
        ...parsedPrg,
        streak: nextStreak,
        lastActiveDate: todayString
      });
    } else {
      setProgress(prev => ({
        ...prev,
        lastActiveDate: new Date().toISOString().split("T")[0]
      }));
    }
  }, []);

  // Sync state helpers to localStorage
  const saveProgressState = (updatedPrg: UserProgress) => {
    setProgress(updatedPrg);
    localStorage.setItem("language_partner_progress", JSON.stringify(updatedPrg));
  };

  const handleAddVocabulary = (word: string, translation: string, contextSentence: string, note?: string) => {
    if (!selectedPartner) return;
    const cleanWord = word.trim();
    if (!cleanWord) return;

    const exists = vocabulary.some(v => v.word.toLowerCase() === cleanWord.toLowerCase() && v.languageName === selectedPartner.languageName);
    if (exists) return;

    const newItem: VocabWord = {
      id: `${Date.now()}-${Math.random()}`,
      word: cleanWord,
      translation,
      languageName: selectedPartner.languageName,
      contextSentence,
      notes: note,
      createdAt: Date.now()
    };

    const updated = [newItem, ...vocabulary];
    setVocabulary(updated);
    localStorage.setItem("language_partner_vocab", JSON.stringify(updated));
  };

  const handleRemoveWord = (id: string) => {
    const updated = vocabulary.filter(w => w.id !== id);
    setVocabulary(updated);
    localStorage.setItem("language_partner_vocab", JSON.stringify(updated));
  };

  // When a partner is swapped, load their cached session history if any
  const handlePartnerSelect = (partner: Partner) => {
    setSelectedPartner(partner);
    setActiveTab("chat");
    
    const savedSession = localStorage.getItem(`lang_partner_session_${partner.id}`);
    if (savedSession) {
      setMessages(JSON.parse(savedSession));
    } else {
      setMessages([]);
    }
  };

  const handleSendMessage = async (text: string) => {
    if (!selectedPartner || isGenerating) return;

    const userMsg: Message = {
      id: `${Date.now()}-user`,
      role: "user",
      text,
    };

    const currentTopic = selectedPartner.suggestedTopics[0];
    const updatedMessages = [...messages, userMsg];
    setMessages(updatedMessages);

    setIsGenerating(true);

    try {
      const response = await fetch("/api/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          messages: updatedMessages.map(m => ({ role: m.role, text: m.text })),
          partnerName: selectedPartner.name,
          partnerLanguage: selectedPartner.languageName,
          partnerRole: selectedPartner.role,
          partnerDescription: selectedPartner.description,
          userLevel,
          currentTopic
        })
      });

      if (!response.ok) {
        throw new Error("Partner is currently unavailable. Please inspect your Gemini network credentials.");
      }

      const rawResult = await response.json();
      
      const feedback = rawResult.feedback;
      
      // Update grammar scores metrics:
      // Determine user correctness index and aggregate total counts
      const userMessageLogs = updatedMessages.filter(m => m.role === 'user');
      const correctMessages = userMessageLogs.filter((m, index) => {
        // Evaluate feedback for historical user entries (plus current message)
        if (index === userMessageLogs.length - 1) {
          return feedback?.grammarCorrect;
        }
        return m.feedback?.grammarCorrect !== false; 
      });

      const nextAccuracy = Math.round((correctMessages.length / (userMessageLogs.length)) * 100);

      // Attach feedback to the users last message for inline display inside chat window
      userMsg.feedback = feedback;

      const modelMsg: Message = {
        id: `${Date.now()}-model`,
        role: "model",
        text: rawResult.replyText,
        translation: rawResult.translation,
        pronunciationHint: rawResult.pronunciationHint || undefined,
        suggestedReplies: rawResult.suggestedReplies || [],
      };

      const finalMessages = [...updatedMessages, modelMsg];
      setMessages(finalMessages);

      // Store partner conversation history cache
      localStorage.setItem(`lang_partner_session_${selectedPartner.id}`, JSON.stringify(finalMessages));

      // Build updated metrics
      saveProgressState({
        streak: progress.streak,
        totalMessagesSent: progress.totalMessagesSent + 1,
        grammarAccuracyScore: nextAccuracy,
        lastActiveDate: new Date().toISOString().split("T")[0]
      });

    } catch (e: any) {
      console.error(e);
      // Give fallback model dialog on network errors
      const errorMsg: Message = {
        id: `${Date.now()}-error`,
        role: "model",
        text: `Sorry, I hit a slight connection bump! Please retry. (${e.message || 'Status failure'})`,
        translation: "System translation fallback error.",
      };
      setMessages([...updatedMessages, errorMsg]);
    } finally {
      setIsGenerating(false);
    }
  };

  const handleResetSession = () => {
    if (!selectedPartner) return;
    if (window.confirm("Are you sure you want to clear your current conversation history?")) {
      setMessages([]);
      localStorage.removeItem(`lang_partner_session_${selectedPartner.id}`);
    }
  };

  return (
    <div className="min-h-screen bg-brand-black text-white antialiased font-sans flex flex-col">
      
      {/* Top Navigation / Dashboard banner */}
      <header className="sticky top-0 z-50 bg-[#0A0B0E]/90 backdrop-blur-md border-b border-white/10">
        <div className="max-w-6xl mx-auto px-6 py-5 flex items-center justify-between">
          <div className="flex items-center gap-6">
            <div className="text-xs font-bold uppercase tracking-[0.2em] border border-white/20 px-3.5 py-1.5 font-mono">
              LingoAI / V.2
            </div>
            <div className="h-4 w-px bg-white/20 hidden sm:block"></div>
            <div className="hidden sm:flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-brand-lime animate-pulse"></span>
              <span className="text-[10px] font-mono uppercase tracking-wider text-white/60">Live Conversation Environment</span>
            </div>
          </div>

          <div className="flex items-center gap-8">
            <div className="text-right">
              <div className="text-[9px] text-white/40 uppercase tracking-widest font-bold">PROFICIENCY</div>
              <div className="text-[11px] sm:text-xs font-mono text-brand-lime uppercase tracking-widest font-black mt-0.5">{userLevel}</div>
            </div>
            <div className="text-right">
              <div className="text-[9px] text-white/40 uppercase tracking-widest font-bold">STREAK</div>
              <div className="text-[11px] sm:text-xs font-mono text-brand-lime font-black mt-0.5">{progress.streak} DAYS</div>
            </div>
          </div>
        </div>
      </header>

      {/* Main Workspace container */}
      <main className="max-w-6xl mx-auto px-6 py-12 flex-grow w-full">
        {!selectedPartner ? (
          // Landing View: Partner Catalogue Selection
          <div className="space-y-12 max-w-4xl mx-auto relative">
            <div className="absolute top-0 right-0 text-[260px] leading-none font-black text-white/[0.015] select-none pointer-events-none translate-x-1/4 translate-y-[-10%] uppercase font-display select-none">
              HABLA
            </div>

            <div className="text-center space-y-4 py-6 relative z-10">
              <span className="text-[10px] border border-brand-lime/30 bg-brand-lime/10 text-brand-lime font-mono tracking-[0.25em] uppercase px-3 py-1">
                ACOUSTIC MULTI-LINGUAL AI PRACTICE
              </span>
              <h2 className="text-[44px] sm:text-[76px] font-black leading-[0.9] tracking-tighter text-white uppercase font-display pt-2">
                Speak Out Loud <br/>
                <span className="text-transparent" style={{ WebkitTextStroke: "1px white" }}>Master</span> Languages
              </h2>
              <p className="text-white/50 max-w-xl mx-auto text-xs leading-relaxed font-sans font-medium pt-2">
                Connect with cultural guides, creative planners, and technical architects. Acquire natural dialogue, immediate grammar analysis, and live pronunciation telemetry.
              </p>
            </div>

            <div className="relative z-10">
              <PartnerSelector 
                onSelectPartner={handlePartnerSelect}
                selectedPartnerId={selectedPartner?.id}
              />
            </div>
          </div>
        ) : (
          // Workspace View: Chosen Speak partner interactive tabs
          <div className="space-y-8">
            
            {/* Header controls inside companion panel */}
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-5 bg-white/5 p-5 rounded-none border border-white/10">
              <div className="flex items-center space-x-4">
                <button
                  onClick={() => setSelectedPartner(null)}
                  className="p-3 bg-white/5 hover:bg-brand-lime hover:text-black border border-white/10 text-white rounded-none transition duration-200 cursor-pointer"
                  title="Swap partners or languages"
                >
                  <ArrowLeft className="w-5 h-5" />
                </button>
                <div className="flex items-center space-x-3.5">
                  <span className="text-3xl shrink-0">{selectedPartner.flag}</span>
                  <div>
                    <h3 className="font-black text-white flex items-center text-sm uppercase tracking-wider font-display">
                      Practicing {selectedPartner.languageName}
                    </h3>
                    <p className="text-[10px] text-white/50 font-mono uppercase tracking-widest mt-0.5">
                      Partner: {selectedPartner.name} / {selectedPartner.role}
                    </p>
                  </div>
                </div>
              </div>

              {/* Advanced Strategy Options */}
              <div className="flex items-center space-x-4 shrink-0 self-end sm:self-auto flex-wrap">
                <div className="flex items-center space-x-1 border border-white/10 bg-[#0A0B0E]/40 p-1 rounded-none">
                  {(["Beginner", "Intermediate", "Advanced"] as const).map((level) => (
                    <button
                      key={level}
                      onClick={() => setUserLevel(level)}
                      className={`px-3 py-2 text-[10px] font-mono tracking-widest font-bold uppercase rounded-none transition-all outline-none cursor-pointer ${
                        userLevel === level 
                          ? "bg-brand-lime text-black" 
                          : "text-white/50 hover:text-white"
                      }`}
                    >
                      {level}
                    </button>
                  ))}
                </div>

                <button
                  onClick={handleResetSession}
                  className="text-[10px] font-mono font-bold tracking-widest uppercase border border-red-500/40 text-red-400 bg-red-950/15 hover:bg-red-950/30 px-3.5 py-2.5 transition duration-200 cursor-pointer"
                >
                  Reset Session
                </button>
              </div>
            </div>

            {/* Workplace navigation tabs row */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-8 items-start">
              
              {/* Sidebar directory */}
              <div className="md:col-span-1 bg-white/5 border border-white/10 rounded-none p-5 space-y-3.5">
                <span className="text-[9px] uppercase font-mono font-black text-white/40 tracking-widest block mb-4">Linguistic workspace</span>
                
                <button
                  onClick={() => setActiveTab("chat")}
                  className={`w-full text-left p-4 rounded-none text-xs font-mono uppercase tracking-widest flex items-center space-x-3.5 transition-all outline-none cursor-pointer border ${
                    activeTab === "chat" 
                      ? "bg-brand-lime text-black border-brand-lime font-black shadow-lg shadow-brand-lime/10" 
                      : "text-white/60 border-transparent hover:border-white/10 hover:text-white hover:bg-white/[0.03]"
                  }`}
                >
                  <MessageSquare className="w-4 h-4 shrink-0" />
                  <span>Interactive Chat</span>
                </button>

                <button
                  onClick={() => setActiveTab("vocab")}
                  className={`w-full text-left p-4 rounded-none text-xs font-mono uppercase tracking-widest flex items-center space-x-3.5 transition-all outline-none cursor-pointer border ${
                    activeTab === "vocab" 
                      ? "bg-brand-lime text-black border-brand-lime font-black shadow-lg shadow-brand-lime/10" 
                      : "text-white/60 border-transparent hover:border-white/10 hover:text-white hover:bg-white/[0.03]"
                  }`}
                >
                  <BookMarked className="w-4 h-4 shrink-0" />
                  <span>Lexicon Deck</span>
                </button>

                <button
                  onClick={() => setActiveTab("progress")}
                  className={`w-full text-left p-4 rounded-none text-xs font-mono uppercase tracking-widest flex items-center space-x-3.5 transition-all outline-none cursor-pointer border ${
                    activeTab === "progress" 
                      ? "bg-brand-lime text-black border-brand-lime font-black shadow-lg shadow-brand-lime/10" 
                      : "text-white/60 border-transparent hover:border-white/10 hover:text-white hover:bg-white/[0.03]"
                  }`}
                >
                  <BarChart3 className="w-4 h-4 shrink-0" />
                  <span>Linguistic Stats</span>
                </button>
              </div>

              {/* Core view focus */}
              <div className="md:col-span-3">
                {activeTab === "chat" && (
                  <ChatWindow 
                    partner={selectedPartner}
                    messages={messages}
                    onSendMessage={handleSendMessage}
                    onAddVocab={handleAddVocabulary}
                    isGenerating={isGenerating}
                    vocabWords={vocabulary}
                  />
                )}

                {activeTab === "vocab" && (
                  <VocabularyBuilder 
                    words={vocabulary}
                    onRemoveWord={handleRemoveWord}
                  />
                )}

                {activeTab === "progress" && (
                  <ProgressStats 
                    progress={progress}
                  />
                )}
              </div>

            </div>

          </div>
        )}
      </main>
    </div>
  );
}
