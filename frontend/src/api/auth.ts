import { apiRequest, isMockMode, setToken } from './client'
import type {
  CreateUserRequest,
  EmailVerifyRequest,
  LoginRequest,
  LoginResponse,
  MessageResponse,
  PhoneVerifyRequest,
  RegisterResponse,
  UserResponse,
} from './types'

const mockUser: UserResponse = {
  id: 1,
  email: 'demo@ems.test',
  name: 'Demo User',
  role: 'USER',
  emailVerified: true,
  phoneVerified: false,
}

export async function register(body: CreateUserRequest): Promise<RegisterResponse> {
  if (isMockMode()) {
    setToken('mock-token')
    return { token: 'mock-token' }
  }
  const res = await apiRequest<RegisterResponse>('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(body),
    auth: false,
  })
  setToken(res.token)
  return res
}

export async function login(body: LoginRequest): Promise<LoginResponse> {
  if (isMockMode()) {
    setToken('mock-token')
    return { token: 'mock-token' }
  }
  const res = await apiRequest<LoginResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(body),
    auth: false,
  })
  setToken(res.token)
  return res
}

export async function getMe(): Promise<UserResponse> {
  if (isMockMode()) return mockUser
  return apiRequest<UserResponse>('/api/auth/me')
}

export async function verifyEmail(body: EmailVerifyRequest): Promise<MessageResponse> {
  if (isMockMode()) return { message: 'Email verified successfully' }
  return apiRequest<MessageResponse>('/api/auth/verify-email', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export async function verifyPhone(body: PhoneVerifyRequest): Promise<MessageResponse> {
  if (isMockMode()) return { message: 'Phone verified successfully' }
  return apiRequest<MessageResponse>('/api/auth/verify-phone', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export async function generateEmailOtp(): Promise<MessageResponse> {
  if (isMockMode()) return { message: 'Email OTP generated successfully' }
  return apiRequest<MessageResponse>('/api/auth/generate-email-otp', { method: 'POST' })
}

export async function generatePhoneOtp(): Promise<MessageResponse> {
  if (isMockMode()) return { message: 'Phone OTP generated successfully' }
  return apiRequest<MessageResponse>('/api/auth/generate-phone-otp', { method: 'POST' })
}

export function logout() {
  setToken(null)
}
