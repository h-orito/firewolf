import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import VillageSituationMessage from '../VillageSituationMessage.vue'

const XSS_NAME = '[A] <img src=x onerror=alert(1)>'

const mockVillage = ref({
  status: { code: 'IN_PROGRESS' },
  silent_time: false,
  setting: {
    rules: { available_suddenly_death: true, available_commit: false },
    charachip: { dummy_chara_id: 1 }
  },
  participant: {
    member_list: [
      { id: 10, dead: null, chara: { id: 2 }, name: XSS_NAME },
      { id: 11, dead: null, chara: { id: 3 }, name: '[B] ふつうの名前' }
    ]
  },
  day: { day_list: [{ day: 2, day_change_datetime: '2026/10/19 00:00' }] }
})

vi.mock('~/composables/village/useVillage', () => ({
  useVillage: () => ({
    village: mockVillage,
    latestDay: ref({ sayable_start_time: null }),
    isCurrentVillageDayLatest: ref(true)
  })
}))

vi.mock('~/composables/village/useMessage', () => ({
  useMessage: () => ({
    messages: ref({ today_message_count_map: {} })
  })
}))

vi.mock('~/composables/village/useUserSettings', () => ({
  useUserSettings: () => ({
    messageDisplay: ref({ isCharLarge: false })
  })
}))

const UiFeedbackAlertStub = {
  name: 'UiFeedbackAlert',
  template: '<div class="alert"><slot /></div>'
}

describe('VillageSituationMessage', () => {
  const mountComponent = () =>
    mount(VillageSituationMessage, {
      global: { stubs: { UiFeedbackAlert: UiFeedbackAlertStub } }
    })

  it('突然死警告の参加者名を HTML として解釈せずテキストで表示する', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('img').exists()).toBe(false)
    expect(wrapper.text()).toContain(XSS_NAME)
    expect(wrapper.html()).toContain('&lt;img src=x onerror=alert(1)&gt;')
  })

  it('未発言者を改行区切りで列挙する', () => {
    const wrapper = mountComponent()
    const spans = wrapper.findAll('span.whitespace-pre-line')
    const suddenlyDeath = spans.find((s) => s.text().includes('突然死します'))

    expect(suddenlyDeath?.element.textContent).toBe(
      `日付更新までに通常発言がない人は突然死します。\n現在まで発言していない人\n${XSS_NAME}\n[B] ふつうの名前`
    )
  })
})
