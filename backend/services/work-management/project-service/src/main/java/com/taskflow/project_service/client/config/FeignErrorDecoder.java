package com.taskflow.project_service.client.config;

import com.taskflow.project_service.exception.InvalidProjectException;
import com.taskflow.project_service.exception.ProjectAccessDeniedException;
import com.taskflow.project_service.exception.UserServiceUnavailableException;

import feign.Response;
import feign.codec.ErrorDecoder;

public class FeignErrorDecoder
        implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

    @Override
    public Exception decode( String methodKey, Response response ) {

        int status = response.status();
        /*
         * user-service rejected the forwarded authentication.
         */
        if (status == 401 || status == 403) {
            return new ProjectAccessDeniedException( "Access denied by user-service" );
        }
        /*
         * Requested TaskFlow user does not exist.
         */
        if (status == 404) {
            return new InvalidProjectException( "Referenced user does not exist" );
        }
        /*
         * user-service responded, but it is unhealthy or
         * failed while processing the request.
         */
        if (status >= 500) {
            return new UserServiceUnavailableException( "User service is temporarily unavailable" );
        }

        return defaultDecoder.decode( methodKey, response );
    }
}