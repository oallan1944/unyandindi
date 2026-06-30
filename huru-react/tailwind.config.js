// /** @type {import('tailwindcss').Config} */
// module.exports = {
//   content: [],
//   theme: {
//     extend: {},
//   },
//   plugins: [],
// }

/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {
      colors: {

        "primary-color": "#00927c",
        // "primary-color": "#4caf50",
        "mainBody-color": "#ff3b30",
        // "electric-card-color": "#EA580C", deeper orange-600
        "electric-card-color": "#f97316",
        "secondary-color": "#f42c37",
        "brandYellow-color": "#fdc62e",
        "brandGreen-color": "#2dcc6f",
        "brandBlue-color": "#1376f4",
        "brandWhite-color": "#eeeeee",
        "charcoal": "#36454F"

      },

      keyframes: {
        flash: {
          '0%, 50%, 100%': { opacity: '1' },
          '25%, 75%': { opacity: '0.2' },
        },
      },
      animation: {
        flash: 'flash 3s ease-in-out infinite',
      },
    },
  },
  plugins: [],
}