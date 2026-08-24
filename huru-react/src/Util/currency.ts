/**
 * Shared currency formatting for the storefront.
 *
 * Centralized here so every price-displaying component (cart, checkout,
 * product cards, order history, seller payouts, admin deal/promotion
 * screens) formats Uganda Shillings identically — same symbol placement,
 * same rounding, same handling of bad/missing data. A component that
 * builds its own "UGX " + value.toLocaleString() string is how you end
 * up with three slightly different-looking prices across the app.
 */

const ugxFormatter = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'UGX',
  // UGX has no minor unit in everyday use — cents/coins aren't part of
  // normal pricing, so showing ".00" on every price is just noise.
  minimumFractionDigits: 0,
  maximumFractionDigits: 0,
});

/**
 * Formats a numeric amount as Uganda Shillings, e.g. formatUGX(45000) -> "UGX 45,000".
 * Defensively falls back to UGX 0 for NaN/undefined/Infinity rather than
 * rendering "UGX NaN" or a blank price to the customer.
 */
export function formatUGX(amount: number | null | undefined): string {
  const safeAmount = typeof amount === 'number' && Number.isFinite(amount) ? amount : 0;
  return ugxFormatter.format(safeAmount);
}

/**
 * Rounds a monetary amount to the nearest whole shilling. Use this before
 * storing or comparing derived amounts (e.g. discount = mrp - selling) so
 * floating-point arithmetic never produces fractional shillings that then
 * get silently truncated by formatUGX and look inconsistent to the user.
 */
export function roundUGX(amount: number): number {
  return Number.isFinite(amount) ? Math.round(amount) : 0;
}