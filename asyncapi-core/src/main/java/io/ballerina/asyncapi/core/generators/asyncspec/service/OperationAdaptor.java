/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package io.ballerina.asyncapi.core.generators.asyncspec.service;

import io.swagger.v3.oas.models.Operation;

/**
 * This class will hold operation details specific to HTTP operation.
 */
public class OperationAdaptor {

    private Operation operation;
    private String path;
    private String httpOperation;

    public OperationAdaptor() {
        this.operation = new io.swagger.v3.oas.models.Operation();
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        if (!path.startsWith("/")) {
            this.path = "/" + path;
        } else {
            this.path = path;
        }
    }

    public String getHttpOperation() {
        return httpOperation;
    }

    public void setHttpOperation(String httpOperation) {
        this.httpOperation = httpOperation;
    }

}
