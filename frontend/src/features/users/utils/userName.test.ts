/**
 * Unit coverage for profile/auth naming conventions, whitespace normalization,
 * full-name handling, and human-readable fallbacks. This suite tests presentation,
 * not account lookup or membership authorization.
 */

import { describe, expect, it } from 'vitest'
import { userDisplayName } from './userName'

describe('userDisplayName', () => {
  it('trims and combines names from the user profile', () => {
    expect(userDisplayName({ firstName: ' Ada ', lastName: ' Lovelace ' })).toBe('Ada Lovelace')
  })

  it('supports the authentication API name fields', () => {
    expect(userDisplayName({ firstname: 'Grace', lastname: 'Hopper' })).toBe('Grace Hopper')
  })

  it('uses a full name when individual names are blank', () => {
    expect(userDisplayName({ firstName: ' ', lastName: '', fullName: ' Alan Turing ' })).toBe('Alan Turing')
  })

  it('handles a profile with only a first name', () => {
    expect(userDisplayName({ firstName: 'Ada' })).toBe('Ada')
  })

  it.each([undefined, null, {}, { firstName: ' ', lastName: null }])(
    'uses the fallback for an unavailable name: %j', (user) => {
      expect(userDisplayName(user, 'Unknown user')).toBe('Unknown user')
    },
  )
})
