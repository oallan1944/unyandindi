import { Divider } from '@mui/material'
import { useMemo } from 'react'
import { useAppSelector } from '../../../State/store'
import { formatUGX, roundUGX } from '../../../Util/currency'

/**
 * TODO: source this from the cart/order API once shipping is priced
 * server-side (e.g. by seller location or order weight). A shipping fee
 * hardcoded here is a display-only number — if it doesn't match what the
 * backend actually charges at checkout, the customer sees one total and
 * gets billed another, which is a trust problem, not just a cosmetic one.
 */
const FALLBACK_SHIPPING_FEE_UGX = 5000

interface PricingBreakdown {
  subtotal: number
  discount: number
  shipping: number
  total: number
}

const PricingCard = () => {
  const { cart } = useAppSelector((state) => state.cart)

  const breakdown: PricingBreakdown = useMemo(() => {
    const mrp = typeof cart?.totalMrpPrice === 'number' ? cart.totalMrpPrice : 0
    const selling = typeof cart?.totalSellingPrice === 'number' ? cart.totalSellingPrice : 0

    // Clamp to 0: a discount should never render as negative, even if
    // upstream data is inconsistent (e.g. a broken promo pushes selling
    // price above MRP). Showing "-UGX -3,000" to a customer looks broken
    // and erodes trust in the whole checkout flow.
    const discount = roundUGX(Math.max(0, mrp - selling))
    const shipping = FALLBACK_SHIPPING_FEE_UGX
    const total = roundUGX(Math.max(0, selling + shipping))

    return { subtotal: roundUGX(mrp), discount, shipping, total }
  }, [cart])

  return (
    <>
      <div className="space-y-3 p-5">
        <div className="flex justify-between items-center">
          <span>Subtotal</span>
          <span>{formatUGX(breakdown.subtotal)}</span>
        </div>
        <div className="flex justify-between items-center">
          <span>Discount</span>
          <span>{breakdown.discount > 0 ? `- ${formatUGX(breakdown.discount)}` : formatUGX(0)}</span>
        </div>
        <div className="flex justify-between items-center">
          <span>Shipping</span>
          <span>{formatUGX(breakdown.shipping)}</span>
        </div>
        <div className="flex justify-between items-center">
          <span>Platform Fee</span>
          <span>Free</span>
        </div>
      </div>
      <Divider />
      <div className="flex justify-between items-center p-5 text-primary-color" aria-live="polite">
        <span>Total</span>
        <span>{formatUGX(breakdown.total)}</span>
      </div>
    </>
  )
}

export default PricingCard
