import React from 'react'
import { HomeCategory } from '../../../../types/HomeCategoryType'

const AllCategory = ({ item }: { item: HomeCategory }) => {
    return (
        <div
            className='w-full flex items-center gap-3 cursor-pointer px-2 py-1 rounded-md transition duration-200 hover:bg-gray-100'
        >
            {/* Optional: if image property exists in the future */}
            {/* <img src={item.image} alt={item.name} className='w-6 h-6 object-cover' /> */}

            <h1 className='text-sm text-gray-800'>{item.name}</h1>
        </div>
    )
}

export default AllCategory
