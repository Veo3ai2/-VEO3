import { motion } from "motion/react";
import { Partner } from "../types";
import { Sparkles, ArrowRight, Compass, Languages, Quote } from "lucide-react";

interface PartnerSelectorProps {
  onSelectPartner: (partner: Partner) => void;
  selectedPartnerId?: string;
}

export const PARTNERS: Partner[] = [
  {
    id: "elena",
    name: "Elena",
    language: "es-ES",
    languageName: "Spanish",
    flag: "🇪🇸",
    avatarColor: "bg-amber-100 text-amber-900 border-amber-300",
    voiceName: "Kore",
    role: "Local Cultural Guide in Seville",
    description: "Elena is warm, conversational, and highly enthusiastic about introducing Sevilla's Andalusian history, flamenco, and tapas culture. She uses lively phrasing and speaks clear, direct Spanish.",
    welcomeMessage: "¡Hola! Qué alegría conocerte. Me llamo Elena. ¿Te gustaría hablar sobre gastronomía, viajes o simplemente practicar tu español del día a día?",
    suggestedTopics: ["Flamenco & Art", "Tapas & Spanish Food", "Summer Festivities", "Daily Routine"]
  },
  {
    id: "marie",
    name: "Marie",
    language: "fr-FR",
    languageName: "French",
    flag: "🇫🇷",
    avatarColor: "bg-blue-100 text-blue-900 border-blue-300",
    voiceName: "Puck",
    role: "Event Planner & Gallerist in Paris",
    description: "Marie is elegant, sharp, and highly creative. She speaks natural Parisienne French, loves contemporary art, modern gastronomy, and classical cinema. She is excellent at sharing advanced idioms.",
    welcomeMessage: "Bonjour ! Enchantée de faire votre connaissance. Je m'appelle Marie. De quoi aimeriez-vous parler aujourd'hui ? On peut parler d'art, de projets littéraires ou de votre journée !",
    suggestedTopics: ["Parisian Art Galleries", "French Gastronomy", "Cinema & Fashion", "Travel & Culture"]
  },
  {
    id: "yuki",
    name: "Yuki (結衣)",
    language: "ja-JP",
    languageName: "Japanese",
    flag: "🇯🇵",
    avatarColor: "bg-rose-100 text-rose-900 border-rose-300",
    voiceName: "Kore",
    role: "Traditional Teahouse & Café Owner in Kyoto",
    description: "Yuki is polite, calm, and incredibly understanding. She has a soft spot for green tea aesthetics, classical Japanese literature, and pottery. She provides exceptional phonetic transliterations.",
    welcomeMessage: "こんにちは！お会いできて嬉しいです。京都でカフェを営んでいる結衣（ユキ）と申します。日本の文化や美味しいお茶、あるいは最近の出来事について気軽にお話ししましょう。",
    suggestedTopics: ["Kyoto Temples & Gardens", "Traditional Tea & Sweets", "Daily Life in Japan", "Nature & Seasons"]
  },
  {
    id: "lucas",
    name: "Lucas",
    language: "de-DE",
    languageName: "German",
    flag: "🇩🇪",
    avatarColor: "bg-emerald-100 text-emerald-900 border-emerald-300",
    voiceName: "Fenrir",
    role: "Green Energy Architect in Berlin",
    description: "Lucas is logical, friendly, and structured. He loves urban design, sustainable technologies, cycling in Berlin, and weekend hiking. He has an articulate, clear, and perfectly enunciated speaking style.",
    welcomeMessage: "Hallo! Schön, dich kennenzulernen. Ich bin Lucas. Lass uns ein angenehmes Gespräch auf Deutsch führen. Interessierst du dich für Umwelt, Technik, oder wie läuft dein Tag?",
    suggestedTopics: ["Eco-Architecture", "Berlin Bike Culture", "Professional German Tips", "Outdoor Hobbies"]
  },
  {
    id: "giulia",
    name: "Giulia",
    language: "it-IT",
    languageName: "Italian",
    flag: "🇮🇹",
    avatarColor: "bg-purple-100 text-purple-900 border-purple-300",
    voiceName: "Zephyr",
    role: "Boutique Fashion & Textile Designer in Milan",
    description: "Giulia is passionate, artistic, and speaks with gorgeous rhythmic cadence. She loves Italian cinema, Milanese fashion design, and coastal travel. She brings warmth and energetic colloquial vocabulary.",
    welcomeMessage: "Ciao! Che bello essere qui con te. Mi chiamo Giulia. Parliamo un po' di moda, di viaggi in Italia o delle tue passioni. Il mio scopo è aiutarti a parlare con naturalezza!",
    suggestedTopics: ["Italian Riviera", "Milan Fashion Trends", "Modern Italian Cinema", "Aperitivo Culture"]
  }
];

export const PartnerSelector = ({ onSelectPartner, selectedPartnerId }: PartnerSelectorProps) => {
  return (
    <div className="space-y-8" id="partner-selector-container">
      <div className="flex items-center space-x-4 border-b border-white/10 pb-6">
        <div className="p-3 bg-brand-lime text-black rounded-none font-display font-black text-xs uppercase tracking-wider">
          LIVE
        </div>
        <div>
          <span className="text-brand-lime text-xs font-bold uppercase tracking-[0.25em] block">
            Partner Catalog / V.2
          </span>
          <h2 className="text-2xl font-black tracking-tight text-white uppercase font-display mt-0.5">
            Select Your Conversational Partner
          </h2>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        {PARTNERS.map((partner, index) => {
          const isSelected = selectedPartnerId === partner.id;
          const numValue = String(index + 1).padStart(2, "0");
          return (
            <motion.button
              key={partner.id}
              initial={{ opacity: 0, y: 15 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.08, duration: 0.3 }}
              onClick={() => onSelectPartner(partner)}
              id={`partner-card-${partner.id}`}
              className={`flex flex-col text-left p-6 border transition-all duration-300 outline-none group cursor-pointer relative overflow-hidden rounded-none ${
                isSelected 
                  ? "bg-brand-lime border-brand-lime text-black shadow-lg shadow-brand-lime/10" 
                  : "bg-white/5 border-white/10 text-white hover:border-brand-lime/60 hover:bg-white/[0.08]"
              }`}
            >
              {/* Giant background number for a beautiful editorial feel */}
              <div className={`absolute -bottom-6 -right-2 text-[120px] font-black tracking-tighter select-none pointer-events-none transition-colors duration-350 opacity-[0.03] font-display ${
                isSelected ? "text-black/10 opacity-[0.08]" : "text-white/20"
              }`}>
                {numValue}
              </div>

              <div className="flex items-start justify-between w-full mb-4 relative z-10">
                <div className="flex items-center space-x-4">
                  <div className={`w-14 h-14 flex items-center justify-center rounded-none text-3xl font-bold border shrink-0 ${
                    isSelected 
                      ? "bg-black/10 text-black border-black/20" 
                      : "bg-white/10 text-white border-white/10"
                  }`}>
                    {partner.flag}
                  </div>
                  <div>
                    <div className="flex items-center gap-2 flex-wrap">
                      <h3 className={`font-black text-xl tracking-tight uppercase font-display ${isSelected ? "text-black" : "text-white"}`}>
                        {partner.name}
                      </h3>
                      <span className={`text-[10px] uppercase font-bold tracking-widest px-2 py-0.5 border ${
                        isSelected 
                          ? "bg-black text-brand-lime border-black" 
                          : "bg-brand-lime/10 text-brand-lime border-brand-lime/20"
                      }`}>
                        {partner.languageName}
                      </span>
                    </div>
                    <p className={`text-[11px] font-mono tracking-wider uppercase mt-1 ${isSelected ? "text-black/70" : "text-white/60"}`}>
                      {partner.role}
                    </p>
                  </div>
                </div>

                <div className={`w-8 h-8 flex items-center justify-center border transition-colors ${
                  isSelected 
                    ? "bg-black text-brand-lime border-black" 
                    : "bg-white/5 text-white/40 border-white/10 group-hover:text-brand-lime group-hover:border-brand-lime/40"
                }`}>
                  <span className="text-xs font-mono font-bold">{numValue}</span>
                </div>
              </div>

              <p className={`text-xs leading-relaxed mb-6 flex-grow relative z-10 font-sans ${isSelected ? "text-black/80 font-medium" : "text-white/70"}`}>
                {partner.description}
              </p>

              <div className={`pt-4 border-t w-full flex items-center justify-between transition-colors duration-300 relative z-10 ${
                isSelected ? "border-black/10" : "border-white/10"
              }`}>
                <div className="flex items-center space-x-2 text-[10px] uppercase tracking-wider font-bold">
                  <Compass className={`w-3.5 h-3.5 ${isSelected ? "text-black/70" : "text-brand-lime"}`} />
                  <span className={isSelected ? "text-black/70" : "text-white/50"}>
                    Discussion foci:
                  </span>
                </div>
                <div className="flex flex-wrap gap-1.5 max-w-[70%] justify-end">
                  {partner.suggestedTopics.slice(0, 2).map((topic, i) => (
                    <span 
                      key={i} 
                      className={`text-[9px] font-mono tracking-wider uppercase px-2 py-0.5 border ${
                        isSelected 
                          ? "bg-black/10 text-black border-black/10" 
                          : "bg-white/5 text-white/80 border-white/10"
                      }`}
                    >
                      {topic}
                    </span>
                  ))}
                </div>
              </div>
            </motion.button>
          );
        })}
      </div>
    </div>
  );
};
