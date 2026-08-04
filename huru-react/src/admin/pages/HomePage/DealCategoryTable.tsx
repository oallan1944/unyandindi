import React, { useEffect } from 'react'
import HomeCategoryTable from './HomeCategoryTable'
import { useAppDispatch, useAppSelector } from '../../../State/store';
import { fetchHomeCategories } from '../../../State/admin/AdminSlice';

const DealCategoryTable = () => {
    const dispatch = useAppDispatch();
    const { admin } = useAppSelector(store => store);

    useEffect(() => {
        dispatch(fetchHomeCategories());
    }, [dispatch]);

    const dealCategories = admin.categories.filter(c => c.section === "DEALS");

    return (
        <div>
            <HomeCategoryTable data={dealCategories} section="DEALS" />
        </div>
    )
}

export default DealCategoryTable