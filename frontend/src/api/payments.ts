import { ApiError, apiRequest, isMockMode } from './client'
import type {
  CreateOrderRequest,
  CreateOrderResponse,
  PaymentResponse,
  VerifyPaymentRequest,
} from './types'

export async function createOrder(body: CreateOrderRequest): Promise<CreateOrderResponse> {
  if (isMockMode()) {
    return {
      paymentId: 1,
      razorpayOrderId: 'order_mock',
      amount: 499,
      currency: 'INR',
      razorpayKeyId: 'rzp_test_mock',
    }
  }
  return apiRequest<CreateOrderResponse>('/api/payments/orders', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export async function verifyPayment(body: VerifyPaymentRequest): Promise<PaymentResponse> {
  if (isMockMode()) {
    return {
      id: 1,
      registrationId: 10,
      amount: 499,
      status: 'COMPLETED',
      providerOrderId: body.razorpayOrderId,
      providerPaymentId: body.razorpayPaymentId,
      createdAt: new Date().toISOString(),
      paidAt: new Date().toISOString(),
    }
  }
  return apiRequest<PaymentResponse>('/api/payments/verify', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export async function failPayment(razorpayOrderId: string): Promise<PaymentResponse> {
  if (isMockMode()) {
    return {
      id: 1,
      registrationId: 10,
      amount: 499,
      status: 'FAILED',
      providerOrderId: razorpayOrderId,
      providerPaymentId: null,
      createdAt: new Date().toISOString(),
      paidAt: null,
    }
  }
  return apiRequest<PaymentResponse>(`/api/payments/fail/${razorpayOrderId}`, {
    method: 'POST',
  })
}

export async function getPaymentByRegistration(
  registrationId: number,
): Promise<PaymentResponse | null> {
  if (isMockMode()) return null
  try {
    return await apiRequest<PaymentResponse>(`/api/payments/registration/${registrationId}`)
  } catch (e) {
    if (e instanceof ApiError && e.status === 404) return null
    throw e
  }
}
