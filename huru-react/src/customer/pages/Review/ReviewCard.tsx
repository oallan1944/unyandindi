import { Delete } from '@mui/icons-material'
import { Avatar, Box, Grid, IconButton, Rating } from '@mui/material'
import { red } from '@mui/material/colors'

import React from 'react'

const ReviewCard = () => {
    return (
        <div className='flex justify-between gap-2 '>
            <Grid container spacing={10} >
                <Grid size={{ xs: 1 }} >
                    <Box>
                        <Avatar className='text-white' sx={{ width: 56, height: 56, bgcolor: "#9155FD" }}>
                            A
                        </Avatar>
                    </Box>

                </Grid>

                <Grid size={{ xs: 10 }}>
                    <div className='space-y-2 '>
                        <div>
                            <p className='font-semibold text-lg'>Allan</p>
                            <p className='opacity-70'> 2025-04-27T23:16:07.478333</p>
                        </div>

                    </div>
                    <Rating
                        readOnly
                        value={4}
                        precision={1}
                    />
                    <p>value for money product, great product</p>
                    <div>
                        <img className='w-24 h-24 object-cover'
                            src="https://images.pexels.com/photos/10669638/pexels-photo-10669638.jpeg?auto=compress&cs=tinysrgb&w=600"
                            alt="" />
                    </div>

                </Grid>

            </Grid>
            <div>
                <IconButton>
                    <Delete sx={{ color: red[700] }} />
                </IconButton>
            </div>

        </div>
    )
}

export default ReviewCard
