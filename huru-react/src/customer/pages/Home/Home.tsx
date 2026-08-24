import { useEffect } from 'react'
import ShopByCategory from './ShopByCategory/ShopByCategory'
import DealCategories from './Deal/DealCategories'
import Button from '@mui/material/Button'
import { Storefront } from '@mui/icons-material'
import { useNavigate } from 'react-router-dom'
import CategoryGrid2 from './CategoryGrid/CategoryGrid2'
import CountdownTimer from '../../../hooks/CountdownTimer'
import HeroSlider from './ElectricCategory/HeroSlider'
import { useAppDispatch, useAppSelector } from '../../../State/store'
import { fetchHomePageData } from '../../../State/customer/customerSlice'
import ErrorBoundary from '../../../component/ErrorBoundary'





const Home = () => {
  const navigate = useNavigate()
  const dispatch = useAppDispatch()
  const { customer } = useAppSelector(store => store)

useEffect(() => {
    dispatch(fetchHomePageData()).then((result: any) => {
        console.log("FULL RESPONSE:", result.payload)
        console.log("shopByCategories:", result.payload?.shopByCategories)
        console.log("electricCategories:", result.payload?.electricCategories)
        console.log("deals:", result.payload?.deals)
    })
}, [dispatch])

  if (customer.loading && !customer.homePageData ) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="animate-spin rounded-full h-16 w-16 border-t-4 border-b-4 border-primary-color"></div>
      </div>
    )
  }
  return (
    <>
      <div

        className='space-y-1 lg:space-y-2 relative pb-3'>

          <ErrorBoundary>
        <section className='px-2 lg:px-4'>
          {/* <ElectricCategory1 /> */}
          <HeroSlider />
        </section>
        </ErrorBoundary>
          <ErrorBoundary>
        <section>

          <CategoryGrid2 />
          {/* <CategoryGrid /> */}
        </section>
        </ErrorBoundary>

          <ErrorBoundary>

        <div className="pt-2">
          <div className="py-4 bg-electric-card-color flex flex-col lg:flex-row items-center justify-between px-6 space-y-4 lg:space-y-0  shadow-md">

            {/* Heading + Button — left side */}
            <div className="flex flex-col lg:flex-row items-center gap-4 w-full lg:w-auto">
              <h1 className="text-lg lg:text-2xl font-bold text-white text-center lg:text-left">
                Today's Deal, All Day
              </h1>
              <Button variant='contained' className="bg-charcoal text-primary-color font-bold py-2 px-8 rounded-lg hover:bg-gray-200 transition">
                Explore Deals
              </Button>
            </div>

            {/* Timer Card — right side */}
            <div className="flex justify-center lg:justify-end w-full lg:w-auto">
              <div className="bg-black text-white px-5 py-2 rounded-lg shadow-lg flex items-center space-x-3">
                <span className="text-sm font-semibold">Sale Ends In:</span>
                <CountdownTimer
                  targetTime={new Date(Date.now() + 24 * 60 * 60 * 1000)}
                />
              </div>
            </div>

          </div>

          <DealCategories />
        </div>
        </ErrorBoundary>

         <ErrorBoundary>

        <section className="py-2">
          <div className="py-5 bg-electric-card-color flex flex-col lg:flex-row items-center justify-between px-6 space-y-4 lg:space-y-0 shadow-md">

            {/* Heading — centered, bigger */}
            <h1 className="text-2xl lg:text-4xl font-bold text-white text-center w-full animate-bounce">
              Shop Your Favorite Category
            </h1>

            {/* Button — at end on large screens */}
            <div className="flex w-full lg:w-auto justify-center lg:justify-end">
              <Button
                variant="text"
                className="text-white font-bold py-2 px-6 rounded-lg hover:bg-gray-200 hover:text-primary-color transition whitespace-nowrap"
              >
                See All
              </Button>
            </div>

          </div>

          <ShopByCategory />
        </section>

        </ErrorBoundary>

         <ErrorBoundary>
        <section
          className=' lg:px-20 relative h-[200px] lg:h-[450px] object-cover'>
          <img
            className='w-full h-full'
            src="https://images.pexels.com/photos/5712970/pexels-photo-5712970.jpeg?auto=compress&cs=tinysrgb&w=600" alt="" />

          <div className='absolute top-1/3 left-4 lg:left-[15rem] -translate-y-1/2 font-semibold lg:text-4xl space-y-3'>
            <h1>Sell your Product</h1>
            <p className='text-lg md:text-2xl'>With <span className='logo'>Huru Market</span> </p>

            <div className='pt-6 flex justify-center'>
              <Button onClick={() => navigate("/become-seller")}
                startIcon={<Storefront />} variant='contained' size='large'>Become a Seller</Button>
            </div>
          </div>
        </section>
        </ErrorBoundary>

      </div>

    </>
  )
}

export default Home








// import { useEffect } from 'react'
// import Deal from './Deal/Deal'
// import ShopByCategory from './ShopByCategory/ShopByCategory'
// import Button from '@mui/material/Button'
// import { Storefront } from '@mui/icons-material'
// import { useNavigate } from 'react-router-dom'
// import CategoryGrid2 from './CategoryGrid/CategoryGrid2'
// import CountdownTimer from '../../../hooks/CountdownTimer'
// import HeroSlider from './ElectricCategory/HeroSlider'
// import { useAppDispatch, useAppSelector } from '../../../State/store'
// import { fetchHomePageData } from '../../../State/customer/customerSlice'
// import ErrorBoundary from '../../../component/ErrorBoundary'





// const Home = () => {
//   const navigate = useNavigate()
//   const dispatch = useAppDispatch()
//   const { customer } = useAppSelector(store => store)

// useEffect(() => {
//     dispatch(fetchHomePageData()).then((result: any) => {
//         console.log("FULL RESPONSE:", result.payload)
//         console.log("shopByCategories:", result.payload?.shopByCategories)
//         console.log("electricCategories:", result.payload?.electricCategories)
//         console.log("deals:", result.payload?.deals)
//     })
// }, [dispatch])

//   if (customer.loading && !customer.homePageData ) {
//     return (
//       <div className="flex items-center justify-center h-screen">
//         <div className="animate-spin rounded-full h-16 w-16 border-t-4 border-b-4 border-primary-color"></div>
//       </div>
//     )
//   }
//   return (
//     <>
//       <div

//         className='space-y-1 lg:space-y-2 relative pb-3'>

//           <ErrorBoundary>
//         <section className='px-2 lg:px-4'>
//           {/* <ElectricCategory1 /> */}
//           <HeroSlider />
//         </section>
//         </ErrorBoundary>
//         {/* <ErrorBoundary>
//          <Deal />
//         </ErrorBoundary> */}
//           <ErrorBoundary>
//         <section>

//           <CategoryGrid2 />
//           {/* <CategoryGrid /> */}
//         </section>
//         </ErrorBoundary>

//           <ErrorBoundary>

//         <div className="pt-2">
//           <div className="py-4 bg-electric-card-color flex flex-col lg:flex-row items-center justify-between px-6 space-y-4 lg:space-y-0  shadow-md">

//             {/* Heading + Button — left side */}
//             <div className="flex flex-col lg:flex-row items-center gap-4 w-full lg:w-auto">
//               <h1 className="text-lg lg:text-2xl font-bold text-white text-center lg:text-left">
//                 Today's Deal, All Day
//               </h1>
//               <Button variant='contained' className="bg-charcoal text-primary-color font-bold py-2 px-8 rounded-lg hover:bg-gray-200 transition">
//                 Explore Deals
//               </Button>
//             </div>

//             {/* Timer Card — right side */}
//             <div className="flex justify-center lg:justify-end w-full lg:w-auto">
//               <div className="bg-black text-white px-5 py-2 rounded-lg shadow-lg flex items-center space-x-3">
//                 <span className="text-sm font-semibold">Sale Ends In:</span>
//                 <CountdownTimer
//                   targetTime={new Date(Date.now() + 24 * 60 * 60 * 1000)}
//                 />
//               </div>
//             </div>

//           </div>

//           <Deal />
//         </div>
//         </ErrorBoundary>

//          <ErrorBoundary>

//         <section className="py-2">
//           <div className="py-5 bg-electric-card-color flex flex-col lg:flex-row items-center justify-between px-6 space-y-4 lg:space-y-0 shadow-md">

//             {/* Heading — centered, bigger */}
//             <h1 className="text-2xl lg:text-4xl font-bold text-white text-center w-full animate-bounce">
//               Shop Your Favorite Category
//             </h1>

//             {/* Button — at end on large screens */}
//             <div className="flex w-full lg:w-auto justify-center lg:justify-end">
//               <Button
//                 variant="text"
//                 className="text-white font-bold py-2 px-6 rounded-lg hover:bg-gray-200 hover:text-primary-color transition whitespace-nowrap"
//               >
//                 See All
//               </Button>
//             </div>

//           </div>

//           <ShopByCategory />
//         </section>

//         </ErrorBoundary>

//          <ErrorBoundary>
//         <section
//           className=' lg:px-20 relative h-[200px] lg:h-[450px] object-cover'>
//           <img
//             className='w-full h-full'
//             src="https://images.pexels.com/photos/5712970/pexels-photo-5712970.jpeg?auto=compress&cs=tinysrgb&w=600" alt="" />

//           <div className='absolute top-1/3 left-4 lg:left-[15rem] transform-translate
//           -y-1/2 font-semibold lg:text-4xl space-y-3'>
//             <h1>Sell your Product</h1>
//             <p className='text-lg md:text-2xl'>With <span className='logo'>Huru Market</span> </p>

//             <div className='pt-6 flex justify-center'>
//               <Button onClick={() => navigate("/become-seller")}
//                 startIcon={<Storefront />} variant='contained' size='large'>Become a Seller</Button>
//             </div>
//           </div>
//         </section>
//         </ErrorBoundary>

//       </div>

//     </>
//   )
// }

// export default Home


