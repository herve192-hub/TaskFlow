package com.taskflow.task_service.client.config;

import com.taskflow.task_service.exception.InvalidTaskException;
import com.taskflow.task_service.exception.ProjectServiceUnavailableException;
import com.taskflow.task_service.exception.TaskAccessDeniedException;
import com.taskflow.task_service.exception.UserServiceUnavailableException;

import feign.Response;
import feign.codec.ErrorDecoder;

import org.springframework.stereotype.Component;

@Component
public class FeignErrorDecoder
        implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder =
            new ErrorDecoder.Default();

    @Override
    public Exception decode(
            String methodKey,
            Response response
    ) {

        String service =
                resolveService(methodKey);

        int status =
                response.status();

        if (status == 401 || status == 403) {
            return new TaskAccessDeniedException(
                    "Access denied by " + service
            );
        }

        if (status == 404) {

            if ("project-service".equals(service)) {
                return new InvalidTaskException(
                        "Referenced project does not exist"
                );
            }

            if ("user-service".equals(service)) {
                return new InvalidTaskException(
                        "Referenced user does not exist"
                );
            }
        }

        if (status >= 500) {

            if ("project-service".equals(service)) {
                return new ProjectServiceUnavailableException(
                        "Project service is temporarily unavailable"
                );
            }

            if ("user-service".equals(service)) {
                return new UserServiceUnavailableException(
                        "User service is temporarily unavailable"
                );
            }
        }

        return defaultDecoder.decode(
                methodKey,
                response
        );
    }

    private String resolveService(
            String methodKey
    ) {

        if (methodKey.contains(
                "ProjectServiceClient"
        )) {
            return "project-service";
        }

        if (methodKey.contains(
                "UserServiceClient"
        )) {
            return "user-service";
        }

        return "downstream-service";
    }
}