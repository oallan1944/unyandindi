import { menLevelTwo } from '../../../Data/Category/Level two/menLevelTwo'
import { womenLevelTwo } from '../../../Data/Category/Level two/womenLevelTwo'
import { electronicsLevelTwo } from '../../../Data/Category/Level two/electronicsLevelTwo'
import { furnitureLevelTwo } from '../../../Data/Category/Level two/furnitureLevelTwo'
import { menLevelThree } from '../../../Data/Category/Level three/menLevelThree'
import { womenLevelThree } from '../../../Data/Category/Level three/womenLevelThree'
import { electronicsLevelThree } from '../../../Data/Category/Level three/electronicsLevelThree'
import { FurnitureLevelThree } from '../../../Data/Category/Level three/furnitureLevelThree'
import { Box } from '@mui/material'
import { fitnessLevelTwo } from '../../../Data/Category/Level two/fitnessLevelTwo'
import { fitnessLevelThree } from '../../../Data/Category/Level three/fitnessLevelThree'
import { useNavigate } from 'react-router-dom'


const categoryTwo: { [key: string]: any[] } = {
    men: menLevelTwo,
    women: womenLevelTwo,
    electronics: electronicsLevelTwo,
    home_furniture: furnitureLevelTwo,
    sports_fitness: fitnessLevelTwo

}
const categoryThree: { [key: string]: any[] } = {
    men: menLevelThree,
    women: womenLevelThree,
    electronics: electronicsLevelThree,
    home_furniture: FurnitureLevelThree,
    sports_fitness: fitnessLevelThree
}

const CategorySheet = ({ selectedCategory }: any) => {
    const navigate = useNavigate()
    // filtering child category
    const childCategory = (category: any, parentCategoryId: any) => {
        return category.filter((child: any) => child.parentCategoryId === parentCategoryId)
    }

    return (
        <Box
            sx={{ zIndex: 2 }}
            className='bg-white shadow-lg lg:h-[500px] overflow-y-auto'>
            <div className='flex text-sm flex-wrap'>
                {/* map categories */}
                {
                    categoryTwo[selectedCategory]?.map((item: any, index) => (
                        <div
                            className={`p-8 lg:w-[20%] ${index % 2 === 0 ? "bg-slate-50" : "bg-white"} `}
                        >
                            <p className='text-mainBody-color mb-5 font-semibold'>
                                {item.name}
                            </p>
                            <ul className='space-y-3'>
                                {childCategory(categoryThree[selectedCategory], item.categoryId).map(
                                    (childItem: any) => (

                                        <li
                                            key={childItem.categoryId}
                                            onClick={() => navigate("/products/" + childItem.categoryId)}
                                            className='hover:text-mainBody-color cursor-pointer'>
                                            {childItem.name}

                                        </li>
                                    )

                                )}


                            </ul>
                        </div>
                    )
                    )}


            </div>

        </Box>
    )
}

export default CategorySheet
