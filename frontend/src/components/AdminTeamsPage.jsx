import { useState, useEffect, useCallback } from 'react';
import { useSession } from '../context/SessionContext';
import { useToast } from '../context/ToastContext';
import { EmptyState, Spinner, Modal } from './ui';
import api from '../api';

export default function AdminTeamsPage() {
  const { sessions } = useSession();
  const adminToken = sessions.admin?.token;

  if (!adminToken) {
    return <EmptyState icon="🛡️" text="Log in as Admin in the Sessions tab to view all teams." />;
  }

  return (
    <div>
      <div className="mb-5">
        <h2 className="text-xl font-bold tracking-tight">🏢 All Teams (Admin View)</h2>
        <p className="text-sm text-slate-400 mt-1">View all teams and their members across the system</p>
      </div>
      <TeamsList token={adminToken} />
    </div>
  );
}

function TeamsList({ token }) {
  const toast = useToast();
  const [teams, setTeams] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedTeam, setSelectedTeam] = useState(null);
  const [members, setMembers] = useState([]);
  const [membersLoading, setMembersLoading] = useState(false);

  const loadTeams = useCallback(async () => {
    setLoading(true);
    try { setTeams(await api.adminGetAllTeams(token)); }
    catch (err) { toast.error('Failed', err.message); }
    finally { setLoading(false); }
  }, [token, toast]);

  useEffect(() => { loadTeams(); }, [loadTeams]);

  const viewMembers = async (team) => {
    setSelectedTeam(team);
    setMembersLoading(true);
    try { setMembers(await api.adminGetTeamMembers(team.id, token)); }
    catch (err) { toast.error('Failed', err.message); setMembers([]); }
    finally { setMembersLoading(false); }
  };

  const deleteTeam = async (team) => {
    if (!window.confirm(`Delete team \"${team.name}\"? This cannot be undone.`)) return;
    try {
      await api.adminDeleteTeam(team.id, token);
      toast.success('Deleted', 'Team deleted successfully');
      if (selectedTeam?.id === team.id) {
        setSelectedTeam(null);
      }
      await loadTeams();
    } catch (err) {
      toast.error('Failed', err.message);
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <span className="text-sm text-slate-500">{teams.length} teams total</span>
        <button onClick={loadTeams} disabled={loading} className="px-3 py-1.5 text-xs border border-white/10 text-slate-400 rounded-lg hover:bg-white/5 hover:text-white transition-colors cursor-pointer flex items-center gap-2">
          {loading ? <Spinner /> : '↻ Refresh'}
        </button>
      </div>

      {teams.length === 0 && !loading ? (
        <EmptyState icon="🏢" text="No teams exist yet" />
      ) : (
        <div className="bg-gray-800/50 border border-white/8 rounded-xl overflow-hidden backdrop-blur-sm">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="text-left">
                  {['ID', 'Name', 'Owner ID', 'Max Members', 'Created', 'Actions'].map(h => (
                    <th key={h} className="text-[0.7rem] font-semibold uppercase tracking-wide text-slate-500 px-4 py-3 border-b border-white/8 bg-gray-900">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {teams.map(team => (
                  <tr key={team.id} className="hover:bg-white/3 transition-colors">
                    <td className="px-4 py-3 border-b border-white/3 text-sm font-mono text-slate-500">#{team.id}</td>
                    <td className="px-4 py-3 border-b border-white/3 text-sm font-medium">{team.name}</td>
                    <td className="px-4 py-3 border-b border-white/3 text-sm font-mono">#{team.ownerId}</td>
                    <td className="px-4 py-3 border-b border-white/3 text-sm">{team.maxMembers}</td>
                    <td className="px-4 py-3 border-b border-white/3 text-xs text-slate-500">{new Date(team.createdAt).toLocaleString()}</td>
                    <td className="px-4 py-3 border-b border-white/3 text-sm space-x-2">
                      <button onClick={() => viewMembers(team)} className="px-2 py-1 text-xs border border-white/10 text-slate-400 rounded-md hover:bg-white/5 hover:text-white transition-colors cursor-pointer">👥 Members</button>
                      <button onClick={() => deleteTeam(team)} className="px-2 py-1 text-xs border border-red-400/30 text-red-300 rounded-md hover:bg-red-500/10 hover:text-red-200 transition-colors cursor-pointer">🗑 Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {selectedTeam && (
        <Modal title={`Members of "${selectedTeam.name}"`} onClose={() => setSelectedTeam(null)} footer={<button onClick={() => setSelectedTeam(null)} className="px-4 py-2 text-sm border border-white/10 text-slate-400 rounded-lg hover:bg-white/5 transition-colors cursor-pointer">Close</button>}>
          {membersLoading ? (
            <div className="text-center py-6"><Spinner /></div>
          ) : members.length === 0 ? (
            <EmptyState icon="👥" text="No members in this team" />
          ) : (
            <table className="w-full">
              <thead>
                <tr className="text-left">
                  {['ID', 'Name', 'Email', 'Role'].map(h => (
                    <th key={h} className="text-[0.7rem] font-semibold uppercase tracking-wide text-slate-500 px-4 py-3 border-b border-white/8">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {members.map(m => (
                  <tr key={m.id} className="hover:bg-white/3 transition-colors">
                    <td className="px-4 py-3 border-b border-white/3 text-sm font-mono text-slate-500">#{m.id}</td>
                    <td className="px-4 py-3 border-b border-white/3 text-sm font-medium">{m.name}</td>
                    <td className="px-4 py-3 border-b border-white/3 text-sm text-slate-400">{m.email}</td>
                    <td className="px-4 py-3 border-b border-white/3 text-sm">
                      <span className={`inline-flex px-2 py-0.5 rounded-full text-[0.7rem] font-semibold uppercase ${m.role === 'ADMIN' ? 'bg-indigo-500/15 text-indigo-400' : m.role === 'MODERATOR' ? 'bg-amber-500/15 text-amber-400' : 'bg-emerald-500/15 text-emerald-400'}`}>{m.role}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Modal>
      )}
    </div>
  );
}
