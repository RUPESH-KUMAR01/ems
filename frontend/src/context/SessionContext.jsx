import { createContext, useContext, useState, useCallback } from 'react';
import api from '../api';

const SessionContext = createContext(null);

export function SessionProvider({ children }) {
  // Three sessions: admin, user1, user2
  const [sessions, setSessions] = useState({
    admin: { token: null, user: null, loading: false },
    user1: { token: null, user: null, loading: false },
    user2: { token: null, user: null, loading: false },
  });

  const updateSession = useCallback((key, updates) => {
    setSessions(prev => ({
      ...prev,
      [key]: { ...prev[key], ...updates },
    }));
  }, []);

  const login = useCallback(async (sessionKey, email, password) => {
    updateSession(sessionKey, { loading: true });
    try {
      const res = await api.login(email, password);
      const user = await api.me(res.token);
      updateSession(sessionKey, { token: res.token, user, loading: false });
      return { success: true, user };
    } catch (err) {
      updateSession(sessionKey, { loading: false });
      throw err;
    }
  }, [updateSession]);

  const register = useCallback(async (sessionKey, name, email, password, phone) => {
    updateSession(sessionKey, { loading: true });
    try {
      const res = await api.register(name, email, password, phone);
      const user = await api.me(res.token);
      updateSession(sessionKey, { token: res.token, user, loading: false });
      return { success: true, user };
    } catch (err) {
      updateSession(sessionKey, { loading: false });
      throw err;
    }
  }, [updateSession]);

  const logout = useCallback((sessionKey) => {
    updateSession(sessionKey, { token: null, user: null });
  }, [updateSession]);

  const refreshUser = useCallback(async (sessionKey) => {
    const session = sessions[sessionKey];
    if (!session?.token) return;
    try {
      const user = await api.me(session.token);
      updateSession(sessionKey, { user });
    } catch (err) {
      // Token expired or invalid
      updateSession(sessionKey, { token: null, user: null });
    }
  }, [sessions, updateSession]);

  const value = {
    sessions,
    login,
    register,
    logout,
    refreshUser,
    getToken: (key) => sessions[key]?.token,
    getUser: (key) => sessions[key]?.user,
    isLoggedIn: (key) => !!sessions[key]?.token,
  };

  return (
    <SessionContext.Provider value={value}>
      {children}
    </SessionContext.Provider>
  );
}

export function useSession() {
  const ctx = useContext(SessionContext);
  if (!ctx) throw new Error('useSession must be used within SessionProvider');
  return ctx;
}
