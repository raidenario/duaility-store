/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx,ts,tsx}'],
  theme: {
    extend: {
      colors: {
        bg: {
          dark: '#0d1117',
          card: '#111827',
          soft: '#0f172a',
        },
        accent: {
          blue: '#58a6ff',
          green: '#3fb950',
          purple: '#a371f7',
          orange: '#d29922',
          pink: '#ec4899',
        },
      },
      fontFamily: {
        display: ['"Space Grotesk"', 'Inter', 'sans-serif'],
        body: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'monospace'],
      },
      boxShadow: {
        neon: '0 20px 50px rgba(88,166,255,0.15)',
      },
    },
  },
  plugins: [],
}
