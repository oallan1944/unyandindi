export const CATEGORY_IMAGE_FALLBACK = "/assets/placeholders/category-placeholder.png";

/**
 * Attached to an <img>'s onError handler. Swaps in a local fallback if the
 * given src is missing, empty, or fails to load (Cloudinary URL 404s, etc).
 * Guards against an infinite onError loop if the fallback itself is broken.
 */
export const handleImageError = (event: React.SyntheticEvent<HTMLImageElement>) => {
    const img = event.currentTarget;
    if (img.src.endsWith(CATEGORY_IMAGE_FALLBACK)) return;
    img.src = CATEGORY_IMAGE_FALLBACK;
};