import type { LogicRule, SurveyQuestion } from '../types'

export function matchesRule(
    operator: string,
    answer: unknown,
    expected: string,
): boolean {
    if (answer === undefined || answer === null) return false

    const values = Array.isArray(answer) ? answer.map(String) : [String(answer)]

    if (operator === 'CONTAINS') {
        return values.some((value) =>
            value.toLowerCase().includes(expected.toLowerCase()),
        )
    }

    if (operator === 'EQUALS') {
        return values.some((value) => value.trim().toLowerCase() === expected.trim().toLowerCase())
    }
    if (operator === 'NOT_EQUALS') {
        return values.every((value) => value.trim().toLowerCase() !== expected.trim().toLowerCase())
    }
    if (operator === 'NOT_CONTAINS') {
        return values.every((value) => !value.toLowerCase().includes(expected.toLowerCase()))
    }
    if (operator === 'IS_EMPTY') return values.every((value) => value.trim() === '')
    if (operator === 'IS_NOT_EMPTY') return values.some((value) => value.trim() !== '')
    if (operator === 'IN') {
        const expectedValues = expected.split(',').map(value => value.trim().toLowerCase()).filter(Boolean)
        return values.some(value => expectedValues.includes(value.trim().toLowerCase()))
    }
    if (operator === 'NOT_IN') {
        const expectedValues = expected.split(',').map(value => value.trim().toLowerCase()).filter(Boolean)
        return values.every(value => !expectedValues.includes(value.trim().toLowerCase()))
    }

    const actualNumber = Number(values[0])
    if (Number.isNaN(actualNumber)) return false
    if (operator === 'BETWEEN') {
        const [minimum, maximum] = expected.split(',').map(value => Number(value.trim()))
        return !Number.isNaN(minimum) && !Number.isNaN(maximum) && actualNumber >= minimum && actualNumber <= maximum
    }
    const expectedNumber = Number(expected)
    if (Number.isNaN(expectedNumber)) return false
    if (operator === 'GREATER_THAN') return actualNumber > expectedNumber
    if (operator === 'LESS_THAN') return actualNumber < expectedNumber
    if (operator === 'GREATER_OR_EQUAL') return actualNumber >= expectedNumber
    if (operator === 'LESS_OR_EQUAL') return actualNumber <= expectedNumber
    return false
}

export function getNextQuestionIndex(
    questions: SurveyQuestion[],
    rules: LogicRule[],
    currentIndex: number,
    answers: Record<string, unknown>,
): number {
    const answer = answers[String(currentIndex)]
    const applicableRules = rules
        .filter(
            (rule) =>
                rule.sourceIndex === currentIndex &&
                matchesRule(rule.operator, answer, rule.value),
        )
        .sort((left, right) => {
            if (left.kind === right.kind) return 0
            return left.kind === 'BRANCH' ? -1 : 1
        })

    for (const rule of applicableRules) {
        if (rule.kind === 'BRANCH') {
            const target = clampQuestionIndex(rule.targetIndex, questions.length)
            return findNextVisibleQuestion(target, questions.length, rules, answers)
        }
    }

    let next = currentIndex + 1
    while (next < questions.length && !isQuestionVisible(next, rules, answers)) {
        next += 1
    }
    return next
}

function findNextVisibleQuestion(start: number, length: number, rules: LogicRule[], answers: Record<string, unknown>): number {
    let index = start
    while (index < length && !isQuestionVisible(index, rules, answers)) index += 1
    return index
}

function clampQuestionIndex(index: number, length: number): number {
    return Math.max(0, Math.min(index, length))
}

function isQuestionVisible(
    questionIndex: number,
    rules: LogicRule[],
    answers: Record<string, unknown>,
): boolean {
    const displayRules = rules.filter(
        (rule) => rule.kind === 'DISPLAY' && rule.targetIndex === questionIndex,
    )
    if (displayRules.length === 0) return true

    return displayRules.some((rule) =>
        matchesRule(
            rule.operator,
            answers[String(rule.sourceIndex)],
            rule.value,
        ),
    )
}
