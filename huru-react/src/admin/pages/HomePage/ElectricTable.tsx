// ElectricTable.tsx
import { useEffect } from 'react'
import HomeCategoryTable from './HomeCategoryTable';
import { useAppDispatch, useAppSelector } from '../../../State/store';
import { fetchHomeCategories } from '../../../State/admin/AdminSlice';

const ElectricTable = () => {
    const dispatch = useAppDispatch();
    const { admin } = useAppSelector(store => store);

    useEffect(() => {
        dispatch(fetchHomeCategories());
    }, [dispatch]);

    const electricCategories = admin.categories.filter(c => c.section === "ELECTRIC_CATEGORIES");

    return (
        <div>
            <HomeCategoryTable data={electricCategories} section="ELECTRIC_CATEGORIES" />
        </div>
    );
};

export default ElectricTable