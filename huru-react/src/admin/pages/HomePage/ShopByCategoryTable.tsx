import React from 'react'
import HomeCategoryTable from './HomeCategoryTable'
import { useAppSelector } from '../../../State/store'

const ShopByCategoryTable = () => {
    const { customer } = useAppSelector(store => store);
    return (
        <div>
            <HomeCategoryTable data={customer.homePageData?.shopByCategories || []} />
        </div>
    )
}

export default ShopByCategoryTable
