import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuth } from '../useAuth'

vi.mock('~/lib/firebase/auth', () => ({
  signOut: vi.fn(async () => {}),
  onAuthStateChanged: vi.fn()
}))

describe('useAuth', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('ログアウトすると ID トークンの Cookie を消す', async () => {
    useCookie('id-token').value = 'token'
    useCookie('id-token-check-date').value = '2026-10-24T00:00:00.000Z'
    await nextTick()
    expect(document.cookie).toContain('id-token=token')

    await useAuth().logout()
    await nextTick()

    expect(document.cookie).not.toContain('id-token=')
    expect(document.cookie).not.toContain('id-token-check-date=')
  })
})
