import { useState } from 'react';
import { useSession } from '../context/SessionContext';
import { useToast } from '../context/ToastContext';
import { Spinner } from './ui';

const SESSION_META = {
  admin: { label: 'Admin Session', icon: '🛡️', gradient: 'from-indigo-500 to-violet-500', desc: 'Log in as admin to manage users, verify accounts, and oversee teams.' },
  user1: { label: 'User Session 1', icon: '👤', gradient: 'from-emerald-500 to-sky-500', desc: 'First user session for testing team workflows.' },
  user2: { label: 'User Session 2', icon: '👥', gradient: 'from-emerald-500 to-sky-500', desc: 'Second user session for multi-user testing.' },
};

export default function LoginPanel() {
  return (
    <div>
      <div className="mb-5">
        <h2 className="text-xl font-bold tracking-tight">🔐 Sessions</h2>
        <p className="text-sm text-slate-400 mt-1">Log in to up to 3 concurrent sessions — 1 Admin + 2 Users</p>
      </div>
      <div className="flex gap-5 flex-wrap">
        {Object.entries(SESSION_META).map(([key, meta]) => (
          <SessionCard key={key} sessionKey={key} meta={meta} />
        ))}
      </div>
    </div>
  );
}

function SessionCard({ sessionKey, meta }) {
  const { sessions, login, register, logout, refreshUser } = useSession();
  const toast = useToast();
  const session = sessions[sessionKey];

  const [mode, setMode] = useState('login');
  const [email, setEmail] = useState(sessionKey === 'admin' ? 'admin@ems.com' : sessionKey === 'user1' ? 'user1@gmail.com' : sessionKey === 'user2' ? 'user2@gmail.com' : '');
  const [password, setPassword] = useState(sessionKey === 'admin' ? 'super-secret' : 'testuser');
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const res = await login(sessionKey, email, password);
      toast.success(`${meta.label} Active`, `Logged in as ${res.user.name} (${res.user.role})`);
    } catch (err) {
      toast.error('Login Failed', err.message);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    try {
      const res = await register(sessionKey, name, email, password, phone);
      toast.success(`${meta.label} Active`, `Registered as ${res.user.name}`);
    } catch (err) {
      toast.error('Registration Failed', err.message);
    }
  };

  const handleLogout = () => {
    logout(sessionKey);
    toast.info(`${meta.label}`, 'Session ended');
  };

  if (session.user) {
    return (
      <div className="flex-1 min-w-[350px] bg-gray-800/50 border border-white/8 rounded-xl overflow-hidden backdrop-blur-sm">
        <div className="p-4 px-5 border-b border-white/8 flex items-center justify-between bg-white/[0.02]">
          <h3 className="text-sm font-semibold flex items-center gap-2">{meta.icon} {meta.label}</h3>
          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[0.7rem] font-semibold bg-emerald-500/15 text-emerald-400">Active</span>
        </div>
        <div className="p-5">
          <div className="flex items-center gap-3 p-3 bg-emerald-500/8 border border-emerald-500/20 rounded-lg mb-4 text-sm">
            <div className={`w-8 h-8 rounded-full bg-gradient-to-br ${meta.gradient} flex items-center justify-center font-semibold text-sm text-white shrink-0`}>
              {session.user.name?.charAt(0)?.toUpperCase()}
            </div>
            <div className="flex-1 min-w-0">
              <div className="font-semibold">{session.user.name}</div>
              <div className="text-[0.7rem] text-slate-500">{session.user.email}</div>
            </div>
            <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[0.7rem] font-semibold uppercase ${
              session.user.role === 'ADMIN' ? 'bg-indigo-500/15 text-indigo-400' :
              session.user.role === 'MODERATOR' ? 'bg-amber-500/15 text-amber-400' :
              'bg-emerald-500/15 text-emerald-400'
            }`}>{session.user.role}</span>
          </div>

          <div className="flex gap-2 text-[0.7rem] mb-3">
            <span className={`inline-flex items-center px-2 py-0.5 rounded-full font-semibold ${session.user.emailVerified ? 'bg-emerald-500/15 text-emerald-400' : 'bg-rose-500/15 text-rose-400'}`}>
              {session.user.emailVerified ? '✓' : '✕'} Email
            </span>
            <span className={`inline-flex items-center px-2 py-0.5 rounded-full font-semibold ${session.user.phoneVerified ? 'bg-emerald-500/15 text-emerald-400' : 'bg-rose-500/15 text-rose-400'}`}>
              {session.user.phoneVerified ? '✓' : '✕'} Phone
            </span>
          </div>

          <div className="flex gap-2">
            <button onClick={() => refreshUser(sessionKey)} className="px-3 py-1 text-xs border border-white/10 text-slate-400 rounded-lg hover:bg-white/5 hover:text-white transition-colors cursor-pointer">↻ Refresh</button>
            <button onClick={handleLogout} className="px-3 py-1 text-xs bg-rose-500 text-white rounded-lg hover:bg-rose-400 transition-colors cursor-pointer">Logout</button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex-1 min-w-[350px] bg-gray-800/50 border border-white/8 rounded-xl overflow-hidden backdrop-blur-sm">
      <div className="p-4 px-5 border-b border-white/8 flex items-center justify-between bg-white/[0.02]">
        <h3 className="text-sm font-semibold flex items-center gap-2">{meta.icon} {meta.label}</h3>
        <div className="flex gap-0.5 p-1 bg-white/5 rounded-lg">
          <button onClick={() => setMode('login')} className={`px-3 py-1 text-xs font-medium rounded-md transition-all cursor-pointer ${mode === 'login' ? 'bg-indigo-500 text-white' : 'text-slate-400 hover:text-white'}`}>Login</button>
          <button onClick={() => setMode('register')} className={`px-3 py-1 text-xs font-medium rounded-md transition-all cursor-pointer ${mode === 'register' ? 'bg-indigo-500 text-white' : 'text-slate-400 hover:text-white'}`}>Register</button>
        </div>
      </div>
      <div className="p-5">
        <p className="text-[0.7rem] text-slate-500 mb-4">{meta.desc}</p>

        {mode === 'login' ? (
          <form onSubmit={handleLogin}>
            <div className="mb-3">
              <label className="block text-xs font-medium text-slate-400 mb-1">Email</label>
              <input className="w-full px-3 py-2 bg-white/5 border border-white/10 rounded-lg text-slate-200 text-sm outline-none focus:border-indigo-500/50 focus:ring-2 focus:ring-indigo-500/20 transition placeholder:text-slate-600" type="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="user@example.com" required />
            </div>
            <div className="mb-4">
              <label className="block text-xs font-medium text-slate-400 mb-1">Password</label>
              <input className="w-full px-3 py-2 bg-white/5 border border-white/10 rounded-lg text-slate-200 text-sm outline-none focus:border-indigo-500/50 focus:ring-2 focus:ring-indigo-500/20 transition placeholder:text-slate-600" type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="••••••" required />
            </div>
            <button type="submit" disabled={session.loading} className="w-full py-2 px-4 bg-gradient-to-r from-indigo-500 to-violet-500 text-white text-sm font-medium rounded-lg hover:brightness-110 hover:shadow-lg hover:shadow-indigo-500/20 active:scale-[0.98] transition-all cursor-pointer disabled:opacity-50 flex items-center justify-center gap-2">
              {session.loading ? <Spinner /> : 'Sign In'}
            </button>
          </form>
        ) : (
          <form onSubmit={handleRegister}>
            <div className="grid grid-cols-2 gap-3">
              <div className="mb-3">
                <label className="block text-xs font-medium text-slate-400 mb-1">Name</label>
                <input className="w-full px-3 py-2 bg-white/5 border border-white/10 rounded-lg text-slate-200 text-sm outline-none focus:border-indigo-500/50 focus:ring-2 focus:ring-indigo-500/20 transition placeholder:text-slate-600" value={name} onChange={e => setName(e.target.value)} placeholder="John Doe" required />
              </div>
              <div className="mb-3">
                <label className="block text-xs font-medium text-slate-400 mb-1">Phone</label>
                <input className="w-full px-3 py-2 bg-white/5 border border-white/10 rounded-lg text-slate-200 text-sm outline-none focus:border-indigo-500/50 focus:ring-2 focus:ring-indigo-500/20 transition placeholder:text-slate-600" value={phone} onChange={e => setPhone(e.target.value)} placeholder="+91..." required />
              </div>
            </div>
            <div className="mb-3">
              <label className="block text-xs font-medium text-slate-400 mb-1">Email</label>
              <input className="w-full px-3 py-2 bg-white/5 border border-white/10 rounded-lg text-slate-200 text-sm outline-none focus:border-indigo-500/50 focus:ring-2 focus:ring-indigo-500/20 transition placeholder:text-slate-600" type="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="user@example.com" required />
            </div>
            <div className="mb-4">
              <label className="block text-xs font-medium text-slate-400 mb-1">Password</label>
              <input className="w-full px-3 py-2 bg-white/5 border border-white/10 rounded-lg text-slate-200 text-sm outline-none focus:border-indigo-500/50 focus:ring-2 focus:ring-indigo-500/20 transition placeholder:text-slate-600" type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="min 6 chars" required />
            </div>
            <button type="submit" disabled={session.loading} className="w-full py-2 px-4 bg-emerald-500 text-white text-sm font-medium rounded-lg hover:bg-emerald-400 hover:shadow-lg hover:shadow-emerald-500/20 active:scale-[0.98] transition-all cursor-pointer disabled:opacity-50 flex items-center justify-center gap-2">
              {session.loading ? <Spinner /> : 'Register & Login'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
