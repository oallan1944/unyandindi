import axios from "axios"

const api = "http://localhost:5454/products"
export const fetchProduct = async () => {

    try {
        const response = await axios.get(api)
        console.log("Response", response)

    } catch (error) {
        console.error(error)
    }

}