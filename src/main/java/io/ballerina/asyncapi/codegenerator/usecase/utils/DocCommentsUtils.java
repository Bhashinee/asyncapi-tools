/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.asyncapi.codegenerator.usecase.utils;

import io.ballerina.compiler.syntax.tree.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.*;
import static io.ballerina.compiler.syntax.tree.NodeFactory.*;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.*;

/**
 * This class util for maintain the API doc comment related functions.
 */
public class DocCommentsUtils {
    /**
     * Extract extension for find the display annotation.
     *
     * @param extensions - openapi extension.
     * @return Annotation node list.
     */
    public NodeList<AnnotationNode> extractDisplayAnnotation(Map<String, Object> extensions) {
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        if (extensions != null) {
            for (Map.Entry<String, Object> extension : extensions.entrySet()) {
                if (extension.getKey().trim().equals("x-display")) {
                    AnnotationNode annotationNode = getAnnotationNode(extension);
                    annotationNodes = createNodeList(annotationNode);
                }
            }
        }
        return annotationNodes;
    }

    private AnnotationNode getAnnotationNode(Map.Entry<String, Object> extension) {

        LinkedHashMap<String, String> extFields = (LinkedHashMap<String, String>) extension.getValue();
        List<Node> annotFields = new ArrayList<>();
        if (!extFields.isEmpty()) {
            for (Map.Entry<String, String> field : extFields.entrySet()) {

                BasicLiteralNode valueExpr = createBasicLiteralNode(STRING_LITERAL,
                        createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,
                                '"' + field.getValue().trim() + '"',
                                createEmptyMinutiaeList(),
                                createEmptyMinutiaeList()));
                SpecificFieldNode fields = createSpecificFieldNode(null,
                        createIdentifierToken(field.getKey().trim()),
                        createToken(COLON_TOKEN), valueExpr);
                annotFields.add(fields);
                annotFields.add(createToken(COMMA_TOKEN));
            }
            if (annotFields.size() == 2) {
                annotFields.remove(1);
            }
        }

        MappingConstructorExpressionNode annotValue = createMappingConstructorExpressionNode(
                createToken(OPEN_BRACE_TOKEN), createSeparatedNodeList(annotFields),
                createToken(CLOSE_BRACE_TOKEN));

        SimpleNameReferenceNode annotateReference =
                createSimpleNameReferenceNode(createIdentifierToken("display"));

        return createAnnotationNode(createToken(SyntaxKind.AT_TOKEN)
                , annotateReference, annotValue);
    }


    /**
     * Generate metaDataNode with display annotation.
     */
    public MetadataNode getMetadataNodeForDisplayAnnotation(Map.Entry<String, Object> extension) {

        MetadataNode metadataNode;
        AnnotationNode annotationNode = getAnnotationNode(extension);
        metadataNode = createMetadataNode(null, createNodeList(annotationNode));
        return metadataNode;
    }

    public List<MarkdownDocumentationLineNode> createAPIDescriptionDoc(
            String description, boolean addExtraLine) {
        String[] descriptionLines = description.split("\n");
        List<MarkdownDocumentationLineNode> documentElements = new ArrayList<>();
        for (String line : descriptionLines) {
            MarkdownDocumentationLineNode documentationLineNode =
                    createMarkdownDocumentationLineNode(DOCUMENTATION_DESCRIPTION,
                            createToken(SyntaxKind.HASH_TOKEN), createNodeList(createIdentifierToken(line)));
            documentElements.add(documentationLineNode);
        }
        if (addExtraLine) {
            MarkdownDocumentationLineNode newLine = createMarkdownDocumentationLineNode(null,
                    createToken(SyntaxKind.HASH_TOKEN), createEmptyNodeList());
            documentElements.add(newLine);
        }
        return documentElements;
    }

    public MarkdownParameterDocumentationLineNode createAPIParamDoc(String paramName, String description) {
        String[] paramDescriptionLines = description.split("\n");
        List<Node> documentElements = new ArrayList<>();
        for (String line : paramDescriptionLines) {
            if (!line.isBlank()) {
                documentElements.add(createIdentifierToken(line + " "));
            }
        }
        return createMarkdownParameterDocumentationLineNode(null, createToken(SyntaxKind.HASH_TOKEN),
                createToken(SyntaxKind.PLUS_TOKEN), createIdentifierToken(paramName),
                createToken(SyntaxKind.MINUS_TOKEN), createNodeList(documentElements));
    }
}

