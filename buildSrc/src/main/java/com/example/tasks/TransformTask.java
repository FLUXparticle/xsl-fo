package com.example.tasks;

import net.sf.saxon.*;
import org.gradle.api.*;
import org.gradle.api.file.*;
import org.gradle.api.tasks.*;
import org.gradle.work.*;

import javax.xml.transform.Transformer;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import java.io.*;
import java.nio.file.*;

public abstract class TransformTask extends DefaultTask {

    private final TransformerFactory transformerFactory = new TransformerFactoryImpl();

    @InputFile
    public abstract RegularFileProperty getXslFile();

    @Incremental
    @PathSensitive(PathSensitivity.NAME_ONLY)
    @InputDirectory
    public abstract DirectoryProperty getInputDir();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void performTask(InputChanges inputChanges) throws IOException, TransformerException {
        RegularFileProperty xslFile = getXslFile();
        DirectoryProperty xmlDir = getInputDir();
        DirectoryProperty outputDir = getOutputDir();

        Iterable<FileChange> fileChanges = inputChanges.getFileChanges(xmlDir);

        for (FileChange fileChange : fileChanges) {
            if (fileChange.getFileType() == FileType.FILE) {
                System.out.println(fileChange);

                File xmlFile = fileChange.getFile();
                String filename = xmlFile.getName();

                File depFile = outputDir.file(filename + ".d").get().getAsFile();

                if (fileChange.getChangeType() == ChangeType.REMOVED) {
                    depFile.delete();
                    System.out.println(depFile.getName() + " gelöscht!");
                    continue;
                }

                System.out.println(xmlFile.getName() + " -> " + depFile.getName());

                Source xsltSource = new StreamSource(xslFile.getAsFile().get());
                Transformer xsltTransformer = transformerFactory.newTransformer(xsltSource);

                // Ausgabestrom für FO-Datei erstellen
                try (OutputStream out = Files.newOutputStream(depFile.toPath())) {
                    // Eingabedatei öffnen
                    Source xmlSource = new StreamSource(xmlFile);

                    // Transformation durchführen
                    xsltTransformer.transform(xmlSource, new StreamResult(out));
                }
            }
        }
    }

}
