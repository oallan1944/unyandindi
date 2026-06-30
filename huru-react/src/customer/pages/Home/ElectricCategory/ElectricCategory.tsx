import React from 'react'
// import ElectricCategoryCard from './ElectricCategoryCard'
import { useAppSelector } from '../../../../State/store'
import Slider from 'react-slick';
import ElectricCategoryCard2 from './ElectricCategoryCard2';

const ElectricCategory = () => {
  const settings = {
    dots: false,
    infinite: true,
    speed: 500,
    slidesToShow: 4,
    slidesToScroll: 1,
    autoplay: true,
    autoplaySpeed: 3000,

     arrows: false, // optional
    centerMode: false,
   // centerMode: true,
    centerPadding: '30px',
    responsive: [
      {
        breakpoint: 1024,
        settings: {
          slidesToShow: 2,
          slidesToScroll: 1,
          infinite: true,
          dots: true,
          centerMode: true,
          centerPadding: '20px'
        }
      },
      {
        breakpoint: 640,
        settings: {
          slidesToShow: 1,
          slidesToScroll: 1,
          centerMode: true,
          centerPadding: '15px'
        }
      }
    ]
  };

  
  const { customer } = useAppSelector(store => store);

  console.log(customer.homePageData?.electricCategories)
  return (
    <div className='py-0.5'>
      <Slider {...settings}>
        {customer.homePageData?.electricCategories.map((item, index) => (
          <div key={index} className='px-0.5'>
            {/* <ElectricCategoryCard
              item={item}
            /> */}

            <ElectricCategoryCard2
              item={item}
            />
          </div>

        ))}

      </Slider>


    </div>
  )
  
}

export default ElectricCategory
