import { HomeCategory } from '../../../../types/HomeCategoryType'

interface FlashSaleProps {
  item: HomeCategory
  onSelect?: (item: HomeCategory) => void
}

const FlashSale = ({ item, onSelect }: FlashSaleProps) => {
  const isInteractive = typeof onSelect === 'function'

  const handleActivate = () => {
    if (isInteractive) onSelect(item)
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault()
      handleActivate()
    }
  }

  return (
    <div
      className="flex flex-col items-center group cursor-pointer gap-2 justify-center outline-none focus-visible:ring-2 focus-visible:ring-primary-color rounded-md"
      role={isInteractive ? 'button' : undefined}
      tabIndex={isInteractive ? 0 : undefined}
      aria-label={isInteractive ? `View ${item.name} products` : undefined}
      onClick={handleActivate}
      onKeyDown={handleKeyDown}
    >
      <div className="custom-border w-[350px] h-[450px] lg:w-[350px] lg:h-[500px] rounded-md bg-charcoal overflow-hidden mx-4">
        <img
          className="border-x-[7px] border-t-[7px] border-electric-card-color w-full h-[20rem] object-cover object-top group-hover:scale-95 transition-transform duration-700"
          src={item.image}
          alt={item.name}
          loading="lazy"
          onError={(e) => {
            e.currentTarget.onerror = null
            e.currentTarget.src = '/Assets/placeholder.png'
          }}
        />
      </div>
      <h1 className="text-center text-base">{item.name}</h1>
    </div>
  )
}

export default FlashSale

// import { HomeCategory } from '../../../../types/HomeCategoryType'

// const FlashSale = ({ item }: { item: HomeCategory }) => {
//     return (
//         <div className='flex flex-col items-center group cursor-pointer gap-2 justify-center'>
//             <div className='custom-border w-[350px] h-[450px] lg:w-[350px] lg:h-[500px] rounded-md bg-charcoal overflow-hidden mx-4'>
//                 <img
//                     className='border-x-[7px] border-t-[7px] border-electric-card-color w-full h-[20rem] object-cover object-top group-hover:scale-95 transition-transform duration-700'
//                     //className='w-full h-full object-cover object-top rounded-md group-hover:scale-95 transition-transform duration-700'
//                     src={item.image}
//                     alt={item.name}
//                 />
//             </div>
//             <h1 className="text-center text-base">{item.name}</h1>
//         </div>
//     )
// }

// export default FlashSale
