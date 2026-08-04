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
        {/* <ElectricCategoryCard2 item={lastCard} /> */}
        
      </div>
    </div>
  )
}

export default ElectricCategory


// import React, { useEffect, useMemo, useState } from "react";
// import { useAppSelector } from "../../../../State/store";
// import FlashHour from "../ShopByCategory/FlashHour";
// import ElectricCategoryCard2 from "./ElectricCategoryCard2";

// const ROTATE_INTERVAL = 3000;

// const ElectricCategory = () => {
//   const { customer } = useAppSelector((store) => store);

//   // Backend data
//   const items = customer.homePageData?.electricCategories ?? [];

//   // Split into:
//   // [FlashHour] [Featured Deals...] [Promo Banner]
//   const firstCard = items[0];
//   const lastCard = items[items.length - 1];
//   const middleCards = items.slice(1, -1);

//   /**
//    * Group featured deals into pages of 2
//    *
//    * Example:
//    * [A,B,C,D,E,F]
//    *
//    * =>
//    *
//    * [
//    *   [A,B],
//    *   [C,D],
//    *   [E,F]
//    * ]
//    */
//   const pages = useMemo(() => {
//     const grouped = [];

//     for (let i = 0; i < middleCards.length; i += 2) {
//       grouped.push(middleCards.slice(i, i + 2));
//     }

//     return grouped;
//   }, [middleCards]);

//   const [currentPage, setCurrentPage] = useState(0);

//   useEffect(() => {
//     if (pages.length <= 1) return;

//     const timer = setInterval(() => {
//       setCurrentPage((prev) => (prev + 1) % pages.length);
//     }, ROTATE_INTERVAL);

//     return () => clearInterval(timer);
//   }, [pages.length]);

//   // Loading
//   if (!customer.homePageData) {
//     return (
//       <div className="py-10 text-center text-gray-400">
//         Loading...
//       </div>
//     );
//   }

//   // Need at least:
//   // FlashHour + 2 Featured + Promo
//   if (items.length < 4) {
//     return null;
//   }

//   return (
//     <section className="flex items-stretch gap-3 px-2 py-2">

//       {/* Flash Hour */}

//       <div className="w-[380px] flex-shrink-0">
//         <FlashHour item={firstCard} />
//       </div>

//       {/* Featured Deals */}

//       <div className="flex-1 overflow-hidden">

//         <div
//           key={currentPage}
//           className="grid grid-cols-2 gap-3 animate-fade"
//         >
//           {pages[currentPage]?.map((item) => (
//             <ElectricCategoryCard2
//               key={item.id}
//               item={item}
//             />
//           ))}
//         </div>

//       </div>

//       {/* Promo Banner */}

//       <div className="w-[380px] flex-shrink-0">
//         <ElectricCategoryCard2 item={lastCard} />
//       </div>

//     </section>
//   );
// };

// export default ElectricCategory;