import { describe, expect, it } from 'vitest'
import { CHANNELS, DIFFICULTY, MASTERY, RATING, ROUNDS, RESULT, meta } from '@/utils/dict'

describe('dict', () => {
  it('exposes the four difficulty levels', () => {
    expect(Object.keys(DIFFICULTY)).toEqual(['EASY', 'MEDIUM', 'HARD'])
    expect(DIFFICULTY.MEDIUM.label).toBe('中等')
  })

  it('exposes mastery and rating labels', () => {
    expect(MASTERY.MASTERED.label).toBe('长期记忆')
    expect(RATING.GOOD.key).toBe('3')
    expect(RESULT.PASS.label).toBe('通过')
  })

  it('keeps option lists non-empty and unique', () => {
    expect(ROUNDS.length).toBeGreaterThan(0)
    expect(CHANNELS.length).toBeGreaterThan(0)
    expect(new Set(ROUNDS).size).toBe(ROUNDS.length)
  })

  it('returns a fallback for unknown meta keys', () => {
    expect(meta(DIFFICULTY, 'HARD').label).toBe('困难')
    expect(meta(DIFFICULTY, 'UNKNOWN')).toEqual({})
    expect(meta(DIFFICULTY, 'UNKNOWN', { label: '未知' })).toEqual({ label: '未知' })
  })
})
