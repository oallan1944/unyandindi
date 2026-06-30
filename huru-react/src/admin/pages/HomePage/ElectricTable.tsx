import React from 'react'
import HomeCategoryTable from './HomeCategoryTable';
import { useAppSelector } from '../../../State/store';

const ElectricTable = () => {
    const { customer } = useAppSelector(store => store);
     const electricCategories = customer.homePageData?.electricCategories || [];
    return (
        <div>
            <HomeCategoryTable data={electricCategories} />
        </div>
    );
};

export default ElectricTable
