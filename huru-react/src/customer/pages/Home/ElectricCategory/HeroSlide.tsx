import { Button } from '@mui/material'
import { HeroSlideData } from './HeroSection.types'

const tagStyles: Record<HeroSlideData['tagVariant'], string> = {
  flash: 'bg-orange-600 text-white',
  new: 'bg-emerald-600 text-white',
  info: 'bg-amber-400 text-gray-900',
}

const HeroSlide = ({ slide }: { slide: HeroSlideData }) => {
  return (
    <div className="relative h-[280px] md:h-[380px] w-full outline-none">
      <img src={slide.image} alt="" className="absolute inset-0 h-full w-full object-cover" />
      <div className="absolute inset-0 bg-gradient-to-r from-black/85 via-black/50 to-transparent" />

      <div className="relative z-10 flex h-full max-w-xl flex-col justify-end gap-3 p-6 md:p-10">
        <span
          className={`w-fit rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-wide ${tagStyles[slide.tagVariant]}`}
        >
          {slide.tag}
        </span>

        <h1 className="text-2xl font-bold leading-tight text-white md:text-4xl">{slide.title}</h1>

        <p className="max-w-md text-sm text-gray-200 md:text-base">{slide.subtitle}</p>

        <Button
          variant="contained"
          href={slide.ctaLink}
          sx={{
            width: 'fit-content',
            textTransform: 'none',
            fontWeight: 600,
            borderRadius: '999px',
            bgcolor: 'white',
            color: '#111827',
            '&:hover': { bgcolor: '#F2A93B' },
          }}
        >
          {slide.ctaLabel}
        </Button>
      </div>
    </div>
  )
}

export default HeroSlide
