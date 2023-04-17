// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/websocket;
import 'service.types;


@websocket:ServiceConfig{dispatcherKey: "event"}
service /payloadV on new websocket:Listener(8080) {
    # List all products
    # + return - List of products
    resource function get .() returns websocket:Service|websocket:UpgradeError {
        return new ArrayChatServer();
    }
}


service class ArrayChatServer{

    *websocket:Service;
    private map<types:Product> products = {};

    remote function onPrice(types:Price price) returns types:Product[]|stream<int> {
        
        return self.products.toArray();
    }
}