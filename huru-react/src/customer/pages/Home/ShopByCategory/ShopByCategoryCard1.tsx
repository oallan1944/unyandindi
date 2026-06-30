import React from 'react';
import {
  Box,
  Card,
  CardContent,
  CardMedia,
  
  Grid,
  
  Typography
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { menLevelThree } from '../../../../Data/Category/Level three/menLevelThree';

interface Category {
  categoryId: string;
  name: string;
  parentCategoryId: string;
  parentCategoryName: string;
}

const groupByParentCategory = (data: Category[]) => {
  return data.reduce((acc, item) => {
    if (!acc[item.parentCategoryId]) {
      acc[item.parentCategoryId] = [];
    }
    acc[item.parentCategoryId].push(item);
    return acc;
  }, {} as { [key: string]: Category[] });
};

const MenCategoryCards: React.FC = () => {
  const navigate = useNavigate();
  const groupedCategories = groupByParentCategory(menLevelThree);

  return (
   <Box sx={{ p: 3, overflowX: 'auto' }}>
  <Box
    sx={{
      display: 'flex',
      flexWrap: 'nowrap',
      gap: 3,
      width: 'max-content',
    }}
  >
    {Object.entries(groupedCategories).map(([parentCategoryId, subcategories]) => {
      const parentName = subcategories[0]?.parentCategoryName || 'Category';

      return (
        <Card
          key={parentCategoryId}
          sx={{
            p: 2,
            minWidth: 320,
            maxWidth: 360,
            flexShrink: 0,
            boxShadow: 3,
          }}
        >
          <Typography variant="h5" sx={{ mb: 2, textAlign: 'center' }}>
            {parentName}
          </Typography>

          <Grid container spacing={2}>
            {subcategories.slice(0, 4).map((subcategory) => (
              <Grid key={subcategory.categoryId} size= {{xs:6}}>
                <Card
                  onClick={() => navigate(`/products/${subcategory.categoryId}`)}
                  sx={{
                    cursor: 'pointer',
                    transition: '0.3s',
                    '&:hover': { transform: 'scale(1.03)', boxShadow: 4 }
                  }}
                >
                  <CardMedia
                    component="img"
                    height="140"
                    image={`https://via.placeholder.com/150?text=${encodeURIComponent(subcategory.name)}`}
                    alt={subcategory.name}
                  />
                  <CardContent sx={{ p: 1 }}>
                    <Typography variant="body2" align="center" noWrap>
                      {subcategory.name}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Card>
      );
    })}
  </Box>
</Box>



  );
};

export default MenCategoryCards;
