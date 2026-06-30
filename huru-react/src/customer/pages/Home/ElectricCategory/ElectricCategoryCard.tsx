// import React from 'react'
// import { HomeCategory } from '../../../../types/HomeCategoryType'

// const ElectricCategoryCard = ({ item }: { item: HomeCategory }) => {
//   return (
//     <div className='flex flex-col gap-2 justify-center'>
//       <img className='object-contain h-12'
//         src={item.image} alt="" />

//       <h2 className='font-semibold text-sm text-center'>{item.name}</h2>
//     </div>
//   )
// }

// export default ElectricCategoryCard
import React from 'react'
import { HomeCategory } from '../../../../types/HomeCategoryType'

const ElectricCategoryCard = ({ item }: { item: HomeCategory }) => {
  return (


    <div className='flex flex-col gap-2 justify-center items-center p-4  rounded-lg bg-electric-card-color'>


      <img
        className='object-contain h-12 bg-secondary'
        src={item.image}
        alt={item.name}

      />
      <div>
        <h2 className='font-semibold text-sm text-center text-white'>{item.name}</h2>
      </div>
    </div>
  )
}

export default ElectricCategoryCard
