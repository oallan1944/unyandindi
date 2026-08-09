// GridTable.tsx
import { useEffect } from 'react'
import HomeCategoryTable from './HomeCategoryTable'
import { useAppDispatch, useAppSelector } from '../../../State/store';
import { fetchHomeCategories } from '../../../State/admin/AdminSlice';

const GridTable = () => {
    const dispatch = useAppDispatch();
    const { admin } = useAppSelector(store => store);

    useEffect(() => {
        dispatch(fetchHomeCategories());
    }, [dispatch]);

    const gridCategories = admin.categories.filter(c => c.section === "GRID");

    return (
        <div>
            <HomeCategoryTable data={gridCategories} section="GRID" />
        </div>
    )
}

export default GridTable