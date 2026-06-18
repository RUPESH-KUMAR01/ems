export function RoleTag({ role }) {
  const styles = {
    ADMIN: 'bg-indigo-500/15 text-indigo-400',
    MODERATOR: 'bg-amber-500/15 text-amber-400',
    USER: 'bg-emerald-500/15 text-emerald-400',
  };
  return (
    <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[0.7rem] font-semibold uppercase tracking-wide ${styles[role] || styles.USER}`}>
      {role}
    </span>
  );
}

export function VerifiedTag({ verified, label }) {
  return (
    <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[0.7rem] font-semibold uppercase tracking-wide ${verified ? 'bg-emerald-500/15 text-emerald-400' : 'bg-rose-500/15 text-rose-400'}`}>
      {verified ? '✓' : '✕'} {label}
    </span>
  );
}

export function StatusTag({ status }) {
  const styles = {
    APPROVED: 'bg-emerald-500/15 text-emerald-400',
    REJECTED: 'bg-rose-500/15 text-rose-400',
    PENDING: 'bg-amber-500/15 text-amber-400',
    INVITATION: 'bg-sky-500/15 text-sky-400',
    JOIN_REQUEST: 'bg-violet-500/15 text-violet-400',
  };
  return (
    <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[0.7rem] font-semibold uppercase tracking-wide ${styles[status] || 'bg-gray-500/15 text-gray-400'}`}>
      {status}
    </span>
  );
}

export function EmptyState({ icon, text }) {
  return (
    <div className="text-center py-10 px-6 text-slate-500">
      <div className="text-4xl mb-3 opacity-50">{icon}</div>
      <div className="text-sm">{text}</div>
    </div>
  );
}

export function Spinner({ className = '' }) {
  return (
    <span className={`inline-block w-4 h-4 border-2 border-white/20 border-t-indigo-400 rounded-full animate-spin ${className}`} />
  );
}

export function Modal({ title, children, footer, onClose }) {
  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-[5000] flex items-center justify-center animate-[fadeIn_0.2s_ease]" onClick={onClose}>
      <div
        className="bg-gray-900 border border-white/10 rounded-2xl w-[90%] max-w-[580px] max-h-[90vh] overflow-y-auto shadow-2xl animate-[scaleIn_0.2s_ease]"
        onClick={e => e.stopPropagation()}
      >
        <div className="p-5 border-b border-white/10 flex items-center justify-between">
          <h3 className="text-lg font-semibold">{title}</h3>
          <button onClick={onClose} className="text-slate-500 hover:text-white hover:bg-white/5 rounded-md px-1 text-xl transition-colors cursor-pointer">×</button>
        </div>
        <div className="p-5">{children}</div>
        {footer && <div className="p-4 px-5 border-t border-white/10 flex justify-end gap-2">{footer}</div>}
      </div>
    </div>
  );
}
