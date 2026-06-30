import React from 'react'
import DealCard from './DealCard'
import "slick-carousel/slick/slick.css";
import "slick-carousel/slick/slick-theme.css";
import { useAppSelector } from '../../../../State/store';
import Slider from "react-slick";

const Deal = () => {
    const settings = {
        dots: true,
        infinite: true,
        speed: 500,
        slidesToShow: 6,
        slidesToScroll: 1,
        autoplay: true,
        autoplaySpeed: 3000,
        responsive: [
            {
                breakpoint: 1024,
                settings: {
                    slidesToShow: 2,
                    slidesToScroll: 1,
                    infinite: true,
                    dots: true
                }
            },
            {
                breakpoint: 640,
                settings: {
                    slidesToShow: 1,
                    slidesToScroll: 1
                }
            }
        ]
    };

    const { customer } = useAppSelector(store => store);

     // ✅ guard — don't render slider until deals are loaded
    if (!customer.homePageData?.deals?.length) {
        return (
            <div className="py-8 text-center text-gray-400">
                Loading deals...
            </div>
        )
    }

    return (
        <div className='py-2 lg:px-2 bg-charcoal'>
            {/* <div className='flex items-center justify-between mb-4'>
                <h2 className='text-xl font-bold'>Deals of the Day</h2>
            </div> */}

            <Slider {...settings}>
                {customer.homePageData?.deals.slice(0, 9).map((item, index) => (
                    <div key={index} className='px-1'>
                        <DealCard item={item} />
                    </div>
                ))}
            </Slider>
        </div>
    )
}

export default Deal
