import { useEffect, useRef, useState } from 'react';
import Slider from 'react-slick'
import { IconButton } from '@mui/material'
import ArrowBackIosNewIcon from '@mui/icons-material/ArrowBackIosNew'
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos'
// import { useAppSelector } from '../../../State/store'
import HeroSlide from './HeroSlide'
import { heroSlidesFallback } from './heroSlides.data'
import { HeroSlideData } from './HeroSection.types'
import './heroSlider.css'

const AUTOPLAY_SPEED = 5000

const HeroSlider = () => {
  const sliderRef = useRef<Slider>(null)
  const [autoplay, setAutoplay] = useState(true)

  // Static for now. Once /api/promotions/active + /api/flash-sales/active
  // are wired up, this becomes the same pattern ElectricCategory1 uses:
  //
  // const { customer } = useAppSelector((store) => store)
  // const slides: HeroSlideData[] = customer.homePageData?.heroSlides?.length
  //   ? customer.homePageData.heroSlides
  //   : heroSlidesFallback
  const slides: HeroSlideData[] = heroSlidesFallback

  useEffect(() => {
    const query = window.matchMedia('(prefers-reduced-motion: reduce)')
    setAutoplay(!query.matches)
  }, [])

  if (!slides.length) return null

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
      aria-label="Featured promotions"
    >
      <Slider ref={sliderRef} {...settings}>
        {slides.map((slide) => (
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

export default HeroSlider
