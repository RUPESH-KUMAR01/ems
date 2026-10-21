import { failPayment, verifyPayment } from '@/api/payments'
import type { CreateOrderResponse } from '@/api/types'
import { useCallback, useRef, useState } from 'react'

const SCRIPT_URL = 'https://checkout.razorpay.com/v1/checkout.js'

function isMockOrder(order: CreateOrderResponse): boolean {
  return (
    order.razorpayKeyId === 'rzp_test_mock' || order.razorpayOrderId.startsWith('order_mock_')
  )
}

function loadRazorpayScript(): Promise<void> {
  if (window.Razorpay) return Promise.resolve()
  return new Promise((resolve, reject) => {
    const existing = document.querySelector(`script[src="${SCRIPT_URL}"]`)
    if (existing) {
      existing.addEventListener('load', () => resolve())
      return
    }
    const script = document.createElement('script')
    script.src = SCRIPT_URL
    script.onload = () => resolve()
    script.onerror = () => reject(new Error('Failed to load Razorpay'))
    document.body.appendChild(script)
  })
}

export function useRazorpayCheckout() {
  const [busy, setBusy] = useState(false)
  const openingRef = useRef(false)

  const openCheckout = useCallback(
    async (
      order: CreateOrderResponse,
      opts: {
        name?: string
        email?: string
        onSuccess: () => void
        onFailure: () => void
      },
    ) => {
      if (openingRef.current || busy) return
      openingRef.current = true
      setBusy(true)
      try {
        if (isMockOrder(order)) {
          await verifyPayment({
            razorpayOrderId: order.razorpayOrderId,
            razorpayPaymentId: `pay_mock_${Date.now()}`,
            razorpaySignature: 'mock',
          })
          opts.onSuccess()
          return
        }

        await loadRazorpayScript()
        const amountPaise = Math.round(Number(order.amount) * 100)
        const rzp = new window.Razorpay({
          key: order.razorpayKeyId,
          amount: amountPaise,
          currency: order.currency,
          name: 'EMS',
          description: 'Event registration',
          order_id: order.razorpayOrderId,
          prefill: { name: opts.name, email: opts.email },
          handler: async (response) => {
            await verifyPayment({
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            })
            opts.onSuccess()
          },
          modal: {
            ondismiss: async () => {
              await failPayment(order.razorpayOrderId)
              opts.onFailure()
            },
          },
        })
        rzp.open()
      } finally {
        setBusy(false)
        openingRef.current = false
      }
    },
    [busy],
  )

  return { openCheckout, busy }
}
