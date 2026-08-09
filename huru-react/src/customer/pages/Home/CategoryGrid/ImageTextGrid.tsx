import { motion } from "framer-motion";
import { useEffect, useState } from 'react';
const ImageTextGrid = () => {
    const images = [
        "/Assets/electricAssets/macbook.png",
        "/Assets/electricAssets/earphone.png",
        "/Assets/electricAssets/gaming.png",
        "/Assets/electricAssets/headphone.png",
        "/Assets/electricAssets/speaker.png",
        "/Assets/electricAssets/vr.png",
        "/Assets/electricAssets/watch.png",
        "/Assets/electricAssets/watch copy.png",
    ];

    const [currentImageIndex, setCurrentImageIndex] = useState(0);

    useEffect(() => {
        const interval = setInterval(() => {
            setCurrentImageIndex((prev) => (prev + 1) % images.length);
        }, 5000);
        return () => clearInterval(interval);
    }, [images.length]);

    return (
        <div className="grid grid-cols-1 sm:grid-cols-3 bg-charcoal mx-auto overflow-hidden rounded-lg h-full">
            {/* Text section */}
            <div className="flex flex-col justify-center gap-3 p-3 text-center sm:text-left order-2 sm:order-1 col-span-1">
                <motion.h1
                    className="text-xl lg:text-2xl font-bold"
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.5 }}
                >
                    New Season Styles
                </motion.h1>

                <motion.h2
                    className="text-lg lg:text-xl font-semibold"
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.7 }}
                >
                    Exclusive Deals
                </motion.h2>

                <motion.h3
                    className="text-base font-medium text-brandWhite-color"
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.9 }}
                >
                    Up to 30% off
                </motion.h3>

                <motion.button
                    className="bg-white text-primary-color font-medium py-1.5 px-3 rounded-full hover:scale-105 transition duration-300 mx-auto sm:mx-0"
                    whileHover={{ scale: 1.05 }}
                    whileTap={{ scale: 0.95 }}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 1.1 }}
                >
                    Shop Now
                </motion.button>
            </div>

            {/* Image section */}
            <motion.div
                className="flex flex-col items-center justify-center p-2 sm:p-3 order-1 sm:order-2 col-span-2 h-full"
                initial={{ opacity: 0, scale: 0.85 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ duration: 0.6 }}
            >
                <img
                    src={images[currentImageIndex]}
                    alt="promo"
                    className="w-[240px] h-[240px] sm:w-[300px] sm:h-[300px] object-contain drop-shadow-[-8px_4px_6px_rgba(0,0,0,.3)] transition duration-500 rounded-md"
                />

                {/* Dots */}
                <div className="flex space-x-2 mt-3 z-20">
                    {images.map((_, index) => (
                        <motion.span
                            key={index}
                            className={`w-2.5 h-2.5 rounded-full ${index === currentImageIndex ? "bg-white" : "bg-white/40"
                                }`}
                            initial={{ scale: 0.8, opacity: 0 }}
                            animate={{ scale: 1, opacity: 1 }}
                            transition={{ duration: 0.3, delay: index * 0.05 }}
                        />
                    ))}
                </div>

            </motion.div>
        </div>
    );
};

export default ImageTextGrid;
