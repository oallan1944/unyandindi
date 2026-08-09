import { useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAppSelector } from '../../../../State/store'
import FlashSale from './FlashSale'
import Slider from 'react-slick'
import { ArrowLeft, ArrowRight } from 'lucide-react'
import { HomeCategory } from '../../../../types/HomeCategoryType'

const NextArrow = (props: any) => {
  const { onClick } = props
  return (
    <button
      onClick={onClick}
      aria-label="Next category"
      className="absolute z-10 right-0 top-1/2 transform -translate-y-1/2 bg-primary-color p-2 rounded-full shadow-md hover:bg-primary-color/90"
    >
      <ArrowRight className="text-white w-5 h-5" />
    </button>
  )
}

const PrevArrow = (props: any) => {
  const { onClick } = props
  return (
    <button
      onClick={onClick}
      aria-label="Previous category"
      className="absolute z-10 left-0 top-1/2 transform -translate-y-1/2 bg-primary-color p-2 rounded-full shadow-md hover:bg-primary-color/90"
    >
      <ArrowLeft className="text-white w-5 h-5" />
    </button>
  )
}

const ShopByCategory = () => {
  const { customer } = useAppSelector(store => store)
  const navigate = useNavigate()

  const settings = {
    dots: false,
    infinite: true,
    speed: 500,
    slidesToShow: 4,
    slidesToScroll: 1,
    autoplay: true,
    autoplaySpeed: 3000,
    pauseOnHover: true,
    centerMode: true,
    centerPadding: '40px',
    nextArrow: <NextArrow />,
    prevArrow: <PrevArrow />,
    responsive: [
      { breakpoint: 1024, settings: { slidesToShow: 3, centerPadding: '30px' } },
      { breakpoint: 640, settings: { slidesToShow: 1.3, centerPadding: '20px' } },
    ],
  }

  // Single source of truth for how a category maps to its product-listing route.
  // Validates the id server-side data gave us before trusting it in a URL.
  const handleCategorySelect = useCallback(
    (item: HomeCategory) => {
      const categoryId = (item as any)?.id ?? (item as any)?.categoryId

      if (!categoryId || typeof categoryId !== 'string' && typeof categoryId !== 'number') {
        // Malformed backend data — fail closed, don't navigate to a broken/unsafe route.
        console.error('ShopByCategory: missing or invalid category id for item', item)
        return
      }

      navigate(`/products/category/${encodeURIComponent(String(categoryId))}`)
    },
    [navigate]
  )

  if (!customer.homePageData?.shopByCategories?.length) {
    return (
      <div className="py-8 text-center text-gray-400">
        Loading categories...
      </div>
    )
  }

  return (
    <div className="relative py-2">
      <Slider {...settings}>
        {customer.homePageData.shopByCategories.map((item, index) => (
          <div key={(item as any).id ?? index} className="px-4">
            <FlashSale item={item} onSelect={handleCategorySelect} />
          </div>
        ))}
      </Slider>
    </div>
  )
}

export default ShopByCategory

// // import React from 'react'
// import { useAppSelector } from '../../../../State/store'
// import FlashSale from './FlashSale'
// import Slider from 'react-slick'
// import { ArrowLeft, ArrowRight } from 'lucide-react'

// const NextArrow = (props: any) => {
//   const { onClick } = props
//   return (
//     <button
//       onClick={onClick}
//       className="absolute z-10 right-0 top-1/2 transform -translate-y-1/2 bg-primary-color p-2 rounded-full shadow-md hover:bg-primary-color/90"
//     >
//       <ArrowRight className="text-white w-5 h-5" />
//     </button>
//   )
// }

// const PrevArrow = (props: any) => {
//   const { onClick } = props
//   return (
//     <button
//       onClick={onClick}
//       className="absolute z-10 left-0 top-1/2 transform -translate-y-1/2 bg-primary-color p-2 rounded-full shadow-md hover:bg-primary-color/90"
//     >
//       <ArrowLeft className="text-white w-5 h-5" />
//     </button>
//   )
// }

// const ShopByCategory = () => {
//   const { customer } = useAppSelector(store => store)

//   const settings = {
//     dots: false,
//     infinite: true,
//     speed: 500,
//     slidesToShow: 4,
//     slidesToScroll: 1,
//     autoplay: true,
//     autoplaySpeed: 3000,
//     pauseOnHover: true,
//     centerMode: true,
//     centerPadding: '40px',
//     nextArrow: <NextArrow />,
//     prevArrow: <PrevArrow />,
//     responsive: [
//       {
//         breakpoint: 1024,
//         settings: { slidesToShow: 3, centerPadding: '30px' },
//       },
//       {
//         breakpoint: 640,
//         settings: { slidesToShow: 1.3, centerPadding: '20px' },
//       },
//     ],
//   }

//   // ✅ don't render slider until data is loaded
//   if (!customer.homePageData?.shopByCategories?.length) {
//     return (
//       <div className="py-8 text-center text-gray-400">
//         Loading categories...
//       </div>
//     )
//   }

//   return (
//     <div className="relative py-2">
//       <Slider {...settings}>
//         {customer.homePageData.shopByCategories.map((item, index) => (
//           <div key={index} className="px-4">
//             <FlashSale item={item} />
//           </div>
//         ))}
//       </Slider>
//     </div>
//   )
// }

// export default ShopByCategory