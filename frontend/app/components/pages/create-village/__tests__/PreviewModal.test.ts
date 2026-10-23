import { describe, it, expect, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import PreviewModal from '../PreviewModal.vue'
import type { CreateVillageFormData } from '../types'

const XSS_NAME = '<img src=x onerror=alert(1)>村'

const formData: CreateVillageFormData = {
  villageName: XSS_NAME,
  startDatetime: new Date('2026-10-24T00:00:00+09:00'),
  silentHoursDay1: 0,
  silentHoursDay2: 0,
  charachipIds: [1],
  dummyCharaId: 1,
  dummyCharaName: 'ダミー',
  dummyCharaShortName: 'ダ',
  day0Message: '',
  day1Message: '',
  capacityMin: 10,
  capacityMax: 16,
  organization: '村村村村村村村村村人人狼',
  availableDummySkill: false,
  openVote: false,
  availableSkillRequest: true,
  availableSpectate: false,
  openSkillInGrave: false,
  visibleGraveMessage: false,
  availableSuddenlyDeath: true,
  availableCommit: false,
  availableAction: false,
  availableSecretSay: false,
  availableGuardSameTarget: true,
  normalCount: 20,
  normalLength: 200,
  whisperCount: 20,
  whisperLength: 200,
  sympathizeCount: 20,
  sympathizeLength: 200,
  loversCount: 20,
  loversLength: 200,
  graveCount: 20,
  graveLength: 200,
  monologueCount: 20,
  monologueLength: 200,
  spectateCount: 20,
  spectateLength: 200,
  actionCount: 20,
  actionLength: 200,
  joinPassword: '',
  ageLimit: 'ALL'
}

describe('PreviewModal', () => {
  afterEach(() => {
    document.body.innerHTML = ''
  })

  it('村名を HTML として描画せず、文字としてそのまま表示する', () => {
    mount(PreviewModal, {
      props: { isOpen: true, formData },
      attachTo: document.body
    })

    const villageNameCell = [...document.querySelectorAll('td')].find((td) =>
      td.textContent?.includes('村')
    )
    expect(document.querySelector('img[src="x"]')).toBeNull()
    expect(document.body.textContent).toContain(XSS_NAME)
    expect(villageNameCell).toBeDefined()
  })
})
