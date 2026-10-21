export type Role = 'USER' | 'MODERATOR' | 'ADMIN'

export type EventStatus = 'DRAFT' | 'PUBLISHED' | 'COMPLETED' | 'CANCELLED'
export type EventType = 'SOLO' | 'TEAM'
export type EventVisibility = 'PRIVATE' | 'PUBLIC'
export type RegistrationStatus = 'PENDING' | 'REGISTERED' | 'CANCELLED'
export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'CANCELLED'
export type RequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'

export interface ApiErrorResponse {
  timestamp: string
  status: number
  error: string
  code: string
  message: string
  path: string
  details?: string[]
}

export interface MessageResponse {
  message: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface CreateUserRequest {
  name: string
  email: string
  password: string
  phone: string
}

export interface EmailVerifyRequest {
  otp: string
}

export interface PhoneVerifyRequest {
  otp: string
}

export interface LoginResponse {
  token: string
}

export interface RegisterResponse {
  token: string
}

export interface UserResponse {
  id: number
  email: string
  name: string
  role: Role
  emailVerified: boolean
  phoneVerified: boolean
}

export interface EventResponse {
  id: number
  name: string
  description: string
  createdBy: number
  type: EventType
  visibility: EventVisibility
  status: EventStatus
  maxParticipants: number
  minTeamSize: number | null
  maxTeamSize: number | null
  registrationFee: number
  registrationDeadline: string
  createdAt: string
  updatedAt: string
}

export interface CreateEventRequest {
  name: string
  description: string
  type: EventType
  visibility?: EventVisibility
  maxParticipants: number
  minTeamSize?: number
  maxTeamSize?: number
  registrationFee?: number
  registrationDeadline: string
  startTime?: string
  endTime?: string
}

export interface UpdateEventRequest {
  name?: string
  description?: string
  type?: EventType
  visibility?: EventVisibility
  status?: EventStatus
  maxParticipants?: number
  minTeamSize?: number
  maxTeamSize?: number
  registrationFee?: number
  registrationDeadline?: string
  startTime?: string
  endTime?: string
}

export interface RegisterEventRequest {
  teamId?: number | null
}

export interface EventRegistrationResponse {
  id: number
  eventId: number
  userId: number
  teamId: number | null
  status: RegistrationStatus
  registeredAt: string
}

export interface CreateOrderRequest {
  registrationId: number
}

export interface VerifyPaymentRequest {
  razorpayOrderId: string
  razorpayPaymentId: string
  razorpaySignature: string
}

export interface CreateOrderResponse {
  paymentId: number
  razorpayOrderId: string
  amount: number
  currency: string
  razorpayKeyId: string
}

export interface PaymentResponse {
  id: number
  registrationId: number
  amount: number
  status: PaymentStatus
  providerOrderId: string
  providerPaymentId: string | null
  createdAt: string
  paidAt: string | null
}

export interface CreateTeamRequest {
  name: string
}

export interface UpdateTeamRequest {
  name?: string
}

export interface InviteUserRequest {
  email: string
}

export interface RespondToRequestRequest {
  status: 'APPROVED' | 'REJECTED'
}

export interface TeamResponse {
  id: number
  name: string
  ownerId: number
  eventId: number
  createdAt: string
  updatedAt: string
}

export type TeamMembershipRequestType = 'INVITATION' | 'JOIN_REQUEST'
export type TeamMembershipStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export interface TeamMembershipResponse {
  id: number
  teamId: number
  userId: number
  eventId?: number
  userName?: string | null
  userEmail?: string | null
  /** @deprecated use status */
  type: string
  status: TeamMembershipStatus
  requestType: TeamMembershipRequestType
}

export interface CreateManagedUserRequest {
  name: string
  email: string
  password?: string
  role: Role
  phone: string
}

export interface UpdateUserRequest {
  name?: string
  email?: string
  phone?: string
  emailVerified?: boolean
  phoneVerified?: boolean
  role?: Role
}

export interface ChangeUserRoleRequest {
  role: Role
}

export interface AdminResetPasswordRequest {
  newPassword: string
}

export interface AdminUpdateVerificationRequest {
  emailVerified: boolean
  phoneVerified: boolean
}

export interface AdminForceRegisterRequest {
  eventId: number
  userId: number
  teamId?: number
}

export interface AdminUpdateEventStatusRequest {
  status: EventStatus
}

export interface AdminUpdateRegistrationStatusRequest {
  status: RegistrationStatus
}

export interface AdminUpdatePaymentStatusRequest {
  status: PaymentStatus
}

export interface AdminMessageResponse {
  message: string
}

export interface SystemStatsResponse {
  totalUsers: number
  usersByRole: Record<string, number>
  totalEvents: number
  eventsByStatus: Record<string, number>
  totalTeams: number
  totalRegistrations: number
  registrationsByStatus: Record<string, number>
  totalPayments: number
  paymentsByStatus: Record<string, number>
  totalRevenue: number
}
