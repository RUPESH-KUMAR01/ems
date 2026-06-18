const BASE_URL = '/api';

class ApiClient {
  constructor() {
    this.token = null;
  }

  setToken(token) {
    this.token = token;
  }

  getToken() {
    return this.token;
  }

  async request(method, path, body = null, customToken = null) {
    const headers = { 'Content-Type': 'application/json' };
    const tok = customToken || this.token;
    if (tok) {
      headers['Authorization'] = `Bearer ${tok}`;
    }

    const opts = { method, headers };
    if (body && method !== 'GET') {
      opts.body = JSON.stringify(body);
    }

    const res = await fetch(`${BASE_URL}${path}`, opts);

    if (res.status === 204 || res.headers.get('content-length') === '0') {
      if (!res.ok) {
        throw new Error(`Request failed: ${res.status}`);
      }
      return null;
    }

    const data = await res.json().catch(() => null);

    if (!res.ok) {
      const msg = data?.message || data?.error || `Request failed: ${res.status}`;
      const err = new Error(msg);
      err.status = res.status;
      err.data = data;
      throw err;
    }

    return data;
  }

  // ---- Auth ----
  register(name, email, password, phone) {
    return this.request('POST', '/auth/register', { name, email, password, phone });
  }

  login(email, password) {
    return this.request('POST', '/auth/login', { email, password });
  }

  me(token) {
    return this.request('GET', '/auth/me', null, token);
  }

  generateEmailOtp(token) {
    return this.request('POST', '/auth/generate-email-otp', null, token);
  }

  generatePhoneOtp(token) {
    return this.request('POST', '/auth/generate-phone-otp', null, token);
  }

  verifyEmail(otp, token) {
    return this.request('POST', '/auth/verify-email', { otp }, token);
  }

  verifyPhone(otp, token) {
    return this.request('POST', '/auth/verify-phone', { otp }, token);
  }

  // ---- Admin ----
  adminGetAllUsers(token) {
    return this.request('GET', '/admin/users', null, token);
  }

  adminGetUserById(id, token) {
    return this.request('GET', `/admin/users/${id}`, null, token);
  }

  adminSearchByEmail(email, token) {
    return this.request('GET', `/admin/users/search/email?email=${encodeURIComponent(email)}`, null, token);
  }

  adminSearchByPhone(phone, token) {
    return this.request('GET', `/admin/users/search/phone?phone=${encodeURIComponent(phone)}`, null, token);
  }

  adminCreateUser(data, token) {
    return this.request('POST', '/admin/users', data, token);
  }

  adminUpdateUser(id, data, token) {
    return this.request('PUT', `/admin/users/${id}`, data, token);
  }

  adminChangeRole(id, role, token) {
    return this.request('PUT', `/admin/users/${id}/role`, { role }, token);
  }

  adminDeleteUser(id, token) {
    return this.request('DELETE', `/admin/users/${id}`, null, token);
  }

  adminGetAllTeams(token) {
    return this.request('GET', '/admin/teams', null, token);
  }

  adminGetTeamById(teamId, token) {
    return this.request('GET', `/admin/teams/${teamId}`, null, token);
  }

  adminGetTeamMembers(teamId, token) {
    return this.request('GET', `/admin/teams/${teamId}/members`, null, token);
  }

  adminGetTeamRequests(teamId, token) {
    return this.request('GET', `/admin/teams/${teamId}/requests`, null, token);
  }

  adminTransferTeamOwnership(teamId, userId, token) {
    return this.request('PUT', `/admin/teams/${teamId}/owner/${userId}`, null, token);
  }

  adminRemoveTeamMember(teamId, userId, token) {
    return this.request('DELETE', `/admin/teams/${teamId}/members/${userId}`, null, token);
  }

  adminDeleteTeam(teamId, token) {
    return this.request('DELETE', `/admin/teams/${teamId}`, null, token);
  }

  // ---- Teams ----
  getMyTeams(token) {
    return this.request('GET', '/teams', null, token);
  }

  discoverTeams(token) {
    return this.request('GET', '/teams/discover', null, token);
  }

  getTeamById(teamId, token) {
    return this.request('GET', `/teams/${teamId}`, null, token);
  }

  createTeam(name, maxMembers, token) {
    return this.request('POST', '/teams', { name, maxMembers }, token);
  }

  updateTeam(teamId, data, token) {
    return this.request('PUT', `/teams/${teamId}`, data, token);
  }

  deleteTeam(teamId, token) {
    return this.request('DELETE', `/teams/${teamId}`, null, token);
  }

  getTeamMembers(teamId, token) {
    return this.request('GET', `/teams/${teamId}/members`, null, token);
  }

  removeTeamMember(teamId, userId, token) {
    return this.request('DELETE', `/teams/${teamId}/members/${userId}`, null, token);
  }

  transferOwnership(teamId, userId, token) {
    return this.request('PUT', `/teams/${teamId}/owner/${userId}`, null, token);
  }

  // ---- Team Requests ----
  requestToJoin(teamId, token) {
    return this.request('POST', `/teams/${teamId}/requests`, null, token);
  }

  deleteJoinRequest(teamId, token) {
    return this.request('DELETE', `/teams/${teamId}/requests`, null, token);
  }

  getMyPendingRequests(token) {
    return this.request('GET', '/teams/requests/me', null, token);
  }

  getTeamPendingRequests(teamId, token) {
    return this.request('GET', `/teams/${teamId}/requests`, null, token);
  }

  respondToRequest(teamId, userId, status, token) {
    return this.request('PUT', `/teams/${teamId}/requests/${userId}`, { status }, token);
  }

  // ---- Team Invitations ----
  inviteUser(teamId, email, token) {
    return this.request('POST', `/teams/${teamId}/invitations`, { email }, token);
  }

  deleteInvitation(teamId, userId, token) {
    return this.request('DELETE', `/teams/${teamId}/invitations/${userId}`, null, token);
  }

  respondToInvitation(teamId, status, token) {
    return this.request('PUT', `/teams/${teamId}/invitations`, { status }, token);
  }
}

const api = new ApiClient();
export default api;
