package com.sap.bulletinboard.ads.services;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.netflix.hystrix.Hystrix;
import com.sap.bulletinboard.ads.services.UserServiceClient.User;

/**
 * This is a learning test for Hystrix and to test the GetUserCommand implementation.
 */
public class GetUserCommandTest {
    private static final User FALLBACK_USER = new User();
    private static final User USER = new User();

    @Test
    public void responseReturnedSynchronously() {
        TestableUserCommand command = new TestableUserCommand().responseWithOkUser();
        User user = command.execute();
        assertThat(user, is(USER));
    }

    @Test
    public void responseReturnedAsynchronously() throws Exception {
        TestableUserCommand command = new TestableUserCommand().responseWithOkUser();
        User user = command.queue().get();
        assertThat(user, is(USER));
    }

//    @Test
//    public void responseTimedOutFallback() {
//        TestableUserCommand command = new TestableUserCommand().provokeTimeout();
//        User user = command.execute();
//        assertThat(user, is(not(USER)));
//    }
//
//    @Test
//    public void responseErrorFallback() {
//        TestableUserCommand command = new TestableUserCommand().responseWithError();
//        User user = command.execute();
//        assertThat(user, is(not(USER)));
//    }

    // useful for optional exercise step
    private User dummyUser() {
        return FALLBACK_USER;
    }

    @After
    public void reset() {
        Hystrix.reset(); // to clear out all HystrixPlugins, allowing you to set a new one
    }

    // This command implementation does not send network requests, but instead behaves as configured using the
    // responseWith methods.
    private static class TestableUserCommand extends GetUserCommand {
        private ResponseEntity<User> response;
        private boolean provokeTimeout;

        TestableUserCommand() {
            super("", null);
        }

        TestableUserCommand responseWithOkUser() {
            this.response = ResponseEntity.ok(USER);
            return this;
        }

        TestableUserCommand responseWithError() {
            this.response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            return this;
        }

        TestableUserCommand provokeTimeout() {
            this.provokeTimeout = true;
            return this;
        }

        @Override
        protected ResponseEntity<User> sendRequest() {
            if (provokeTimeout) {
                try {
                    Thread.sleep(getTimeoutInMs() + 100);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            return response;
        }
    }
}