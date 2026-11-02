import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Must match the backend's app.cors.allowed-origins (application.yml) -
    // the SRS's Operating Environment section specifies port 8081 for the
    // frontend. Changing this without updating the backend's CORS config
    // will silently break every API call (browser blocks it before any
    // response comes back, so it shows as a generic network error, not a
    // clear CORS message).
    port: 8081,
  },
})
