import { useState, useEffect } from 'react';
import { SessionProvider, useSession } from './context/SessionContext';
import { ToastProvider } from './context/ToastContext';
import LoginPanel from './components/LoginPanel';
import AdminPage from './components/AdminPage';
import AdminTeamsPage from './components/AdminTeamsPage';
import UserTeamsPage from './components/UserTeamsPage';

function AppContent() {
  const { sessions } = useSession();
  const [activeTab, setActiveTab] = useState('sessions');
  const [serverOnline, setServerOnline] = useState(false);

  // Check server health — a 401 still means the server is reachable
  useEffect(() => {
    let alive = true;
    const check = async () => {
      try {
        await fetch('/api/auth/me', { method: 'HEAD' });
        if (alive) setServerOnline(true);
      } catch (e) {
        // fetch only throws on network errors (server down), not on 4xx/5xx
        if (alive) setServerOnline(e?.message?.includes('Failed to fetch') ? false : true);
      }
    };
    check();
    const interval = setInterval(check, 30000);
    return () => { alive = false; clearInterval(interval); };
  }, []);

  const tabs = [
    { key: 'sessions', label: '🔐 Sessions', type: 'neutral' },
    { key: 'admin-users', label: '🛡️ Admin Users', type: 'admin', badge: sessions.admin?.token ? null : 'locked' },
    { key: 'admin-teams', label: '🏢 Admin Teams', type: 'admin', badge: sessions.admin?.token ? null : 'locked' },
    { key: 'user1', label: '👤 User 1', type: 'user', badge: sessions.user1?.user?.name },
    { key: 'user2', label: '👥 User 2', type: 'user', badge: sessions.user2?.user?.name },
  ];

  return (
    <div className="relative z-[1] flex flex-col min-h-screen">
      {/* Top Bar */}
      <header className="flex items-center justify-between px-6 py-3 bg-[#0a0e1a]/85 backdrop-blur-xl border-b border-white/8 sticky top-0 z-50">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-indigo-500 to-violet-500 flex items-center justify-center text-lg">⚡</div>
          <h1 className="text-lg font-bold tracking-tight">EMS <span className="text-indigo-400">Testing Dashboard</span></h1>
        </div>
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-2 text-sm text-slate-400">
            <div className={`w-2 h-2 rounded-full ${serverOnline ? 'bg-emerald-400 shadow-[0_0_8px_rgba(16,185,129,0.3)]' : 'bg-rose-400 shadow-[0_0_8px_rgba(244,63,94,0.3)]'} animate-pulse-dot`} />
            {serverOnline ? 'API Online' : 'API Offline'}
          </div>
          <span className="text-xs text-slate-600">localhost:8080</span>
        </div>
      </header>

      {/* Tab Navigation */}
      <nav className="flex gap-1 px-6 py-2 bg-gray-900/50 border-b border-white/8 overflow-x-auto">
        {tabs.map(tab => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`px-4 py-1.5 text-sm font-medium rounded-lg transition-all whitespace-nowrap flex items-center gap-2 cursor-pointer ${
              activeTab === tab.key
                ? tab.type === 'admin'
                  ? 'bg-indigo-500/15 text-white ring-1 ring-inset ring-indigo-500/30'
                  : tab.type === 'user'
                    ? 'bg-emerald-500/15 text-white ring-1 ring-inset ring-emerald-500/30'
                    : 'bg-indigo-500/15 text-white ring-1 ring-inset ring-indigo-500/30'
                : 'text-slate-400 hover:text-white hover:bg-white/5'
            }`}
          >
            {tab.label}
            {tab.badge === 'locked' && <span className="text-[0.6rem] bg-slate-700 text-slate-400 px-1.5 py-0.5 rounded-full">🔒</span>}
            {tab.badge && tab.badge !== 'locked' && <span className="text-[0.6rem] bg-indigo-500 text-white px-1.5 py-0.5 rounded-full font-semibold">{tab.badge}</span>}
          </button>
        ))}
      </nav>

      {/* Main Content — only the active tab mounts */}
      <main className="flex-1 p-6 max-w-[1400px] mx-auto w-full">
        {activeTab === 'sessions' && <LoginPanel />}
        {activeTab === 'admin-users' && <AdminPage />}
        {activeTab === 'admin-teams' && <AdminTeamsPage />}
        {activeTab === 'user1' && <UserTeamsPage sessionKey="user1" />}
        {activeTab === 'user2' && <UserTeamsPage sessionKey="user2" />}
      </main>
    </div>
  );
}

export default function App() {
  return (
    <ToastProvider>
      <SessionProvider>
        <AppContent />
      </SessionProvider>
    </ToastProvider>
  );
}
