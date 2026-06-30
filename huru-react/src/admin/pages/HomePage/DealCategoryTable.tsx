import React from 'react'
import HomeCategoryTable from './HomeCategoryTable'
import { useAppSelector } from '../../../State/store';

const DealCategoryTable = () => {
    const { customer } = useAppSelector(store => store);
    return (
        <div>
            <HomeCategoryTable data={customer.homePageData?.dealCategories||[ ]} />
        </div>
    )
}

export default DealCategoryTable
