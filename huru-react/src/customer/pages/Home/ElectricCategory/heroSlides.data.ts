import { HeroSlideData } from './HeroSection.types'

/**
 * Static for now, using real uploaded assets. Swap this whole file out once
 * /api/promotions/active + /api/flash-sales/active exist — HeroSlider will
 * read from redux instead (see the commented block in HeroSlider.tsx). The
 * shape below (id/tag/tagVariant/title/subtitle/ctaLabel/ctaLink/image) is
 * exactly what the mapped API response should produce, so nothing else in
 * HeroSlider.tsx or HeroSlide.tsx needs to change when that happens.
 *
 * Place the source images at public/assets/hero/ using the filenames below.
 */
export const heroSlidesFallback: HeroSlideData[] = [
  {
    id: 'new-season-editorial',
    tag: 'New arrivals',
    tagVariant: 'new',
    title: 'The new season edit is here',
    subtitle: 'Statement outerwear and tailored pieces from sellers across the platform.',
    ctaLabel: 'Shop new arrivals',
    ctaLink: '/category/fashion?promo=new-season',
    image: '/assets/electricAssets/banner-1.jpg',
  },
  {
    id: 'summer-accessories',
    tag: 'Trending now',
    tagVariant: 'info',
    title: 'Sunglasses, totes and everything summer',
    subtitle: 'Bags and eyewear picked from top-rated sellers this week.',
    ctaLabel: 'Browse accessories',
    ctaLink: '/category/accessories?promo=summer',
    image: '/assets/electricAssets/banner-bg.jpg',
  },
  {
    id: 'flash-sale-sitewide',
    tag: '⚡ Flash sale · limited time',
    tagVariant: 'flash',
    title: 'Flash deals across every category',
    subtitle: 'Prices drop for a few hours only — check back often to catch the window.',
    ctaLabel: 'View flash deals',
    ctaLink: '/promotions/flash',
    image: '/assets/electricAssets/banner-img.jpg',
  },
  {
    id: 'sneaker-week',
    tag: 'Footwear deal',
    tagVariant: 'flash',
    title: 'Up to 30% off sneakers this week',
    subtitle: 'Basketball and lifestyle sneakers from verified sellers.',
    ctaLabel: 'Shop sneakers',
    ctaLink: '/category/footwear?promo=sneaker-week',
    image: '/assets/electricAssets/banner-img.png',
  },
  {
    id: 'flash-electronics',
    tag: '⚡ Flash sale · 6 hrs left',
    tagVariant: 'flash',
    title: 'Drones and gadgets, today only',
    subtitle: 'Cameras, drones and accessories from verified electronics sellers.',
    ctaLabel: 'Shop electronics',
    ctaLink: '/category/electronics?promo=flash-electronics',
    image: '/assets/electricAssets/product-banner-3.jpg',
  },
  {
    id: 'knitwear-collection',
    tag: 'New on the platform',
    tagVariant: 'new',
    title: 'Cozy knitwear, just landed',
    subtitle: 'Sweaters and layers from sellers ready to ship this week.',
    ctaLabel: 'Shop knitwear',
    ctaLink: '/category/fashion/knitwear',
    image: '/assets/electricAssets/slider_1.jpg',
  },
]