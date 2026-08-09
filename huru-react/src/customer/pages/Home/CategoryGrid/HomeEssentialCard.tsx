import { Button } from '@mui/material'
const HomeEssentialCard = () => {
    const items = [
        { src: '/Assets/home/fridge.jpg', name: 'Fridge' },
        { src: '/Assets/home/bed-green.jpg', name: 'Bed' },
        { src: '/Assets/home/TV.jpg', name: 'TV' },
        { src: '/Assets/home/sofa.jpg', name: 'Sofa' },
    ]

    const handleImageClick = () => {

        // You could navigate or open a modal etc. here
    }

    return (
        <div className="h-full bg-[#1e1e1e] overflow-hidden shadow-lg flex flex-col">

            {/* Top Section */}
            <div className="p-4 bg-gray-900 text-white text-xl font-semibold flex items-center justify-center">
                Shop for your home
            </div>

            {/* Image Grid */}
            <div className="grid gap-2 p-2 flex-1
        sm:grid-cols-2 sm:grid-rows-2 
        grid-cols-1 grid-rows-4"
            >
                {items.map(({ src, name }, idx) => (
                    <div key={idx} className="flex flex-col">
                        {/* Clickable Image */}
                        <button
                            onClick={() => handleImageClick()}
                            className="bg-[#333333] overflow-hidden aspect-square relative group transition duration-300  focus:outline-none"
                        >
                            <img
                                className="w-full h-full object-cover bg-[#333333] transition-transform duration-300 group-hover:scale-105 group-hover:brightness-90"
                                src={src}
                                alt={name}
                            />
                        </button>

                        {/* Item Name */}
                        <div className="mt-1 text-center text-sm text-white font-medium">
                            {name}
                        </div>
                    </div>
                ))}
            </div>

            {/* Explore Button */}
            <div className="p-2 bg-white text-center">
                <Button
                    variant="text"
                    className="text-gray-900 cursor-pointer py-2 px-4 transition duration-300 transform hover:scale-105 hover:bg-gray-900 hover:text-white"
                >
                    Discover More
                </Button>
            </div>

        </div>
    )
}

export default HomeEssentialCard
