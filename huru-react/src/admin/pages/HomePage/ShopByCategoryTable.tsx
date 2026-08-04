// ShopByCategoryTable.tsx
import React, { useEffect } from 'react'
import HomeCategoryTable from './HomeCategoryTable'
import { useAppDispatch, useAppSelector } from '../../../State/store'
import { fetchHomeCategories } from '../../../State/admin/AdminSlice';

const ShopByCategoryTable = () => {
    const dispatch = useAppDispatch();
    const { admin } = useAppSelector(store => store);

    useEffect(() => {
        dispatch(fetchHomeCategories());
    }, [dispatch]);

    const shopByCategories = admin.categories.filter(c => c.section === "SHOP_BY_CATEGORIES");

    return (
        <div>
            <HomeCategoryTable data={shopByCategories} section="SHOP_BY_CATEGORIES" />
        </div>
    )
}

export default ShopByCategoryTable