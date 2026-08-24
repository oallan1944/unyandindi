import { useEffect, useRef, useState } from 'react'
import Slider from 'react-slick'
import { IconButton } from '@mui/material'
import ArrowBackIosNewIcon from '@mui/icons-material/ArrowBackIosNew'
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos'
import { useAppSelector } from '../../../../State/store'
import { mapCategoryToSlide } from './mapCategoryToSlide'
import HeroSlide from './HeroSlide'
import './heroSlider.css'


const AUTOPLAY_SPEED = 5000

const ElectricCategory = () => {
  const { customer } = useAppSelector(store => store)
  const sliderRef = useRef<Slider>(null)
  const [autoplay, setAutoplay] = useState(true)

  useEffect(() => {
    const query = window.matchMedia('(prefers-reduced-motion: reduce)')
    setAutoplay(!query.matches)
  }, [])

  // ✅ show loading state while data is being fetched
  if (!customer.homePageData) {
    return (
      <div className="py-8 text-center text-gray-400">
        Loading...
      </div>
    )
  }

  const items = customer.homePageData?.electricCategories || []

  if (items.length < 3) {
    return null // must have at least 3 to render properly
  }

  const slides = items.map(mapCategoryToSlide)

  const settings = {
    dots: true,
    infinite: true,
    speed: 600,
    slidesToShow: 1,
    slidesToScroll: 1,
    autoplay,
    autoplaySpeed: AUTOPLAY_SPEED,
    pauseOnHover: true,
    pauseOnFocus: true,
    arrows: false,
    dotsClass: 'slick-dots hero-dots',
  }

  return (
    <section
      className="relative w-full overflow-hidden rounded-2xl"
      aria-roledescription="carousel"
      aria-label="Featured categories"
    >
      <Slider ref={sliderRef} {...settings}>
        {slides.map(slide => (
          <HeroSlide key={slide.id} slide={slide} />
        ))}
      </Slider>

      <IconButton
        aria-label="Previous slide"
        onClick={() => sliderRef.current?.slickPrev()}
        size="small"
        className="!absolute !left-3 !top-1/2 !z-10 !-translate-y-1/2 !bg-black/35 !text-white hover:!bg-black/55"
      >
        <ArrowBackIosNewIcon fontSize="small" />
      </IconButton>
      <IconButton
        aria-label="Next slide"
        onClick={() => sliderRef.current?.slickNext()}
        size="small"
        className="!absolute !right-3 !top-1/2 !z-10 !-translate-y-1/2 !bg-black/35 !text-white hover:!bg-black/55"
      >
        <ArrowForwardIosIcon fontSize="small" />
      </IconButton>
    </section>
  )
}

export default ElectricCategory