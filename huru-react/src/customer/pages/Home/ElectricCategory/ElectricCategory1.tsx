import React from 'react'
import { useAppSelector } from '../../../../State/store'
import Slider from 'react-slick'
import ElectricCategoryCard2 from './ElectricCategoryCard2'
import FlashHour from '../ShopByCategory/FlashHour'

const ElectricCategory = () => {
  const { customer } = useAppSelector(store => store)

  const settings = {
    dots: false,
    infinite: true,
    speed: 500,
    slidesToShow: 2,
    slidesToScroll: 1,
    autoplay: true,
    autoplaySpeed: 3000,
    arrows: false,
    centerMode: false,
    responsive: [
      {
        breakpoint: 1024,
        settings: {
          slidesToShow: 1,
        },
      },
    ],
  }

  const items = customer.homePageData?.electricCategories || []

   // ✅ show loading state while data is being fetched
    if (!customer.homePageData) {
        return (
            <div className="py-8 text-center text-gray-400">
                Loading...
            </div>
        )
    }

  if (items.length < 3) {
    return null // must have at least 3 to render properly
  }

  const firstCard = items[0]
  const middleCards = items.slice(1, items.length - 1)
  const lastCard = items[items.length - 1]

  return (
    <div className="flex items-stretch gap-1 overflow-hidden py-2 px-2">
      {/* Left static card */}
      <div className="w-[400px]">
        <FlashHour item={firstCard} />
      </div>

      {/* Middle slider */}
      <div className="flex-1 overflow-hidden">
        <Slider {...settings}>
          {middleCards.map((item, index) => (
            <div key={index} className="px-2 h-full">
              <ElectricCategoryCard2 item={item} />
            </div>
          ))}
        </Slider>
      </div>

      {/* Right static card */}
      <div className="w-[400px]">
        <ElectricCategoryCard2 item={lastCard} />
      </div>
    </div>
  )
}

export default ElectricCategory
