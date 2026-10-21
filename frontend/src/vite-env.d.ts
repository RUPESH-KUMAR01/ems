/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_URL: string
  readonly VITE_MOCK: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

interface RazorpayHandlerResponse {
  razorpay_payment_id: string
  razorpay_order_id: string
  razorpay_signature: string
}

interface RazorpayOptions {
  key: string
  amount: number
  currency: string
  name?: string
  description?: string
  order_id: string
  handler: (response: RazorpayHandlerResponse) => void
  modal?: {
    ondismiss?: () => void
  }
  prefill?: { email?: string; name?: string }
}

interface RazorpayInstance {
  open: () => void
  on: (event: string, handler: (response: RazorpayHandlerResponse) => void) => void
}

interface Window {
  Razorpay: new (options: RazorpayOptions) => RazorpayInstance
}
