import { describe, expect, it } from 'vitest'
import { getNextQuestionIndex, matchesRule } from './surveyLogic'

describe('surveyLogic', () => {
    it('matches text and numeric operators', () => {
        expect(matchesRule('EQUALS', 'yes', 'yes')).toBe(true)
        expect(matchesRule('CONTAINS', ['Premium', 'Gold'], 'prem')).toBe(true)
        expect(matchesRule('GREATER_THAN', 8, '7')).toBe(true)
        expect(matchesRule('LESS_THAN', 2, '1')).toBe(false)
        expect(matchesRule('NOT_EQUALS', 'no', 'yes')).toBe(true)
        expect(matchesRule('IN', 'Gold', 'silver, gold')).toBe(true)
        expect(matchesRule('BETWEEN', 7, '5, 10')).toBe(true)
    })

    it('branches before skipping hidden questions', () => {
        const rules = [
            { sourceIndex: 0, kind: 'BRANCH' as const, operator: 'EQUALS', value: 'no', targetIndex: 3 },
            { sourceIndex: 0, kind: 'DISPLAY' as const, operator: 'EQUALS', value: 'yes', targetIndex: 1 },
        ]
        expect(getNextQuestionIndex([{}, {}, {}, {}] as never, rules, 0, { '0': 'no' })).toBe(3)
        expect(getNextQuestionIndex([{}, {}, {}, {}] as never, rules, 0, { '0': 'yes' })).toBe(1)
    })

    it('skips questions whose display condition is not met', () => {
        const rules = [
            { sourceIndex: 0, kind: 'DISPLAY' as const, operator: 'EQUALS', value: 'yes', targetIndex: 1 },
        ]
        expect(getNextQuestionIndex([{}, {}, {}] as never, rules, 0, { '0': 'no' })).toBe(2)
    })
})
