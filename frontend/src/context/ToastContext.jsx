import React, { createContext, useContext, useState, useCallback } from 'react';

const ToastContext = createContext(null);

let toastId = 0;

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const addToast = useCallback((type, title, message = '') => {
    const id = ++toastId;
    setToasts(prev => [...prev, { id, type, title, message }]);
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id));
    }, 4000);
    return id;
  }, []);

  const removeToast = useCallback((id) => {
    setToasts(prev => prev.filter(t => t.id !== id));
  }, []);

  const toast = React.useMemo(() => ({
    success: (title, msg) => addToast('success', title, msg),
    error: (title, msg) => addToast('error', title, msg),
    info: (title, msg) => addToast('info', title, msg),
    warning: (title, msg) => addToast('warning', title, msg),
  }), [addToast]);

  return (
    <ToastContext.Provider value={toast}>
      {children}
      <div className="fixed top-5 right-5 z-[10000] flex flex-col gap-2">
        {toasts.map(t => (
          <Toast key={t.id} toast={t} onClose={() => removeToast(t.id)} />
        ))}
      </div>
    </ToastContext.Provider>
  );
}

const TOAST_STYLES = {
  success: 'border-l-emerald-500',
  error: 'border-l-rose-500',
  info: 'border-l-sky-500',
  warning: 'border-l-amber-500',
};

const ICONS = { success: '✓', error: '✕', info: 'ℹ', warning: '⚠' };

function Toast({ toast, onClose }) {
  return (
    <div className={`bg-gray-900 border border-white/10 border-l-[3px] ${TOAST_STYLES[toast.type]} rounded-lg p-3 px-4 min-w-[300px] max-w-[420px] flex items-start gap-3 shadow-2xl animate-slide-in text-sm`}>
      <span className="text-base shrink-0 mt-0.5">{ICONS[toast.type]}</span>
      <div className="flex-1 min-w-0">
        <div className="font-semibold text-sm">{toast.title}</div>
        {toast.message && <div className="text-slate-400 mt-0.5 break-words text-xs">{toast.message}</div>}
      </div>
      <button onClick={onClose} className="text-slate-500 hover:text-white text-lg leading-none cursor-pointer">×</button>
    </div>
  );
}

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used within ToastProvider');
  return ctx;
}
