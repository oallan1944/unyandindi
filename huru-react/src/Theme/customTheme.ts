import { createTheme } from "@mui/material";

const customTheme = createTheme({
    palette: {
        mode: 'light',
        primary: {
            main: "#00927c"
        },
        secondary: {
            main: "#f42c37"
            // main: '#800020'

        }
    }
})

export default customTheme;