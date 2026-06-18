import { useState, useEffect, useCallback } from 'react';
import { useToast } from '../context/ToastContext';
import { useSession } from '../context/SessionContext';
import { EmptyState, Spinner, Modal, StatusTag } from './ui';
import api from '../api';

const inputCls = "w-full px-3 py-2 bg-white/5 border border-white/10 rounded-lg text-slate-200 text-sm outline-none focus:border-indigo-500/50 focus:ring-2 focus:ring-indigo-500/20 transition placeholder:text-slate-600";

export default function UserTeamsPage({ sessionKey }) {
  const { sessions } = useSession();
  const token = sessions[sessionKey]?.token;
  const user = sessions[sessionKey]?.user;

  if (!token) {
    return <EmptyState icon="👤" text={`Log in to ${sessionKey === 'user1' ? 'User Session 1' : 'User Session 2'} in the Sessions tab first.`} />;
  }

  return (
    <div>
      <div className="mb-5">
        <h2 className="text-xl font-bold tracking-tight">{sessionKey === 'user1' ? '👤' : '👥'} {user?.name || sessionKey}</h2>
        <p className="text-sm text-slate-400 mt-1">{user?.email} · Teams, Invitations & Verification</p>
      </div>
      <UserSubTabs sessionKey={sessionKey} token={token} user={user} />
    </div>
  );
}

function PillTabs({ tabs, active, onChange }) {
  return (
    <div className="flex gap-0.5 p-1 bg-white/5 rounded-xl mb-5 w-fit">
      {tabs.map(t => (
        <button key={t.key} onClick={() => onChange(t.key)} className={`px-4 py-1.5 text-sm font-medium rounded-lg transition-all cursor-pointer ${active === t.key ? 'bg-indigo-500 text-white' : 'text-slate-400 hover:text-white'}`}>
          {t.label}
        </button>
      ))}
    </div>
  );
}

function UserSubTabs({ sessionKey, token, user }) {
  const [tab, setTab] = useState('myteams');
  return (
    <div>
      <PillTabs
        tabs={[
          { key: 'myteams', label: 'My Teams' },
          { key: 'discover', label: 'Discover' },
          { key: 'requests', label: 'My Requests' },
          { key: 'verify', label: 'Verify' },
        ]}
        active={tab}
        onChange={setTab}
      />
      {tab === 'myteams' && <MyTeamsTab token={token} user={user} sessionKey={sessionKey} />}
      {tab === 'discover' && <DiscoverTab token={token} />}
      {tab === 'requests' && <MyRequestsTab token={token} />}
      {tab === 'verify' && <VerifyTab token={token} user={user} sessionKey={sessionKey} />}
    </div>
  );
}

/* ========== MY TEAMS ========== */
function MyTeamsTab({ token, user }) {
  const toast = useToast();
  const [teams, setTeams] = useState([]);
  const [loading, setLoading] = useState(false);
  const [createOpen, setCreateOpen] = useState(false);
  const [selectedTeam, setSelectedTeam] = useState(null);

  const loadTeams = useCallback(async () => {
    setLoading(true);
    try { setTeams(await api.getMyTeams(token)); }
    catch (err) { toast.error('Failed', err.message); }
    finally { setLoading(false); }
  }, [token, toast]);

  useEffect(() => { loadTeams(); }, [loadTeams]);

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <span className="text-sm text-slate-500">{teams.length} teams</span>
        <div className="flex items-center gap-2">
          <button onClick={loadTeams} disabled={loading} className="px-3 py-1.5 text-xs border border-white/10 text-slate-400 rounded-lg hover:bg-white/5 hover:text-white transition-colors cursor-pointer flex items-center gap-1">
            {loading ? <Spinner /> : '↻ Refresh'}
          </button>
          <button onClick={() => setCreateOpen(true)} className="px-3 py-1.5 text-xs bg-gradient-to-r from-indigo-500 to-violet-500 text-white rounded-lg hover:brightness-110 transition-all cursor-pointer">+ Create Team</button>
        </div>
      </div>

      {teams.length === 0 && !loading ? (
        <EmptyState icon="🏢" text="You're not in any teams yet. Create one or discover teams to join!" />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {teams.map(team => (
            <div key={team.id} onClick={() => setSelectedTeam(team)} className="bg-gray-800/50 border border-white/8 rounded-xl p-5 cursor-pointer hover:border-white/15 hover:-translate-y-0.5 hover:shadow-lg transition-all">
              <div className="font-semibold mb-2">{team.name}</div>
              <div className="text-xs text-slate-500 mb-4">
                Max Members: {team.maxMembers} · Owner: #{team.ownerId}
                {team.ownerId === user?.id && <span className="ml-2 inline-flex px-2 py-0.5 rounded-full text-[0.65rem] font-semibold bg-indigo-500/15 text-indigo-400 uppercase">Owner</span>}
              </div>
              <div className="text-[0.7rem] text-slate-600">Created {new Date(team.createdAt).toLocaleDateString()}</div>
            </div>
          ))}
        </div>
      )}

      {createOpen && <CreateTeamModal token={token} onClose={() => setCreateOpen(false)} onCreated={() => { setCreateOpen(false); loadTeams(); }} />}
      {selectedTeam && <TeamDetailModal token={token} team={selectedTeam} user={user} onClose={() => setSelectedTeam(null)} onRefresh={() => { setSelectedTeam(null); loadTeams(); }} />}
    </div>
  );
}

function CreateTeamModal({ token, onClose, onCreated }) {
  const toast = useToast();
  const [name, setName] = useState('');
  const [maxMembers, setMaxMembers] = useState(5);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try { await api.createTeam(name, maxMembers, token); toast.success('Team Created', `"${name}" created successfully`); onCreated(); }
    catch (err) { toast.error('Failed', err.message); }
    finally { setLoading(false); }
  };

  return (
    <Modal title="Create Team" onClose={onClose} footer={
      <>
        <button onClick={onClose} className="px-4 py-2 text-sm border border-white/10 text-slate-400 rounded-lg hover:bg-white/5 transition-colors cursor-pointer">Cancel</button>
        <button onClick={handleSubmit} disabled={loading} className="px-4 py-2 text-sm bg-gradient-to-r from-indigo-500 to-violet-500 text-white rounded-lg hover:brightness-110 transition-all cursor-pointer disabled:opacity-50 flex items-center gap-2">
          {loading ? <Spinner /> : 'Create'}
        </button>
      </>
    }>
      <form onSubmit={handleSubmit}>
        <div className="mb-3"><label className="block text-xs font-medium text-slate-400 mb-1">Team Name</label><input className={inputCls} value={name} onChange={e => setName(e.target.value)} placeholder="My Awesome Team" required /></div>
        <div className="mb-3"><label className="block text-xs font-medium text-slate-400 mb-1">Max Members</label><input className={inputCls} type="number" min={1} value={maxMembers} onChange={e => setMaxMembers(parseInt(e.target.value))} required /></div>
      </form>
    </Modal>
  );
}

function TeamDetailModal({ token, team, user, onClose, onRefresh }) {
  const toast = useToast();
  const [members, setMembers] = useState([]);
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [inviteEmail, setInviteEmail] = useState('');
  const [editOpen, setEditOpen] = useState(false);
  const [editName, setEditName] = useState(team.name);
  const [editMax, setEditMax] = useState(team.maxMembers);
  const [tab, setTab] = useState('members');
  const isOwner = team.ownerId === user?.id;

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [m, r] = await Promise.all([
        api.getTeamMembers(team.id, token),
        isOwner ? api.getTeamPendingRequests(team.id, token).catch(() => []) : Promise.resolve([]),
      ]);
      setMembers(m); setRequests(r);
    } catch (err) { toast.error('Failed', err.message); }
    finally { setLoading(false); }
  }, [team.id, token, isOwner, toast]);

  useEffect(() => { load(); }, [load]);

  const handleInvite = async () => {
    if (!inviteEmail.trim()) return;
    try { await api.inviteUser(team.id, inviteEmail, token); toast.success('Invited', `Invitation sent to ${inviteEmail}`); setInviteEmail(''); load(); }
    catch (err) { toast.error('Invite Failed', err.message); }
  };

  const handleRemoveMember = async (id) => {
    try { await api.removeTeamMember(team.id, id, token); toast.success('Removed', 'Member removed'); load(); }
    catch (err) { toast.error('Failed', err.message); }
  };

  const handleTransfer = async (id) => {
    try { await api.transferOwnership(team.id, id, token); toast.success('Transferred', 'Ownership transferred'); onRefresh(); }
    catch (err) { toast.error('Failed', err.message); }
  };

  const handleRespond = async (userId, status) => {
    try { await api.respondToRequest(team.id, userId, status, token); toast.success(status, `Request ${status.toLowerCase()}`); load(); }
    catch (err) { toast.error('Failed', err.message); }
  };

  const handleDeleteTeam = async () => {
    try { await api.deleteTeam(team.id, token); toast.success('Deleted', `Team "${team.name}" deleted`); onRefresh(); }
    catch (err) { toast.error('Failed', err.message); }
  };

  const handleUpdate = async () => {
    try { await api.updateTeam(team.id, { name: editName, maxMembers: editMax }, token); toast.success('Updated', 'Team updated'); setEditOpen(false); onRefresh(); }
    catch (err) { toast.error('Failed', err.message); }
  };

  return (
    <Modal title={`Team: ${team.name}`} onClose={onClose} footer={
      <div className="flex items-center justify-between w-full">
        <div>{isOwner && <button onClick={handleDeleteTeam} className="px-3 py-1.5 text-xs bg-rose-500 text-white rounded-lg hover:bg-rose-400 transition-colors cursor-pointer">🗑️ Delete Team</button>}</div>
        <button onClick={onClose} className="px-4 py-2 text-sm border border-white/10 text-slate-400 rounded-lg hover:bg-white/5 transition-colors cursor-pointer">Close</button>
      </div>
    }>
      {loading ? <div className="text-center py-6"><Spinner /></div> : (
        <div>
          <div className="flex gap-3 mb-4 text-xs text-slate-500">
            <span>ID: #{team.id}</span><span>Owner: #{team.ownerId}</span><span>Max: {team.maxMembers}</span>
            {isOwner && <span className="inline-flex px-2 py-0.5 rounded-full text-[0.65rem] font-semibold bg-indigo-500/15 text-indigo-400 uppercase">You own this</span>}
          </div>

          {isOwner && (
            <div className="flex gap-2 mb-4">
              <button onClick={() => setEditOpen(!editOpen)} className="px-3 py-1 text-xs border border-white/10 text-slate-400 rounded-lg hover:bg-white/5 hover:text-white transition-colors cursor-pointer">✏️ {editOpen ? 'Cancel Edit' : 'Edit Team'}</button>
            </div>
          )}

          {editOpen && (
            <div className="bg-gray-800/50 border border-white/8 rounded-xl p-4 mb-4">
              <div className="grid grid-cols-2 gap-3">
                <div><label className="block text-xs font-medium text-slate-400 mb-1">Team Name</label><input className={inputCls} value={editName} onChange={e => setEditName(e.target.value)} /></div>
                <div><label className="block text-xs font-medium text-slate-400 mb-1">Max Members</label><input className={inputCls} type="number" min={1} value={editMax} onChange={e => setEditMax(parseInt(e.target.value))} /></div>
              </div>
              <button onClick={handleUpdate} className="mt-3 px-3 py-1.5 text-xs bg-gradient-to-r from-indigo-500 to-violet-500 text-white rounded-lg hover:brightness-110 transition-all cursor-pointer">Save</button>
            </div>
          )}

          {/* Sub-tabs */}
          <div className="flex gap-0.5 p-1 bg-white/5 rounded-xl mb-4 w-fit">
            <button onClick={() => setTab('members')} className={`px-3 py-1 text-xs font-medium rounded-lg transition-all cursor-pointer ${tab === 'members' ? 'bg-indigo-500 text-white' : 'text-slate-400 hover:text-white'}`}>Members ({members.length})</button>
            {isOwner && <>
              <button onClick={() => setTab('requests')} className={`px-3 py-1 text-xs font-medium rounded-lg transition-all cursor-pointer ${tab === 'requests' ? 'bg-indigo-500 text-white' : 'text-slate-400 hover:text-white'}`}>Requests ({requests.length})</button>
              <button onClick={() => setTab('invite')} className={`px-3 py-1 text-xs font-medium rounded-lg transition-all cursor-pointer ${tab === 'invite' ? 'bg-indigo-500 text-white' : 'text-slate-400 hover:text-white'}`}>Invite</button>
            </>}
          </div>

          {tab === 'members' && (
            members.length === 0 ? <EmptyState icon="👥" text="No members yet" /> : (
              <table className="w-full">
                <thead><tr className="text-left">
                  {['ID', 'Name', 'Email', ...(isOwner ? ['Actions'] : [])].map(h => <th key={h} className="text-[0.7rem] font-semibold uppercase tracking-wide text-slate-500 px-3 py-2 border-b border-white/8">{h}</th>)}
                </tr></thead>
                <tbody>
                  {members.map(m => (
                    <tr key={m.id} className="hover:bg-white/[0.03] transition-colors">
                      <td className="px-3 py-2 border-b border-white/[0.03] text-sm font-mono text-slate-500">#{m.id}</td>
                      <td className="px-3 py-2 border-b border-white/[0.03] text-sm font-medium">{m.name}</td>
                      <td className="px-3 py-2 border-b border-white/[0.03] text-sm text-slate-400">{m.email}</td>
                      {isOwner && <td className="px-3 py-2 border-b border-white/[0.03] text-sm">
                        {m.id !== user?.id && <div className="flex items-center gap-1">
                          <button onClick={() => handleTransfer(m.id)} title="Transfer ownership" className="px-2 py-0.5 text-xs bg-amber-500 text-gray-900 rounded-md hover:bg-amber-400 transition-colors cursor-pointer">👑</button>
                          <button onClick={() => handleRemoveMember(m.id)} className="px-2 py-0.5 text-xs bg-rose-500 text-white rounded-md hover:bg-rose-400 transition-colors cursor-pointer">Remove</button>
                        </div>}
                      </td>}
                    </tr>
                  ))}
                </tbody>
              </table>
            )
          )}

          {tab === 'requests' && isOwner && (
            requests.length === 0 ? <EmptyState icon="📩" text="No pending requests" /> : (
              <table className="w-full">
                <thead><tr className="text-left">
                  {['Req ID', 'User ID', 'Type', 'Actions'].map(h => <th key={h} className="text-[0.7rem] font-semibold uppercase tracking-wide text-slate-500 px-3 py-2 border-b border-white/8">{h}</th>)}
                </tr></thead>
                <tbody>
                  {requests.map(r => (
                    <tr key={r.id} className="hover:bg-white/[0.03] transition-colors">
                      <td className="px-3 py-2 border-b border-white/[0.03] text-sm font-mono">#{r.id}</td>
                      <td className="px-3 py-2 border-b border-white/[0.03] text-sm font-mono">#{r.userId}</td>
                      <td className="px-3 py-2 border-b border-white/[0.03] text-sm"><StatusTag status={r.type} /></td>
                      <td className="px-3 py-2 border-b border-white/[0.03] text-sm">
                        <div className="flex items-center gap-1">
                          <button onClick={() => handleRespond(r.userId, 'APPROVED')} className="px-2 py-0.5 text-xs bg-emerald-500 text-white rounded-md hover:bg-emerald-400 transition-colors cursor-pointer">✓ Approve</button>
                          <button onClick={() => handleRespond(r.userId, 'REJECTED')} className="px-2 py-0.5 text-xs bg-rose-500 text-white rounded-md hover:bg-rose-400 transition-colors cursor-pointer">✕ Reject</button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )
          )}

          {tab === 'invite' && isOwner && (
            <div className="flex gap-2 items-end">
              <div className="flex-1">
                <label className="block text-xs font-medium text-slate-400 mb-1">Email to invite</label>
                <input className={inputCls} type="email" value={inviteEmail} onChange={e => setInviteEmail(e.target.value)} placeholder="user@example.com" onKeyDown={e => e.key === 'Enter' && handleInvite()} />
              </div>
              <button onClick={handleInvite} className="px-4 py-2 text-sm bg-gradient-to-r from-indigo-500 to-violet-500 text-white rounded-lg hover:brightness-110 transition-all cursor-pointer">Send Invite</button>
            </div>
          )}
        </div>
      )}
    </Modal>
  );
}

/* ========== DISCOVER ========== */
function DiscoverTab({ token }) {
  const toast = useToast();
  const [teams, setTeams] = useState([]);
  const [loading, setLoading] = useState(false);

  const loadTeams = useCallback(async () => {
    setLoading(true);
    try { setTeams(await api.discoverTeams(token)); }
    catch (err) { toast.error('Failed', err.message); }
    finally { setLoading(false); }
  }, [token, toast]);

  useEffect(() => { loadTeams(); }, [loadTeams]);

  const handleJoin = async (teamId, teamName) => {
    try { await api.requestToJoin(teamId, token); toast.success('Request Sent', `Join request sent to "${teamName}"`); loadTeams(); }
    catch (err) { toast.error('Failed', err.message); }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <span className="text-sm text-slate-500">{teams.length} teams available to join</span>
        <button onClick={loadTeams} disabled={loading} className="px-3 py-1.5 text-xs border border-white/10 text-slate-400 rounded-lg hover:bg-white/5 hover:text-white transition-colors cursor-pointer flex items-center gap-1">{loading ? <Spinner /> : '↻ Refresh'}</button>
      </div>

      {teams.length === 0 && !loading ? <EmptyState icon="🔍" text="No teams available to join right now" /> : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {teams.map(team => (
            <div key={team.id} className="bg-gray-800/50 border border-white/8 rounded-xl p-5 hover:border-white/15 hover:-translate-y-0.5 hover:shadow-lg transition-all">
              <div className="font-semibold mb-2">{team.name}</div>
              <div className="text-xs text-slate-500 mb-4">Max: {team.maxMembers} · Owner: #{team.ownerId}</div>
              <button onClick={() => handleJoin(team.id, team.name)} className="px-3 py-1.5 text-xs bg-gradient-to-r from-indigo-500 to-violet-500 text-white rounded-lg hover:brightness-110 transition-all cursor-pointer">🚀 Request to Join</button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

/* ========== MY REQUESTS ========== */
function MyRequestsTab({ token }) {
  const toast = useToast();
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(false);

  const loadRequests = useCallback(async () => {
    setLoading(true);
    try { setRequests(await api.getMyPendingRequests(token)); }
    catch (err) { toast.error('Failed', err.message); }
    finally { setLoading(false); }
  }, [token, toast]);

  useEffect(() => { loadRequests(); }, [loadRequests]);

  const handleRespond = async (teamId, status) => {
    try { await api.respondToInvitation(teamId, status, token); toast.success(status, `Invitation ${status.toLowerCase()}`); loadRequests(); }
    catch (err) { toast.error('Failed', err.message); }
  };

  const handleCancel = async (teamId) => {
    try { await api.deleteJoinRequest(teamId, token); toast.success('Cancelled', 'Join request cancelled'); loadRequests(); }
    catch (err) { toast.error('Failed', err.message); }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <span className="text-sm text-slate-500">{requests.length} pending requests/invitations</span>
        <button onClick={loadRequests} disabled={loading} className="px-3 py-1.5 text-xs border border-white/10 text-slate-400 rounded-lg hover:bg-white/5 hover:text-white transition-colors cursor-pointer flex items-center gap-1">{loading ? <Spinner /> : '↻ Refresh'}</button>
      </div>

      {requests.length === 0 && !loading ? <EmptyState icon="📬" text="No pending requests or invitations" /> : (
        <div className="bg-gray-800/50 border border-white/8 rounded-xl overflow-hidden backdrop-blur-sm">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead><tr className="text-left">
                {['ID', 'Team ID', 'Type', 'Actions'].map(h => <th key={h} className="text-[0.7rem] font-semibold uppercase tracking-wide text-slate-500 px-4 py-3 border-b border-white/8 bg-gray-900">{h}</th>)}
              </tr></thead>
              <tbody>
                {requests.map(r => (
                  <tr key={r.id} className="hover:bg-white/[0.03] transition-colors">
                    <td className="px-4 py-3 border-b border-white/[0.03] text-sm font-mono">#{r.id}</td>
                    <td className="px-4 py-3 border-b border-white/[0.03] text-sm font-mono">Team #{r.teamId}</td>
                    <td className="px-4 py-3 border-b border-white/[0.03] text-sm"><StatusTag status={r.type} /></td>
                    <td className="px-4 py-3 border-b border-white/[0.03] text-sm">
                      {r.type === 'PENDING' ? (
                        <div className="flex items-center gap-1">
                          <button onClick={() => handleRespond(r.teamId, 'APPROVED')} className="px-2 py-0.5 text-xs bg-emerald-500 text-white rounded-md hover:bg-emerald-400 transition-colors cursor-pointer">✓ Accept</button>
                          <button onClick={() => handleRespond(r.teamId, 'REJECTED')} className="px-2 py-0.5 text-xs bg-rose-500 text-white rounded-md hover:bg-rose-400 transition-colors cursor-pointer">✕ Reject</button>
                          <button onClick={() => handleCancel(r.teamId)} className="px-2 py-0.5 text-xs border border-white/10 text-slate-400 rounded-md hover:bg-white/5 transition-colors cursor-pointer">Cancel</button>
                        </div>
                      ) : <StatusTag status={r.type} />}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

/* ========== VERIFY ========== */
function VerifyTab({ token, user, sessionKey }) {
  const toast = useToast();
  const { refreshUser } = useSession();
  const [emailOtp, setEmailOtp] = useState('');
  const [phoneOtp, setPhoneOtp] = useState('');
  const [emailLoading, setEmailLoading] = useState(false);
  const [phoneLoading, setPhoneLoading] = useState(false);

  const handleGenerateEmailOtp = async () => {
    try { await api.generateEmailOtp(token); toast.success('OTP Sent', 'Email OTP generated — check server logs/email'); }
    catch (err) { toast.error('Failed', err.message); }
  };

  const handleVerifyEmail = async () => {
    if (!emailOtp.trim()) return;
    setEmailLoading(true);
    try { await api.verifyEmail(emailOtp, token); toast.success('Verified', 'Email verified!'); setEmailOtp(''); refreshUser(sessionKey); }
    catch (err) { toast.error('Failed', err.message); }
    finally { setEmailLoading(false); }
  };

  const handleGeneratePhoneOtp = async () => {
    try { await api.generatePhoneOtp(token); toast.success('OTP Sent', 'Phone OTP generated — check server console'); }
    catch (err) { toast.error('Failed', err.message); }
  };

  const handleVerifyPhone = async () => {
    if (!phoneOtp.trim()) return;
    setPhoneLoading(true);
    try { await api.verifyPhone(phoneOtp, token); toast.success('Verified', 'Phone verified!'); setPhoneOtp(''); refreshUser(sessionKey); }
    catch (err) { toast.error('Failed', err.message); }
    finally { setPhoneLoading(false); }
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
      {/* Email */}
      <div className="bg-gray-800/50 border border-white/8 rounded-xl overflow-hidden backdrop-blur-sm">
        <div className="p-4 px-5 border-b border-white/8 flex items-center justify-between">
          <span className="text-sm font-semibold flex items-center gap-2">📧 Email Verification</span>
          <span className={`inline-flex px-2 py-0.5 rounded-full text-[0.7rem] font-semibold ${user?.emailVerified ? 'bg-emerald-500/15 text-emerald-400' : 'bg-rose-500/15 text-rose-400'}`}>
            {user?.emailVerified ? '✓ Verified' : '✕ Not Verified'}
          </span>
        </div>
        <div className="p-5">
          {user?.emailVerified ? (
            <p className="text-sm text-slate-500">Email is already verified ✓</p>
          ) : (
            <>
              <button onClick={handleGenerateEmailOtp} className="px-3 py-1.5 text-xs border border-white/10 text-slate-400 rounded-lg hover:bg-white/5 hover:text-white transition-colors cursor-pointer mb-4">📤 Generate Email OTP</button>
              <div className="flex gap-2 items-end">
                <div className="flex-1"><input className={inputCls} value={emailOtp} onChange={e => setEmailOtp(e.target.value)} placeholder="Enter OTP" /></div>
                <button onClick={handleVerifyEmail} disabled={emailLoading} className="px-4 py-2 text-sm bg-gradient-to-r from-indigo-500 to-violet-500 text-white rounded-lg hover:brightness-110 transition-all cursor-pointer disabled:opacity-50 flex items-center gap-2">{emailLoading ? <Spinner /> : 'Verify'}</button>
              </div>
              <p className="text-[0.7rem] text-slate-600 mt-3">💡 Tip: Admin can skip this by toggling verification in Admin Panel</p>
            </>
          )}
        </div>
      </div>

      {/* Phone */}
      <div className="bg-gray-800/50 border border-white/8 rounded-xl overflow-hidden backdrop-blur-sm">
        <div className="p-4 px-5 border-b border-white/8 flex items-center justify-between">
          <span className="text-sm font-semibold flex items-center gap-2">📱 Phone Verification</span>
          <span className={`inline-flex px-2 py-0.5 rounded-full text-[0.7rem] font-semibold ${user?.phoneVerified ? 'bg-emerald-500/15 text-emerald-400' : 'bg-rose-500/15 text-rose-400'}`}>
            {user?.phoneVerified ? '✓ Verified' : '✕ Not Verified'}
          </span>
        </div>
        <div className="p-5">
          {user?.phoneVerified ? (
            <p className="text-sm text-slate-500">Phone is already verified ✓</p>
          ) : (
            <>
              <button onClick={handleGeneratePhoneOtp} className="px-3 py-1.5 text-xs border border-white/10 text-slate-400 rounded-lg hover:bg-white/5 hover:text-white transition-colors cursor-pointer mb-4">📤 Generate Phone OTP</button>
              <div className="flex gap-2 items-end">
                <div className="flex-1"><input className={inputCls} value={phoneOtp} onChange={e => setPhoneOtp(e.target.value)} placeholder="Enter OTP" /></div>
                <button onClick={handleVerifyPhone} disabled={phoneLoading} className="px-4 py-2 text-sm bg-gradient-to-r from-indigo-500 to-violet-500 text-white rounded-lg hover:brightness-110 transition-all cursor-pointer disabled:opacity-50 flex items-center gap-2">{phoneLoading ? <Spinner /> : 'Verify'}</button>
              </div>
              <p className="text-[0.7rem] text-slate-600 mt-3">💡 Tip: OTP is printed to server console (ConsoleSmsService)</p>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
