// src/component/common/SizeSelect.tsx
import React from 'react';
import { Button } from '@mui/material';

interface SizeSelectProps {
    sizes: string[];
    selectedSize: string;
    onSelectSize: (size: string) => void;
}

const SizeSelect: React.FC<SizeSelectProps> = ({ sizes, selectedSize, onSelectSize }) => {
    return (
        <div className='flex flex-wrap gap-2 mt-2'>
            {sizes.length > 0 ? (
                sizes.map((size, index) => (
                    <Button
                        key={index}
                        variant={selectedSize === size ? 'contained' : 'outlined'}
                        onClick={() => onSelectSize(size)}
                        sx={{ textTransform: 'capitalize' }}
                    >
                        {size}
                    </Button>
                ))
            ) : (
                <p className="text-sm text-gray-500">No size options available</p>
            )}
        </div>
    );
};

export default SizeSelect;
