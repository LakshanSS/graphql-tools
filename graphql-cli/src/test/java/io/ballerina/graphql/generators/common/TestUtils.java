/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.graphql.generators.common;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.graphql.cmd.pojo.Config;
import io.ballerina.graphql.cmd.pojo.Extension;
import io.ballerina.graphql.cmd.pojo.Project;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.ParseException;
import org.testng.Assert;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_EMPTY_CONFIGURATION_FILE;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_CONFIGURATION_FILE_CONTENT;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_CONFIGURATION_FILE_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.YAML_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.YML_EXTENSION;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.ROOT_PROJECT_NAME;

/**
 * Common utils for tests.
 */
public class TestUtils {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    private static final String LINE_SEPARATOR = System.lineSeparator();

    //Get string as a content of ballerina file
    public static String getStringFromGivenBalFile(Path expectedFile) throws IOException {
        Stream<String> expectedLines = Files.lines(expectedFile);
        String expectedContent = expectedLines.collect(Collectors.joining(LINE_SEPARATOR));
        expectedLines.close();
        return expectedContent.replaceAll(LINE_SEPARATOR, "");
    }

    public static void compareGeneratedSyntaxTreewithExpectedSyntaxTree(String expectedFilePath, SyntaxTree syntaxTree)
            throws IOException {

        String expectedBallerinaContent = getStringFromGivenBalFile(RES_DIR.resolve(expectedFilePath));
        String generatedSyntaxTree = syntaxTree.toString();

        generatedSyntaxTree = (generatedSyntaxTree.trim()).replaceAll("\\s+", "");
        expectedBallerinaContent = (expectedBallerinaContent.trim()).replaceAll("\\s+", "");
        Assert.assertTrue(generatedSyntaxTree.contains(expectedBallerinaContent));
    }
    public static void compareGeneratedFileWithExpectedFile(String generatedFile, String expectedFile) {
        generatedFile = (generatedFile.trim()).replaceAll("\\s+", "");
        expectedFile = (expectedFile.trim()).replaceAll("\\s+", "");
        Assert.assertTrue(generatedFile.contains(expectedFile));
    }

    /**
     * Constructs an instance of the `Config` reading the given GraphQL config file.
     *
     * @return                              the instance of the Graphql config file
     * @throws FileNotFoundException        when the GraphQL config file doesn't exist
     * @throws ParseException               when a parsing related error occurs
     * @throws CmdException                 when a graphql command related error occurs
     */
    public static Config readConfig(String filePath) throws FileNotFoundException, ParseException, CmdException {
        try {
            if (filePath.endsWith(YAML_EXTENSION) || filePath.endsWith(YML_EXTENSION)) {
                InputStream inputStream = new FileInputStream(new File(filePath));
                Constructor constructor = Utils.getProcessedConstructor();
                Yaml yaml = new Yaml(constructor);
                Config config = yaml.load(inputStream);
                if (config == null) {
                    throw new ParseException(MESSAGE_FOR_EMPTY_CONFIGURATION_FILE);
                }
                return config;
            } else {
                throw new CmdException(MESSAGE_FOR_INVALID_CONFIGURATION_FILE_EXTENSION);
            }
        } catch (YAMLException e) {
            throw new ParseException(MESSAGE_FOR_INVALID_CONFIGURATION_FILE_CONTENT + e.getMessage());
        }
    }

    /**
     * Populate the projects with information given in the GraphQL config file.
     *
     * @param config         the instance of the Graphql config file
     * @return               the list of instances of the GraphQL projects
     */
    public static List<GraphqlProject> populateProjects(Config config) {
        List<GraphqlProject> graphqlProjects = new ArrayList<>();
        String schema = config.getSchema();
        List<String> documents = config.getDocuments();
        Extension extensions = config.getExtensions();
        Map<String, Project> projects = config.getProjects();

        if (schema != null || documents != null || extensions != null) {
            graphqlProjects.add(new GraphqlProject(ROOT_PROJECT_NAME, schema, documents, extensions,
                    ""));
        }

        if (projects != null) {
            for (String projectName : projects.keySet()) {
                graphqlProjects.add(new GraphqlProject(projectName,
                        projects.get(projectName).getSchema(),
                        projects.get(projectName).getDocuments(),
                        projects.get(projectName).getExtensions(),
                        ""));
            }
        }
        return graphqlProjects;
    }
}
