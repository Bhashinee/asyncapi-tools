/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.asyncapi.cmd;

import io.ballerina.asyncapi.cli.AsyncAPICmd;
import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.asyncapi.core.exception.BallerinaAsyncApiException;
import org.apache.commons.io.FileUtils;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * OpenAPI command test suit.
 */
public class AsyncAPICmdTest extends AsyncAPICommandTest {
    private static final String LINE_SEPARATOR = System.lineSeparator();

    @BeforeTest(description = "This will create a new ballerina project for testing below scenarios.")
    public void setupBallerinaProject() throws IOException {
        super.setup();
    }

    @Test(description = "Test asyncapi command with help flag")
    public void testOpenAPICmdHelp() throws IOException {
        String[] args = {"-h"};
        AsyncAPICmd openApiCommand = new AsyncAPICmd(printStream, tmpDir, false);
        new CommandLine(openApiCommand).parseArgs(args);
        openApiCommand.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("NAME\n" +
                "       ballerina-asyncapi - Generate a Ballerina service"));
    }

    @Test(description = "Test asyncapi command without help flag")
    public void testOpenAPICmdHelpWithoutFlag() throws IOException {
        AsyncAPICmd openApiCommand = new AsyncAPICmd(printStream, tmpDir, false);
        new CommandLine(openApiCommand);
        openApiCommand.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("NAME\n" +
                "       ballerina-asyncapi - Generate a Ballerina service"));
    }

    @Test(description = "Test asyncapi gen-service without asyncapi contract file")
    public void testWithoutOpenApiContract() throws IOException {
        String[] args = {"--input"};
        AsyncAPICmd cmd = new AsyncAPICmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("An AsyncAPI definition path is required to generate the service."));
    }

    @Test(description = "Test asyncapi gen-service for successful service generation")
    public void testSuccessfulServiceGeneration() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore.yaml"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString()};
        AsyncAPICmd cmd = new AsyncAPICmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedSchemaFile = resourceDir.resolve(Paths.get("yaml_outputs", "petstore_schema.bal"));
        String expectedSchemaContent = "";
        try (Stream<String> expectedSchemaLines = Files.lines(expectedSchemaFile)) {
            expectedSchemaContent = expectedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                Files.exists(this.tmpDir.resolve("petstore_service.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal"))) {
            //Compare schema contents
            String generatedSchema = "";
            try (Stream<String> generatedSchemaLines = Files.lines(this.tmpDir.resolve("types.bal"))) {
                generatedSchema = generatedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            generatedSchema = (generatedSchema.trim()).replaceAll("\\s+", "");
            expectedSchemaContent = (expectedSchemaContent.trim()).replaceAll("\\s+", "");
            if (expectedSchemaContent.equals(generatedSchema)) {
                Assert.assertTrue(true);
                deleteGeneratedFiles(false);
            } else {
                Assert.fail("Expected content and actual generated content is mismatched for: "
                        + petstoreYaml.toString());
                deleteGeneratedFiles(false);
            }
        } else {
            Assert.fail("Service generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Check the type content in asyncapi-to-ballerina command when using to generate both " +
            "client and service")
    public void testSuccessfulTypeBalGeneration() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore_type.yaml"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString()};
        AsyncAPICmd cmd = new AsyncAPICmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedSchemaFile = resourceDir.resolve(Paths.get("yaml_outputs", "petstore_schema_type.bal"));
        String expectedSchemaContent = "";
        try (Stream<String> expectedSchemaLines = Files.lines(expectedSchemaFile)) {
            expectedSchemaContent = expectedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                Files.exists(this.tmpDir.resolve("petstore_type_service.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal"))) {
            //Compare schema contents
            String generatedSchema = "";
            try (Stream<String> generatedSchemaLines = Files.lines(this.tmpDir.resolve("types.bal"))) {
                generatedSchema = generatedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            generatedSchema = (generatedSchema.trim()).replaceAll("\\s+", "");
            expectedSchemaContent = (expectedSchemaContent.trim()).replaceAll("\\s+", "");
            if (expectedSchemaContent.equals(generatedSchema)) {
                Assert.assertTrue(true);
            } else {
                Assert.fail("Expected content and actual generated content is mismatched for: "
                        + petstoreYaml.toString());
            }
            deleteGeneratedFiles(false);
        } else {
            Assert.fail("Type generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test asyncapi to ballerina generation with license headers")
    public void testGenerationWithLicenseHeaders() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore.yaml"));
        Path licenseHeader = resourceDir.resolve(Paths.get("license.txt"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString(), "--license",
                licenseHeader.toString()};
        AsyncAPICmd cmd = new AsyncAPICmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedSchemaFile = resourceDir.resolve(Paths.get("yaml_outputs",
                "petstore_schema_with_license.bal"));
        String expectedSchemaContent = "";
        try (Stream<String> expectedSchemaLines = Files.lines(expectedSchemaFile)) {
            expectedSchemaContent = expectedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                Files.exists(this.tmpDir.resolve("petstore_service.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal"))) {
            //Compare schema contents
            String generatedSchema = "";
            try (Stream<String> generatedSchemaLines = Files.lines(this.tmpDir.resolve("types.bal"))) {
                generatedSchema = generatedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            generatedSchema = (generatedSchema.trim()).replaceAll("\\s+", "");
            expectedSchemaContent = (expectedSchemaContent.trim()).replaceAll("\\s+", "");
            if (expectedSchemaContent.equals(generatedSchema)) {
                Assert.assertTrue(true);
                deleteGeneratedFiles(false);
            } else {
                Assert.fail("Expected content and actual generated content is mismatched for: "
                        + petstoreYaml.toString());
                deleteGeneratedFiles(false);
            }
        } else {
            Assert.fail("Code generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test asyncapi to ballerina generation with no new line license headers")
    public void testGenerationWithLicenseHeadersWithOneNewLine() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore.yaml"));
        Path licenseHeader = resourceDir.resolve(Paths.get("license_with_new_line.txt"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString(), "--license",
                licenseHeader.toString()};
        AsyncAPICmd cmd = new AsyncAPICmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedSchemaFile = resourceDir.resolve(Paths.get("yaml_outputs",
                "generated_client_with_license.bal"));
        String expectedClientContent = "";
        try (Stream<String> expectedSchemaLines = Files.lines(expectedSchemaFile)) {
            expectedClientContent = expectedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                Files.exists(this.tmpDir.resolve("petstore_service.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal"))) {
            //Compare schema contents
            String generatedClientContent = "";
            try (Stream<String> generatedClientLines = Files.lines(this.tmpDir.resolve("client.bal"))) {
                generatedClientContent = generatedClientLines.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            generatedClientContent = (generatedClientContent.trim()).replaceAll("\\s+", "");
            expectedClientContent = (expectedClientContent.trim()).replaceAll("\\s+", "");
            if (expectedClientContent.equals(generatedClientContent)) {
                Assert.assertTrue(true);
                deleteGeneratedFiles(false);
            } else {
                Assert.fail("Expected content and actual generated content is mismatched for: "
                        + petstoreYaml.toString());
                deleteGeneratedFiles(false);
            }
        } else {
            Assert.fail("Code generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test asyncapi to ballerina connector generation filtering by tags")
    public void testConnectorGenerationFilteringByTags() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore_tags.yaml"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString(), "--tags",
                "pets,dogs", "--mode", "client", "--client-methods", "remote"};
        AsyncAPICmd cmd = new AsyncAPICmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedClientFile = resourceDir.resolve(Paths.get("yaml_outputs",
                "client_filtered_by_tags.bal"));
        String expectedClientContent = "";
        try (Stream<String> expectedSchemaLines = Files.lines(expectedClientFile)) {
            expectedClientContent = expectedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                Files.exists(this.tmpDir.resolve("utils.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal"))) {
            //Compare client contents
            String generatedClientContent = "";
            try (Stream<String> generatedClientLines = Files.lines(this.tmpDir.resolve("client.bal"))) {
                generatedClientContent = generatedClientLines.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            generatedClientContent = (generatedClientContent.trim()).replaceAll("\\s+", "");
            expectedClientContent = (expectedClientContent.trim()).replaceAll("\\s+", "");
            if (expectedClientContent.equals(generatedClientContent)) {
                Assert.assertTrue(true);
                deleteGeneratedFiles(false);
            } else {
                Assert.fail("Expected content and actual generated content is mismatched for: "
                        + petstoreYaml.toString());
                deleteGeneratedFiles(false);
            }
        } else {
            Assert.fail("Code generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test asyncapi to ballerina generation with license headers and test suit")
    public void testGenerationOfTestSuiteWithLicenseHeaders() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore_with_oauth.yaml"));
        Path licenseHeader = resourceDir.resolve(Paths.get("license.txt"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString(), "--license",
                licenseHeader.toString(), "--with-tests"};
        AsyncAPICmd cmd = new AsyncAPICmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedConfigFilePath = resourceDir.resolve(Paths.get("yaml_outputs",
                "bearer_config.toml"));
        String expectedConfig = "";
        try (Stream<String> expectedSchemaLines = Files.lines(expectedConfigFilePath)) {
            expectedConfig = expectedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                Files.exists(this.tmpDir.resolve("petstore_with_oauth_service.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal")) &&
                Files.exists(this.tmpDir.resolve("tests/Config.toml")) &&
                Files.exists(this.tmpDir.resolve("tests/test.bal"))) {
            //Compare schema contents
            String generatedConfig = "";
            try (Stream<String> configContent = Files.lines(this.tmpDir.resolve("tests/Config.toml"))) {
                generatedConfig = configContent.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            generatedConfig = (generatedConfig.trim()).replaceAll("\\s+", "");
            expectedConfig = (expectedConfig.trim()).replaceAll("\\s+", "");
            if (expectedConfig.equals(generatedConfig)) {
                Assert.assertTrue(true);
                deleteGeneratedFiles(true);
            } else {
                Assert.fail("Expected content and actual generated content is mismatched for: "
                        + petstoreYaml.toString());
                deleteGeneratedFiles(true);
            }
        } else {
            Assert.fail("Code generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test exception when invalid prefix file given")
    public void testInvalidPrefixFile() throws IOException, BallerinaAsyncApiException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore.yaml"));
        Path licenseHeader = resourceDir.resolve(Paths.get("licence.txt"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString(), "--license",
                licenseHeader.toString()};
        AsyncAPICmd cmd = new AsyncAPICmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("Invalid license file path : "));
    }

    @Test(description = "Test generation without including test files")
    public void testClientGenerationWithoutIncludeTestFilesOption() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore_with_oauth.yaml"));
        Path licenseHeader = resourceDir.resolve(Paths.get("license.txt"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString(), "--license",
                licenseHeader.toString()};
        AsyncAPICmd cmd = new AsyncAPICmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedConfigFilePath = resourceDir.resolve(Paths.get("yaml_outputs",
                "bearer_config.toml"));
        String expectedConfig = "";
        try (Stream<String> expectedSchemaLines = Files.lines(expectedConfigFilePath)) {
            expectedConfig = expectedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                Files.exists(this.tmpDir.resolve("petstore_with_oauth_service.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal")) &&
                !Files.exists(this.tmpDir.resolve("tests/Config.toml")) &&
                !Files.exists(this.tmpDir.resolve("tests/test.bal"))) {

                Assert.assertTrue(true);
                deleteGeneratedFiles(true);
        } else {
            Assert.fail("Code generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test asyncapi gen-service for .yml file service generation")
    public void testSuccessfulServiceGenerationForYML() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore.yml"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString()};
        AsyncAPICmd cmd = new AsyncAPICmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        try {
            cmd.execute();
        } catch (BLauncherException e) {
        }
        Path expectedSchemaFile = resourceDir.resolve(Paths.get("yaml_outputs", "petstore_schema.bal"));
        String expectedSchemaContent = "";
        try (Stream<String> expectedSchemaLines = Files.lines(expectedSchemaFile)) {
            expectedSchemaContent = expectedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                Files.exists(this.tmpDir.resolve("petstore_service.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal"))) {
            //Compare schema contents
            String generatedSchema = "";
            try (Stream<String> generatedSchemaLines = Files.lines(this.tmpDir.resolve("types.bal"))) {
                generatedSchema = generatedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            generatedSchema = (generatedSchema.trim()).replaceAll("\\s+", "");
            expectedSchemaContent = (expectedSchemaContent.trim()).replaceAll("\\s+", "");
            if (expectedSchemaContent.equals(generatedSchema)) {
                Assert.assertTrue(true);
                deleteGeneratedFiles(false);
            } else {
                Assert.fail("Expected content and actual generated content is mismatched for: "
                        + petstoreYaml.toString());
                deleteGeneratedFiles(false);
            }
        } else {
            Assert.fail("Service generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test for service generation with yaml contract without operationID")
    public void testForYamlContractWithoutOperationID() throws IOException {
        Path yamlContract = resourceDir.resolve(Paths.get("without_operationID.yaml"));
        String[] args = {"--input", yamlContract.toString(), "-o", this.tmpDir.toString(), "--mode", "service"};
        AsyncAPICmd cmd = new AsyncAPICmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        if (Files.exists(this.tmpDir.resolve("without_operationid_service.bal"))) {
            Assert.assertTrue(true);
            File schemaFile = new File(this.tmpDir.resolve("types.bal").toString());
            File serviceFile = new File(this.tmpDir.resolve("without_operationid_service.bal").toString());
            serviceFile.delete();
            schemaFile.delete();
        } else {
            Assert.fail("Code generation failed. : " + readOutput(true));
        }
    }

    // Delete the generated files
    private void deleteGeneratedFiles(boolean isConfigGenerated) throws IOException {
        File serviceFile = new File(this.tmpDir.resolve("petstore_service.bal").toString());
        File clientFile = new File(this.tmpDir.resolve("client.bal").toString());
        File schemaFile = new File(this.tmpDir.resolve("types.bal").toString());
        File testFile = new File(this.tmpDir.resolve("tests/test.bal").toString());
        File testDir = new File(this.tmpDir.resolve("tests").toString());
        serviceFile.delete();
        clientFile.delete();
        schemaFile.delete();
        testFile.delete();
        if (isConfigGenerated) {
            File configFile = new File(this.tmpDir.resolve("tests/Config.toml").toString());
            configFile.delete();
        }
        FileUtils.deleteDirectory(testDir);
    }

    @Test(description = "getRelative path")
    public void getRelativePath() {
        AsyncAPICmd cmd = new AsyncAPICmd();
        File resource01 = new File("dir1/test.txt");
        String target01 = "dir1/dir2";
        File resource02 = new File("dir1/dir2/dir3/test.txt");
        String target02 = "dir1/dir2";
        File resource03 = new File("dir2/dir3/dir4/test.txt");
        String target03 = "dir/dir1";
        Assert.assertTrue((cmd.getRelativePath(resource01, target01).toString()).equals("../test.txt") ||
                (cmd.getRelativePath(resource01, target01).toString()).equals("..\\test.txt"));
        Assert.assertTrue((cmd.getRelativePath(resource02, target02).toString()).equals("dir3/test.txt") ||
                (cmd.getRelativePath(resource02, target02).toString()).equals("dir3\\test.txt"));
        Assert.assertTrue((cmd.getRelativePath(resource03, target03).toString()).
                equals("../../dir2/dir3/dir4/test.txt") || (cmd.getRelativePath(resource03, target03).toString()).
                equals("..\\..\\dir2\\dir3\\dir4\\test.txt"));
    }

    @AfterTest
    public void clean() {
        System.setErr(null);
        System.setOut(null);
    }
}
