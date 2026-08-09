export type HeroTagVariant = 'flash' | 'new' | 'info'

export interface HeroSlideData {
  id: string
  tag: string
  tagVariant: HeroTagVariant
  title: string
  subtitle: string
  ctaLabel: string
  ctaLink: string
  image: string
}

/**
 * Reference shape only — not consumed yet.
 * Once /api/promotions/active and /api/flash-sales/active are wired up,
 * write a mapPromotionToSlide()/mapFlashSaleToSlide() that turns this
 * into HeroSlideData, the same way electricCategories gets mapped today.
 */
export interface PromotionApiResponse {
  id: number
  title: string
  description: string
  discountLabel: string
  bannerImageUrl: string
  ctaUrl: string
  startsAt: string
  endsAt: string
}

export interface FlashSaleApiResponse {
  id: number
  productName: string
  discountPercent: number
  bannerImageUrl: string
  endsAt: string
}
