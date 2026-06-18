import { useState, useEffect, useCallback } from 'react';
import { useSession } from '../context/SessionContext';
import { useToast } from '../context/ToastContext';
import { EmptyState, Modal, Spinner } from './ui';
import api from '../api';

export default function AdminPage() {
  const { sessions } = useSession();
  const adminToken = sessions.admin?.token;

  if (!adminToken) {
    return <EmptyState icon="🛡️" text="Log in as Admin in the Sessions tab to access this panel." />;
  }

  return (
    <div>
      <div className="mb-5">
        <h2 className="text-xl font-bold tracking-tight">🛡️ Admin Panel</h2>
        <p className="text-sm text-slate-400 mt-1">Manage all users — create, edit, verify, change roles, and delete</p>
      </div>
      <UserManagement token={adminToken} />
    </div>
  );
}

function UserManagement({ token }) {
  const toast = useToast();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [editUser, setEditUser] = useState(null);
  const [createOpen, setCreateOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [search, setSearch] = useState('');
  const [searchType, setSearchType] = useState('email');

  const loadUsers = useCallback(async () => {
    setLoading(true);
    try {
      const data = await api.adminGetAllUsers(token);
      setUsers(data);
    } catch (err) {
      toast.error('Failed to load users', err.message);
    } finally {
      setLoading(false);
    }
  }, [token, toast]);

  useEffect(() => { loadUsers(); }, [loadUsers]);

  const handleSearch = async () => {
    if (!search.trim()) return loadUsers();
    try {
      const fn = searchType === 'email' ? api.adminSearchByEmail : api.adminSearchByPhone;
      const user = await fn(search, token);
      setUsers([user]);
      toast.info('Search Result', `Found user: ${user.name}`);
    } catch (err) {
      toast.error('Search Failed', err.message);
    }
  };

  const handleDelete = async () => {
    try {
      await api.adminDeleteUser(deleteTarget.id, token);
      toast.success('User Deleted', `${deleteTarget.name} has been removed`);
      setDeleteTarget(null);
      loadUsers();
    } catch (err) {
      toast.error('Delete Failed', err.message);
    }
  };

  const handleToggleVerification = async (user, field) => {
    const data = field === 'email'
      ? { emailVerified: !user.emailVerified }
      : { phoneVerified: !user.phoneVerified };
    try {
      await api.adminUpdateUser(user.id, data, token);
      toast.success('Updated', `${field} verification toggled for ${user.name}`);
      loadUsers();
    } catch (err) {
      toast.error('Update Failed', err.message);
    }
  };

  const handleRoleChange = async (user, newRole) => {
    try {
      await api.adminChangeRole(user.id, newRole, token);
      toast.success('Role Changed', `${user.name} → ${newRole}`);
      loadUsers();
    } catch (err) {
      toast.error('Role Change Failed', err.message);
    }
  };

  return (
    <div>
      {/* Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-5">
        <StatCard icon="👥" iconBg="bg-indigo-500/15 text-indigo-400" value={users.length} label="Total Users" />
        <StatCard icon="✓" iconBg="bg-emerald-500/15 text-emerald-400" value={users.filter(u => u.emailVerified).length} label="Email Verified" />
        <StatCard icon="📱" iconBg="bg-amber-500/15 text-amber-400" value={users.filter(u => u.phoneVerified).length} label="Phone Verified" />
        <StatCard icon="🛡️" iconBg="bg-rose-500/15 text-rose-400" value={users.filter(u => u.role === 'ADMIN').length} label="Admins" />
      </div>

      {/* Toolbar */}
      <div className="bg-gray-800/50 border border-white/8 rounded-xl p-4 mb-5 backdrop-blur-sm">
        <div className="flex items-center gap-3 flex-wrap">
          <div className="flex gap-2 items-end flex-1 min-w-[280px]">
            <select value={searchType} onChange={e => setSearchType(e.target.value)} className="px-3 py-2 bg-white/5 border border-white/10 rounded-lg text-slate-200 text-sm outline-none cursor-pointer max-w-[120px]">
              <option value="email">Email</option>
              <option value="phone">Phone</option>
            </select>
            <input
              className="flex-1 px-3 py-2 bg-white/5 border border-white/10 rounded-lg text-slate-200 text-sm outline-none focus:border-indigo-500/50 focus:ring-2 focus:ring-indigo-500/20 transition placeholder:text-slate-600"
              placeholder={`Search by ${searchType}...`}
              value={search}
              onChange={e => setSearch(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleSearch()}
            />
            <button onClick={handleSearch} className="px-3 py-2 border border-white/10 text-slate-400 rounded-lg hover:bg-white/5 hover:text-white transition-colors cursor-pointer">🔍</button>
          </div>
          <div className="flex items-center gap-2">
            <button onClick={loadUsers} className="px-3 py-2 text-xs border border-white/10 text-slate-400 rounded-lg hover:bg-white/5 hover:text-white transition-colors cursor-pointer">↻ Refresh</button>
            <button onClick={() => setCreateOpen(true)} className="px-3 py-2 text-xs bg-gradient-to-r from-indigo-500 to-violet-500 text-white rounded-lg hover:brightness-110 hover:shadow-lg hover:shadow-indigo-500/20 transition-all cursor-pointer">+ Create User</button>
          </div>
        </div>
      </div>

      {/* Users Table */}
      <div className="bg-gray-800/50 border border-white/8 rounded-xl overflow-hidden backdrop-blur-sm">
        <div className="p-4 px-5 border-b border-white/8 flex items-center justify-between">
          <span className="text-sm font-semibold flex items-center gap-2">👥 All Users</span>
          {loading && <Spinner />}
        </div>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="text-left">
                <Th>ID</Th>
                <Th>Name</Th>
                <Th>Email</Th>
                <Th>Role</Th>
                <Th>Email ✓</Th>
                <Th>Phone ✓</Th>
                <Th>Actions</Th>
              </tr>
            </thead>
            <tbody>
              {users.length === 0 && !loading ? (
                <tr><td colSpan="7"><EmptyState icon="📭" text="No users found" /></td></tr>
              ) : (
                users.map(user => (
                  <tr key={user.id} className="hover:bg-white/[0.03] transition-colors">
                    <Td className="font-mono text-slate-500">#{user.id}</Td>
                    <Td className="font-medium">{user.name}</Td>
                    <Td className="text-slate-400">{user.email}</Td>
                    <Td>
                      <select
                        value={user.role}
                        onChange={e => handleRoleChange(user, e.target.value)}
                        className="px-2 py-1 bg-white/5 border border-white/10 rounded-md text-slate-200 text-xs outline-none cursor-pointer"
                      >
                        <option value="USER">USER</option>
                        <option value="MODERATOR">MODERATOR</option>
                        <option value="ADMIN">ADMIN</option>
                      </select>
                    </Td>
                    <Td>
                      <button
                        onClick={() => handleToggleVerification(user, 'email')}
                        className={`px-2 py-1 text-xs rounded-md transition-colors cursor-pointer ${user.emailVerified ? 'bg-emerald-500 text-white hover:bg-emerald-400' : 'border border-white/10 text-slate-400 hover:bg-white/5'}`}
                      >
                        {user.emailVerified ? '✓ Verified' : '✕ Unverified'}
                      </button>
                    </Td>
                    <Td>
                      <button
                        onClick={() => handleToggleVerification(user, 'phone')}
                        className={`px-2 py-1 text-xs rounded-md transition-colors cursor-pointer ${user.phoneVerified ? 'bg-emerald-500 text-white hover:bg-emerald-400' : 'border border-white/10 text-slate-400 hover:bg-white/5'}`}
                      >
                        {user.phoneVerified ? '✓ Verified' : '✕ Unverified'}
                      </button>
                    </Td>
                    <Td>
                      <div className="flex items-center gap-2">
                        <button onClick={() => setEditUser(user)} className="px-2 py-1 text-xs border border-white/10 text-slate-400 rounded-md hover:bg-white/5 hover:text-white transition-colors cursor-pointer">✏️ Edit</button>
                        <button onClick={() => setDeleteTarget(user)} className="px-2 py-1 text-xs bg-rose-500 text-white rounded-md hover:bg-rose-400 transition-colors cursor-pointer">🗑️</button>
                      </div>
                    </Td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {createOpen && <CreateUserModal token={token} onClose={() => setCreateOpen(false)} onCreated={() => { setCreateOpen(false); loadUsers(); }} />}
      {editUser && <EditUserModal token={token} user={editUser} onClose={() => setEditUser(null)} onSaved={() => { setEditUser(null); loadUsers(); }} />}
      {deleteTarget && (
        <Modal title="Delete User" onClose={() => setDeleteTarget(null)} footer={
          <>
            <button onClick={() => setDeleteTarget(null)} className="px-4 py-2 text-sm border border-white/10 text-slate-400 rounded-lg hover:bg-white/5 transition-colors cursor-pointer">Cancel</button>
            <button onClick={handleDelete} className="px-4 py-2 text-sm bg-rose-500 text-white rounded-lg hover:bg-rose-400 transition-colors cursor-pointer">Delete</button>
          </>
        }>
          <p className="text-sm text-slate-400 mb-4 leading-relaxed">
            Are you sure you want to delete <strong className="text-white">{deleteTarget.name}</strong> ({deleteTarget.email})? This action cannot be undone.
          </p>
        </Modal>
      )}
    </div>
  );
}

function StatCard({ icon, iconBg, value, label }) {
  return (
    <div className="bg-gray-800/50 border border-white/8 rounded-xl p-5 flex items-center gap-4 hover:border-white/15 hover:-translate-y-0.5 hover:shadow-lg transition-all">
      <div className={`w-11 h-11 rounded-lg flex items-center justify-center text-xl shrink-0 ${iconBg}`}>{icon}</div>
      <div>
        <div className="text-xl font-bold">{value}</div>
        <div className="text-[0.7rem] text-slate-400 uppercase tracking-wide mt-0.5">{label}</div>
      </div>
    </div>
  );
}

function Th({ children }) {
  return <th className="text-[0.7rem] font-semibold uppercase tracking-wide text-slate-500 px-4 py-3 text-left border-b border-white/8 bg-gray-900 sticky top-0">{children}</th>;
}

function Td({ children, className = '' }) {
  return <td className={`px-4 py-3 border-b border-white/[0.03] text-sm ${className}`}>{children}</td>;
}

const inputCls = "w-full px-3 py-2 bg-white/5 border border-white/10 rounded-lg text-slate-200 text-sm outline-none focus:border-indigo-500/50 focus:ring-2 focus:ring-indigo-500/20 transition placeholder:text-slate-600";

function CreateUserModal({ token, onClose, onCreated }) {
  const toast = useToast();
  const [form, setForm] = useState({ name: '', email: '', password: '', phone: '', role: 'USER' });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await api.adminCreateUser(form, token);
      toast.success('User Created', `${form.name} created with role ${form.role}`);
      onCreated();
    } catch (err) {
      toast.error('Creation Failed', err.message);
    } finally {
      setLoading(false);
    }
  };

  const update = (f, v) => setForm(p => ({ ...p, [f]: v }));

  return (
    <Modal title="Create User" onClose={onClose} footer={
      <>
        <button onClick={onClose} className="px-4 py-2 text-sm border border-white/10 text-slate-400 rounded-lg hover:bg-white/5 transition-colors cursor-pointer">Cancel</button>
        <button onClick={handleSubmit} disabled={loading} className="px-4 py-2 text-sm bg-gradient-to-r from-indigo-500 to-violet-500 text-white rounded-lg hover:brightness-110 transition-all cursor-pointer disabled:opacity-50 flex items-center gap-2">
          {loading ? <Spinner /> : 'Create'}
        </button>
      </>
    }>
      <form onSubmit={handleSubmit}>
        <div className="grid grid-cols-2 gap-3">
          <div className="mb-3"><label className="block text-xs font-medium text-slate-400 mb-1">Name</label><input className={inputCls} value={form.name} onChange={e => update('name', e.target.value)} required /></div>
          <div className="mb-3"><label className="block text-xs font-medium text-slate-400 mb-1">Phone</label><input className={inputCls} value={form.phone} onChange={e => update('phone', e.target.value)} required /></div>
        </div>
        <div className="mb-3"><label className="block text-xs font-medium text-slate-400 mb-1">Email</label><input className={inputCls} type="email" value={form.email} onChange={e => update('email', e.target.value)} required /></div>
        <div className="grid grid-cols-2 gap-3">
          <div className="mb-3"><label className="block text-xs font-medium text-slate-400 mb-1">Password</label><input className={inputCls} type="password" value={form.password} onChange={e => update('password', e.target.value)} required minLength={6} /></div>
          <div className="mb-3"><label className="block text-xs font-medium text-slate-400 mb-1">Role</label>
            <select className={inputCls + ' cursor-pointer'} value={form.role} onChange={e => update('role', e.target.value)}>
              <option value="USER">USER</option><option value="MODERATOR">MODERATOR</option><option value="ADMIN">ADMIN</option>
            </select>
          </div>
        </div>
      </form>
    </Modal>
  );
}

function EditUserModal({ token, user, onClose, onSaved }) {
  const toast = useToast();
  const [form, setForm] = useState({ name: user.name, email: user.email, phone: '', emailVerified: user.emailVerified, phoneVerified: user.phoneVerified, role: user.role });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const payload = {};
      if (form.name !== user.name) payload.name = form.name;
      if (form.email !== user.email) payload.email = form.email;
      if (form.phone) payload.phone = form.phone;
      if (form.emailVerified !== user.emailVerified) payload.emailVerified = form.emailVerified;
      if (form.phoneVerified !== user.phoneVerified) payload.phoneVerified = form.phoneVerified;
      if (form.role !== user.role) payload.role = form.role;
      await api.adminUpdateUser(user.id, payload, token);
      toast.success('User Updated', `${form.name} updated successfully`);
      onSaved();
    } catch (err) {
      toast.error('Update Failed', err.message);
    } finally {
      setLoading(false);
    }
  };

  const update = (f, v) => setForm(p => ({ ...p, [f]: v }));

  return (
    <Modal title={`Edit User #${user.id}`} onClose={onClose} footer={
      <>
        <button onClick={onClose} className="px-4 py-2 text-sm border border-white/10 text-slate-400 rounded-lg hover:bg-white/5 transition-colors cursor-pointer">Cancel</button>
        <button onClick={handleSubmit} disabled={loading} className="px-4 py-2 text-sm bg-gradient-to-r from-indigo-500 to-violet-500 text-white rounded-lg hover:brightness-110 transition-all cursor-pointer disabled:opacity-50 flex items-center gap-2">
          {loading ? <Spinner /> : 'Save Changes'}
        </button>
      </>
    }>
      <form onSubmit={handleSubmit}>
        <div className="grid grid-cols-2 gap-3">
          <div className="mb-3"><label className="block text-xs font-medium text-slate-400 mb-1">Name</label><input className={inputCls} value={form.name} onChange={e => update('name', e.target.value)} /></div>
          <div className="mb-3"><label className="block text-xs font-medium text-slate-400 mb-1">Role</label>
            <select className={inputCls + ' cursor-pointer'} value={form.role} onChange={e => update('role', e.target.value)}>
              <option value="USER">USER</option><option value="MODERATOR">MODERATOR</option><option value="ADMIN">ADMIN</option>
            </select>
          </div>
        </div>
        <div className="mb-3"><label className="block text-xs font-medium text-slate-400 mb-1">Email</label><input className={inputCls} type="email" value={form.email} onChange={e => update('email', e.target.value)} /></div>
        <div className="mb-3"><label className="block text-xs font-medium text-slate-400 mb-1">Phone (leave blank to keep current)</label><input className={inputCls} value={form.phone} onChange={e => update('phone', e.target.value)} placeholder="Leave empty to keep unchanged" /></div>
        <hr className="border-white/10 my-4" />
        <div className="grid grid-cols-2 gap-3">
          <label className="flex items-center gap-2 cursor-pointer text-sm">
            <input type="checkbox" checked={form.emailVerified} onChange={e => update('emailVerified', e.target.checked)} className="w-4 h-4 accent-indigo-500 cursor-pointer" />
            Email Verified
          </label>
          <label className="flex items-center gap-2 cursor-pointer text-sm">
            <input type="checkbox" checked={form.phoneVerified} onChange={e => update('phoneVerified', e.target.checked)} className="w-4 h-4 accent-indigo-500 cursor-pointer" />
            Phone Verified
          </label>
        </div>
      </form>
    </Modal>
  );
}
