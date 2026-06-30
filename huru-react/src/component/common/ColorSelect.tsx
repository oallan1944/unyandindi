import React from 'react';
import { Button } from '@mui/material';
import { colors as colorData } from '../../Data/Filter/color'; // 👈 Correct path

interface ColorSelectProps {
    colors: string[];
    selectedColor: string;
    onSelectColor: (color: string) => void;
}

const ColorSelect: React.FC<ColorSelectProps> = ({ colors, selectedColor, onSelectColor }) => {
    if (!colors.length) {
        return <p className="text-sm text-gray-500">No color options available</p>;
    }

    return (
        <div className='flex flex-wrap gap-2 mt-2'>
            {colors.map((colorName, index) => {
                const colorEntry = colorData.find(c => c.name.toLowerCase() === colorName.toLowerCase());
                const colorHex = colorEntry ? colorEntry.hex : '#E0E0E0';

                return (
                    <Button
                        key={index}
                        variant={selectedColor === colorName ? 'contained' : 'outlined'}
                        onClick={() => onSelectColor(colorName)}
                        sx={{
                            textTransform: 'capitalize',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '0.5rem',
                        }}
                    >
                        <span
                            style={{
                                display: 'inline-block',
                                width: '16px',
                                height: '16px',
                                borderRadius: '50%',
                                backgroundColor: colorHex,
                                border: '1px solid #ccc',
                            }}
                        />
                        {colorName}
                    </Button>
                );
            })}
        </div>
    );
};

export default ColorSelect;
