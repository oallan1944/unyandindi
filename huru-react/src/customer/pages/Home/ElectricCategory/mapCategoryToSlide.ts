import { HomeCategory } from '../../../../types/HomeCategoryType'
import { HeroSlideData, HeroTagVariant } from './HeroSection.types'

const TAG_VARIANT_CYCLE: HeroTagVariant[] = ['flash', 'new', 'info']

/**
 * Converts a HomeCategory (electricCategories from the API/store) into the
 * HeroSlideData shape HeroSlide already knows how to render. Mirrors the
 * mapPromotionToSlide()/mapFlashSaleToSlide() pattern anticipated in
 * HeroSection.types.ts — this is the "categories" version of that mapping.
 */
export const mapCategoryToSlide = (item: HomeCategory, index: number): HeroSlideData => {
  const productCount = item.products?.length ?? 0

  return {
    id: String(item.id ?? item.categoryId ?? index),
    tag: item.section ? item.section : 'Featured category',
    tagVariant: TAG_VARIANT_CYCLE[index % TAG_VARIANT_CYCLE.length],
    title: item.name ?? 'Explore this category',
    subtitle: productCount > 0
      ? `${productCount} product${productCount === 1 ? '' : 's'} from sellers across the platform.`
      : 'Fresh picks from sellers across the platform.',
    ctaLabel: 'Shop now',
    ctaLink: `/category/${item.categoryId}`,
    image: item.image ?? '/assets/electricAssets/banner-1.jpg',
  }
}