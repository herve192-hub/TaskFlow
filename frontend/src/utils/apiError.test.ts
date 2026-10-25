/**
 * Unit coverage for API message/detail precedence and fallback behavior with Axios
 * and non-Axios errors. No request is sent; callers remain responsible for a safe,
 * screen-specific fallback.
 */

import { describe, expect, it } from 'vitest'
import { apiErrorMessage } from './apiError'

describe('apiErrorMessage', () => {
  it('prefers the API message when both message and detail are present', () => {
    const error = {
      isAxiosError: true,
      response: { data: { message: 'Access denied', detail: 'Other detail' } },
    }
    expect(apiErrorMessage(error, 'Request failed')).toBe('Access denied')
  })

  it('uses a problem detail when no message is provided', () => {
    const error = { isAxiosError: true, response: { data: { detail: 'Invalid role' } } }
    expect(apiErrorMessage(error, 'Request failed')).toBe('Invalid role')
  })

  it.each([
    new Error('Internal error'),
    null,
    { isAxiosError: true },
    { isAxiosError: true, response: { data: { message: 123 } } },
  ])('uses the fallback for errors without a usable API message: %j', (error) => {
    expect(apiErrorMessage(error, 'Request failed')).toBe('Request failed')
  })
})
