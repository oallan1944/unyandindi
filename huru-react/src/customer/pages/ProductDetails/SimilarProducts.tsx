import React from 'react'
import SimilarProductsCard from './SimilarProductsCard'

const SimilarProducts = () => {
    return (
        <div className='grid grid-cols-1 md:grid-cols-4 lg:grid-cols-6 sm:grid-cols-2 gap-4 justify-between gap-y-8'>
            {[1, 1, 1, 1, 1, 1, 1,].map((item) => <SimilarProductsCard />)}

        </div>
    )
}

export default SimilarProducts
