package io.protostuff.compiler.maven;

import io.protostuff.compiler.model.ImmutableModuleConfiguration;
import io.protostuff.compiler.model.ModuleConfiguration;
import io.protostuff.generator.CompilerModule;
import io.protostuff.generator.ProtostuffCompiler;
import io.protostuff.generator.html.HtmlGenerator;
import io.protostuff.generator.html.StaticPage;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE_PLUS_RUNTIME;

/**
 * @author Kostiantyn Shchepanovskyi
 */
@Mojo(name = "html",
        configurator = "include-project-dependencies",
        requiresDependencyResolution = COMPILE_PLUS_RUNTIME)
public class HtmlGeneratorMojo extends AbstractGeneratorMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlGeneratorMojo.class);

    @Parameter(defaultValue = "${project.build.directory}/generated-html")
    private File target;

    @Parameter
    private List<StaticPage> pages = new ArrayList<>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();
        ProtostuffCompiler compiler = new ProtostuffCompiler();
        final Path sourcePath = getSourcePath();
        List<String> protoFiles = findProtoFiles(sourcePath);
        ModuleConfiguration moduleConfiguration = ImmutableModuleConfiguration.builder()
                .name("html")
                .includePaths(singletonList(sourcePath))
                .generator(CompilerModule.HTML_COMPILER)
                .output(target.getAbsolutePath())
                .putOptions(HtmlGenerator.PAGES, pages)
                .addAllProtoFiles(protoFiles)
                .build();
        compiler.compile(moduleConfiguration);
    }


}
