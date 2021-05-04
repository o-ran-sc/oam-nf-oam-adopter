/*
 *  ============LICENSE_START=======================================================
 *  O-RAN-SC
 *  ================================================================================
 *  Copyright © 2021 AT&T Intellectual Property. All rights reserved.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ============LICENSE_END=========================================================
 */

package org.o.ran.oam.nf.oam.adopter.app.controller;

import java.util.HashMap;
import java.util.Map;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.exceptions.AlreadyPresentException;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(RestExceptionHandler.class);

    /**
     * Handle Already Present Exceptions.
     */
    @ExceptionHandler({AlreadyPresentException.class})
    public static ResponseEntity<Object> handleBadRequestExceptions(final AlreadyPresentException exception) {
        LOG.error("Request failed", exception);
        return ResponseEntity
                .badRequest()
                .body(exception.getMessage());
    }

    /**
     * Handle Not Found Exceptions.
     */
    @ExceptionHandler({NotFoundException.class})
    public static ResponseEntity<Object> handleNotFoundExceptions(final NotFoundException exception) {
        LOG.error("Request failed", exception);
        return ResponseEntity.notFound().build();
    }

    /**
     * Handle MethodArgument Not Valid Exceptions.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(final MethodArgumentNotValidException ex) {
        final Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            final String fieldName = ((FieldError) error).getField();
            final String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}
