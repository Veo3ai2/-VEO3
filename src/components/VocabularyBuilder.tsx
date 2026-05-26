import React, { useState } from "react";
import { VocabWord } from "../types";
import { motion, AnimatePresence } from "motion/react";
import { 
  BookHeart, 
  Trash2, 
  Sparkles, 
  CheckCircle2, 
  Layers, 
  Languages,
  BookOpen,
  ArrowRightLeft
} from "lucide-react";

interface VocabularyBuilderProps {
  words: VocabWord[];
  onRemoveWord: (id: string) => void;
}

export const VocabularyBuilder = ({ words, onRemoveWord }: VocabularyBuilderProps) => {
  const [activeFlippedCard, setActiveFlippedCard] = useState<string | null>(null);
  const [filterLanguage, setFilterLanguage] = useState<string>("All");

  const toggleFlip = (id: string) => {
    setActiveFlippedCard(prev => (prev === id ? null : id));
  };

  // Extract unique languages available
  const availableLanguages = ["All", ...Array.from(new Set(words.map(w => w.languageName)))];

  const filteredWords = filterLanguage === "All" 
    ? words 
    : words.filter(w => w.languageName === filterLanguage);

  return (
    <div className="space-y-8" id="vocabulary-builder-wrapper">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between border-b border-white/10 pb-6 gap-4">
        <div className="flex items-center space-x-4">
          <div className="p-3 bg-brand-lime text-black rounded-none font-display font-black text-xs uppercase tracking-wider">
            VOCAB
          </div>
          <div>
            <span className="text-brand-lime text-xs font-bold uppercase tracking-[0.25em] block">
              Saved Terminology / V.2
            </span>
            <h2 className="text-2xl font-black tracking-tight text-white uppercase font-display mt-0.5">
              Flashcard Lexicon
            </h2>
          </div>
        </div>

        {/* Saved volume badge */}
        <span className="font-mono text-xs uppercase border border-white/25 px-3 py-1.5 bg-white/5 text-brand-lime tracking-widest shrink-0 self-start sm:self-auto">
          {words.length} TERMS CAPTURED
        </span>
      </div>

      {/* Language filter triggers */}
      {availableLanguages.length > 2 && (
        <div className="flex items-center space-x-2 pb-1 overflow-x-auto scrollbar-none">
          {availableLanguages.map((lang) => (
            <button
              key={lang}
              onClick={() => setFilterLanguage(lang)}
              className={`px-3 py-2 text-[10px] font-mono uppercase tracking-widest border transition-all duration-250 outline-none cursor-pointer rounded-none ${
                filterLanguage === lang 
                  ? "bg-brand-lime border-brand-lime text-black font-black" 
                  : "bg-white/5 border-white/10 text-white/70 hover:bg-white/10 hover:border-white/30"
              }`}
            >
              {lang}
            </button>
          ))}
        </div>
      )}

      {/* Grid of flashcards */}
      {filteredWords.length === 0 ? (
        <div className="bg-white/5 border border-white/10 p-12 text-center space-y-5 rounded-none">
          <BookOpen className="w-12 h-12 text-white/20 mx-auto" />
          <div className="space-y-2">
            <h4 className="font-bold text-white uppercase tracking-wider font-mono text-xs">Your personal lexicon is empty</h4>
            <p className="text-xs text-white/50 max-w-sm mx-auto leading-relaxed">
              Start chatting with your partner. Tap/click on any word within the AI response bubbles to automatically translate the word and generate a live flashcard!
            </p>
          </div>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          <AnimatePresence>
            {filteredWords.map((item, index) => {
              const isFlipped = activeFlippedCard === item.id;
              
              return (
                <motion.div
                  key={item.id}
                  initial={{ opacity: 0, scale: 0.95 }}
                  animate={{ opacity: 1, scale: 1 }}
                  exit={{ opacity: 0, scale: 0.9 }}
                  transition={{ duration: 0.2 }}
                  layout
                  className="relative h-48 cursor-pointer perspective-1000 group"
                  onClick={() => toggleFlip(item.id)}
                >
                  <div className={`relative w-full h-full duration-550 transform-style-3d ${isFlipped ? "rotate-y-180" : ""}`}>
                    
                    {/* Front side of the card */}
                    <div className="absolute inset-0 w-full h-full backface-hidden bg-white/5 border border-white/10 rounded-none p-5 flex flex-col justify-between shadow-none hover:border-brand-lime/80 transition-colors">
                      <div>
                        <div className="flex items-center justify-between">
                          <span className="text-[9px] font-mono uppercase tracking-widest bg-brand-lime/10 text-brand-lime border border-brand-lime/20 px-2 py-0.5 rounded-none font-bold">
                            {item.languageName}
                          </span>
                          <button
                            type="button"
                            onClick={(e) => {
                              e.stopPropagation();
                              onRemoveWord(item.id);
                            }}
                            className="p-1.5 text-white/40 hover:text-red-400 hover:bg-white/10 border border-transparent hover:border-white/10 rounded-none transition-all outline-none cursor-pointer"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>

                        <div className="mt-4">
                          <h4 className="text-xl font-black text-white tracking-tight uppercase font-display leading-tight">
                            {item.word}
                          </h4>
                          <p className="text-[10px] text-white/40 font-mono mt-1.5 text-ellipsis overflow-hidden whitespace-nowrap">
                            Context: "{item.contextSentence}"
                          </p>
                        </div>
                      </div>

                      <div className="flex items-center justify-between text-[10px] text-white/40 tracking-wider font-mono border-t pt-3 border-dashed border-white/10 uppercase font-bold">
                        <span className="flex items-center space-x-1.5 group-hover:text-brand-lime transition-colors">
                          <ArrowRightLeft className="w-3.5 h-3.5 text-brand-lime" />
                          <span>Tap to reveal definition</span>
                        </span>
                      </div>
                    </div>

                    {/* Back side of the card (Stark contrasting Yellow focus mode) */}
                    <div className="absolute inset-0 w-full h-full backface-hidden rotate-y-180 bg-brand-lime text-black border border-brand-lime rounded-none p-5 flex flex-col justify-between shadow-md">
                      <div>
                        <div className="flex items-center justify-between">
                          <span className="text-[10px] uppercase font-bold text-black/60 tracking-widest font-mono">
                            ENGLISH DEFINITION
                          </span>
                        </div>

                        <div className="mt-3.5">
                          <p className="text-base font-black text-black leading-snug font-display uppercase tracking-tight">
                            {item.translation}
                          </p>
                          {item.notes && (
                            <p className="text-[11px] text-black/85 leading-relaxed font-sans mt-2.5 bg-black/5 p-2 border border-black/10 rounded-none max-h-16 overflow-y-auto">
                              {item.notes}
                            </p>
                          )}
                        </div>
                      </div>

                      <div className="text-[9px] text-black/65 border-t border-black/10 pt-3 flex items-center justify-between font-bold uppercase tracking-wider font-mono">
                        <span>LEARNED PHRASE</span>
                        <span>TAP TO FLIP BACK</span>
                      </div>
                    </div>

                  </div>
                </motion.div>
              );
            })}
          </AnimatePresence>
        </div>
      )}
    </div>
  );
};
