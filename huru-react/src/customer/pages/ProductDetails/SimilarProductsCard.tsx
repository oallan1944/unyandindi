import React from 'react'

const SimilarProductsCard = () => {
    return (
        <div>
            <div className='group px-4 relative'>
                <div className='card' >



                    <img
                        className='card-media object-top'

                        src={"https://images.pexels.com/photos/31762174/pexels-photo-31762174/free-photo-of-professional-portrait-in-tan-suit.jpeg?auto=compress&cs=tinysrgb&w=600"} alt=""

                    />


                </div>
                <div className='details pt-3 space-y-1 group-hover-effect rounded-md'>

                    <div className='name'>
                        <h1>Polo</h1>
                        <p>White T-shirt</p>
                    </div>
                    <div className='price flex items-center gap-3'>
                        <span className='font-sans text-gray-800'>
                            $4
                        </span>
                        <span className='thin-line-through text-gray-500'>
                            $5
                        </span>
                        <span className='text-primary-color font-semibold'>
                            60%
                        </span>


                    </div>

                </div>

            </div>
        </div>
    )
}

export default SimilarProductsCard
