import { defineStore } from 'pinia'
import type { User } from 'firebase/auth'
import type { MyselfPlayerView } from '~/lib/api/types'

/**
 * 認証状態管理Store
 * 状態の保持と更新のみを担当
 */
export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<User | null>(null)
  const myselfPlayer = ref<MyselfPlayerView | null>(null)
  const isLoading = ref(true)

  // Computed
  const isAuthenticated = computed(() => !!user.value)

  // Mutations（状態更新メソッド）
  const setUser = (newUser: User | null) => {
    user.value = newUser
  }

  const setMyselfPlayer = (player: MyselfPlayerView | null) => {
    myselfPlayer.value = player
  }

  const setLoading = (loading: boolean) => {
    isLoading.value = loading
  }

  // 認証トークンの取得
  // Firebase SDK がトークンをメモリに保持し、期限が近づくと自動で更新する
  const getAuthToken = async (): Promise<string | null> => {
    const currentUser = user.value
    if (!currentUser) {
      return null
    }
    try {
      return await currentUser.getIdToken()
    } catch (error) {
      console.error('Failed to get token:', error)
      return null
    }
  }

  return {
    // State
    user,
    myselfPlayer,
    isAuthenticated,
    isLoading,

    // Mutations
    setUser,
    setMyselfPlayer,
    setLoading,

    // Token管理（API呼び出しで使用）
    getAuthToken
  }
})
