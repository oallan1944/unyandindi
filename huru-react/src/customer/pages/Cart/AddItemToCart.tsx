
import { toast } from 'react-toastify';
import { addItemToCart, fetchUserCart } from '../../../State/customer/cartSlice';
import { useAppDispatch } from '../../../State/store';

const useAddToCart = () => {
  const dispatch = useAppDispatch();

  const handleAddToCart = (productId: number, size: string, quantity: number = 1) => {
    const jwt = localStorage.getItem("jwt");

    if (!jwt) {
      toast.error("You must be logged in to add items to cart");
      return;
    }

    dispatch(
      addItemToCart({
        jwt,
        request: {
          productId,
          size,
          quantity,
        },
      })
    )
      .unwrap()
      .then(() => {
        toast.success("Item added to cart!");
        dispatch(fetchUserCart(jwt));  // Optionally refresh cart summary totals
      })
      .catch((error) => {
        console.error(error);
        toast.error("Failed to add item to cart");
      });
  };

  return handleAddToCart;
};



export default useAddToCart;
