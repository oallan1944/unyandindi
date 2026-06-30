export interface ProductImage {
  url: string;
}

export interface ProductSize {
  value: string;
}

export interface ProductColor {
  name: string;
  hex: string;
}

export interface AddProductForm {
  title: string;
  description: string;
  mrpPrice: number;
  sellingPrice: number;
  quantity: number;
  color: ProductColor[];   // Updated to array of { name, hex } objects
  images: string[];        // URLs as strings
  category: string;
  category2: string;
  category3: string;
  sizes: string[];         // Array of strings
}
