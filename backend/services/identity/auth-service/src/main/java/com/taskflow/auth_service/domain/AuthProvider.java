// 
package com.taskflow.auth_service.domain;

/**
 * Account-provider discriminator persisted in the credential document and returned as metadata.
 * Registration and administrator bootstrap currently create LOCAL accounts. GOOGLE and GITHUB
 * are reserved enum values; their presence does not mean OAuth sign-in is implemented.
 */
public enum AuthProvider {
    LOCAL,

    GOOGLE,

    GITHUB

}
