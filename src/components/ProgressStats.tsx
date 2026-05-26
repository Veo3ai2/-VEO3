import React from "react";
import { UserProgress } from "../types";
import { 
  Flame, 
  Target, 
  Compass, 
  Award, 
  CheckCircle, 
  TrendingUp,
  MessageSquare,
  HelpCircle
} from "lucide-react";

interface ProgressStatsProps {
  progress: UserProgress;
}

export const ProgressStats = ({ progress }: ProgressStatsProps) => {
  // Badges lists based on user performance values
  const achievements = [
    {
      title: "First Words",
      description: "Sent your first practice message to an AI partner.",
      unlocked: progress.totalMessagesSent >= 1,
      icon: "🌱"
    },
    {
      title: "Grammar Master",
      description: "Keep grammar score above 85% with 5+ messages sent.",
      unlocked: progress.grammarAccuracyScore >= 85 && progress.totalMessagesSent >= 5,
      icon: "📝"
    },
    {
      title: "Polyglot Apprentice",
      description: "Engage in more than 10 total chat dialogues.",
      unlocked: progress.totalMessagesSent >= 10,
      icon: "🗣️"
    },
    {
      title: "Daily Streak Enthusiast",
      description: "Maintain a study streak of at least 3 active sessions.",
      unlocked: progress.streak >= 3,
      icon: "🔥"
    }
  ];

  return (
    <div className="space-y-8" id="progress-stats-wrapper">
      {/* Header section */}
      <div className="flex items-center space-x-4 border-b border-white/10 pb-6">
        <div className="p-3 bg-brand-lime text-black rounded-none font-display font-black text-xs uppercase tracking-wider">
          STATS
        </div>
        <div>
          <span className="text-brand-lime text-xs font-bold uppercase tracking-[0.25em] block">
            Linguistic Analytics / V.2
          </span>
          <h2 className="text-2xl font-black tracking-tight text-white uppercase font-display mt-0.5">
            Your Fluency Metrics
          </h2>
        </div>
      </div>

      {/* Metrics Row Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
        {/* Streak card */}
        <div className="bg-white/5 border border-white/10 rounded-none p-6 flex flex-col justify-between relative overflow-hidden group">
          <div className="absolute -right-4 -bottom-4 text-[70px] font-black text-white/[0.012] select-none pointer-events-none font-display">
            DAIL
          </div>
          <div className="flex items-center justify-between">
            <span className="text-[10px] text-white/50 font-mono uppercase tracking-widest font-bold">Daily Streak</span>
            <Flame className="w-5 h-5 text-brand-lime animate-pulse" />
          </div>
          <div className="mt-6">
            <span className="text-4xl font-black text-white font-display tracking-tight uppercase">
              {progress.streak} {progress.streak === 1 ? 'DAY' : 'DAYS'}
            </span>
          </div>
        </div>

        {/* Sent messages card */}
        <div className="bg-white/5 border border-white/10 rounded-none p-6 flex flex-col justify-between relative overflow-hidden group">
          <div className="absolute -right-4 -bottom-4 text-[70px] font-black text-white/[0.012] select-none pointer-events-none font-display">
            TALK
          </div>
          <div className="flex items-center justify-between">
            <span className="text-[10px] text-white/50 font-mono uppercase tracking-widest font-bold">Messages Spoken</span>
            <MessageSquare className="w-5 h-5 text-brand-lime" />
          </div>
          <div className="mt-6">
            <span className="text-4xl font-black text-white font-display tracking-tight uppercase">
              {progress.totalMessagesSent}
            </span>
          </div>
        </div>

        {/* Accuracy percentage card */}
        <div className="bg-white/5 border border-white/10 rounded-none p-6 flex flex-col justify-between relative overflow-hidden group">
          <div className="absolute -right-4 -bottom-4 text-[70px] font-black text-white/[0.012] select-none pointer-events-none font-display">
            ACCU
          </div>
          <div className="flex items-center justify-between">
            <span className="text-[10px] text-white/50 font-mono uppercase tracking-widest font-bold">Grammar Accuracy</span>
            <Target className="w-5 h-5 text-brand-lime" />
          </div>
          <div className="mt-6">
            <span className="text-4xl font-black text-brand-lime font-display tracking-tight uppercase">
              {progress.totalMessagesSent === 0 ? "100%" : `${progress.grammarAccuracyScore}%`}
            </span>
          </div>
        </div>
      </div>

      {/* Accuracy analysis gauge */}
      <div className="bg-white/5 border border-white/10 rounded-none p-6 relative overflow-hidden">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-xs font-mono font-bold text-white uppercase tracking-widest flex items-center space-x-2">
            <span>Accuracy Meter Analysis</span>
          </h3>
          <span className="text-[9px] border border-brand-lime text-brand-lime bg-brand-lime/10 px-2 py-0.5 rounded-none font-bold font-mono">
            LIVE ANALYST
          </span>
        </div>
        <div className="w-full bg-white/10 h-3 rounded-none overflow-hidden border border-white/5">
          <div 
            className="bg-brand-lime h-full transition-all duration-500 rounded-none"
            style={{ width: `${progress.totalMessagesSent === 0 ? 100 : progress.grammarAccuracyScore}%` }}
          />
        </div>
        <p className="text-xs text-white/60 leading-relaxed mt-4 font-sans max-w-xl">
          {progress.totalMessagesSent === 0 
            ? "Your grammar accuracy score is calibrated in real-time. Practice dialogues to activate telemetry updates."
            : progress.grammarAccuracyScore >= 85
              ? "Exquisite performance. Your sentence compositions show extreme grasp over noun genders, local articles, and tense agreements."
              : "Review correcting suggestions embedded inside the conversation window. Continuous practice reduces grammatical error margins."
          }
        </p>
      </div>

      {/* Gamified Achievements Deck */}
      <div className="space-y-4">
        <h3 className="text-xs font-mono font-bold text-white tracking-widest uppercase flex items-center space-x-2 pb-2 border-b border-white/10">
          <Award className="w-4 h-4 text-brand-lime" />
          <span>Fluency badges achieved</span>
        </h3>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          {achievements.map((badge, idx) => (
            <div 
              key={idx}
              className={`p-5 rounded-none border transition-all duration-200 flex items-start space-x-4 ${
                badge.unlocked 
                  ? "bg-white/5 border-white/15 shadow-none" 
                  : "bg-white/[0.012] border-white/5 opacity-40"
              }`}
            >
              <div className={`text-2xl shrink-0 w-11 h-11 flex items-center justify-center border ${
                badge.unlocked ? "bg-white/10 border-white/20" : "bg-white/5 border-transparent"
              }`}>
                {badge.icon}
              </div>
              <div className="space-y-1">
                <div className="flex items-center space-x-2">
                  <h4 className={`text-sm font-black tracking-tight uppercase font-display ${badge.unlocked ? "text-white" : "text-white/40"}`}>
                    {badge.title}
                  </h4>
                  {badge.unlocked && <span className="text-[8px] border border-brand-lime text-brand-lime px-1.5 py-0.5 rounded-none font-mono font-bold tracking-wider">UNLOCKED</span>}
                </div>
                <p className="text-xs text-white/50 leading-relaxed font-sans">
                  {badge.description}
                </p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
