import { defineVitestConfig } from '@nuxt/test-utils/config'

export default defineVitestConfig({
  test: {
    environment: 'nuxt',
    include: ['**/*.{test,spec}.ts'],
    // e2e は Playwright で実行する（pnpm test:e2e）
    exclude: ['node_modules', '.old-nuxt2', '**/e2e/**'],
    globals: true
  }
})
