import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import type { User } from 'firebase/auth'
import { useAuth } from '../useAuth'

vi.mock('~/lib/firebase/auth', () => ({
  signOut: vi.fn(async () => {}),
  onAuthStateChanged: vi.fn()
}))

describe('useAuth', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('初期化時に以前保存していた ID トークンの Cookie を消す', async () => {
    useCookie('id-token').value = 'token'
    useCookie('id-token-check-date').value = '2026-10-24T00:00:00.000Z'
    await nextTick()
    expect(document.cookie).toContain('id-token=token')

    useAuth().initializeAuth()
    await nextTick()

    expect(document.cookie).not.toContain('id-token=')
    expect(document.cookie).not.toContain('id-token-check-date=')
  })

  it('トークンは Firebase のユーザーから取得し、Cookie には保存しない', async () => {
    const getIdToken = vi.fn(async () => 'fresh-token')
    useAuthStore().setUser({ getIdToken } as unknown as User)

    expect(await useAuth().getAuthToken()).toBe('fresh-token')
    expect(getIdToken).toHaveBeenCalledWith()
    await nextTick()
    expect(document.cookie).not.toContain('fresh-token')
  })

  it('ログインしていなければトークンは null', async () => {
    expect(await useAuth().getAuthToken()).toBeNull()
  })

  it('トークンの取得に失敗したら null', async () => {
    const getIdToken = vi.fn(async () => {
      throw new Error('network')
    })
    vi.spyOn(console, 'error').mockImplementation(() => {})
    useAuthStore().setUser({ getIdToken } as unknown as User)

    expect(await useAuth().getAuthToken()).toBeNull()
  })
})
