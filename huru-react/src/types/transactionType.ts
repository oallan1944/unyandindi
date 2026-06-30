import { Order } from "./orderType";
import { Seller } from "./sellerType";
import { User } from "./userTypes";

export interface Transaction {
    id: number;
    customer: User;
    order: Order;
    seller: Seller;
    date: string;
}