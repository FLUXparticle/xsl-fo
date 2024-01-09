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
import java.util.*;

import static java.util.Collections.*;

public abstract class ConvertXMLtoFOTask extends DefaultTask {

    private final TransformerFactory transformerFactory = new TransformerFactoryImpl();

    @Incremental
    @PathSensitive(PathSensitivity.NAME_ONLY)
    @InputDirectory
    public abstract DirectoryProperty getXslDir();

    @InputDirectory
    public abstract DirectoryProperty getDepDir();

    @InputDirectory
    public abstract DirectoryProperty getXmlDir();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void performTask(InputChanges inputChanges) throws IOException, TransformerException {
        DirectoryProperty xslDir = getXslDir();
        DirectoryProperty depDir = getDepDir();

        Map<String, Set<String>> triggers = new HashMap<>();

        File[] depFiles = depDir.getAsFile().get().listFiles();
        for (File depFile : depFiles) {
            String depFilename = depFile.getName();
            String filename = depFilename.substring(0, depFilename.lastIndexOf('.'));
            List<String> lines = Files.readAllLines(depFile.toPath());
            for (String line : lines) {
                Set<String> set = triggers.computeIfAbsent(line, key -> new HashSet<>());
                set.add(filename);
            }
        }

        Iterable<FileChange> fileChanges = inputChanges.getFileChanges(xslDir);

        for (FileChange fileChange : fileChanges) {
            if (fileChange.getFileType() == FileType.FILE) {
                System.out.println(fileChange);

                File xslFile = fileChange.getFile();
                boolean wasRemoved = fileChange.getChangeType() == ChangeType.REMOVED;

                transform(xslFile, wasRemoved, triggers);
            }
        }
    }

    private void transform(File xslFile, boolean wasRemoved, Map<String, Set<String>> triggers) throws IOException, TransformerException {
        DirectoryProperty xmlDir = getXmlDir();
        DirectoryProperty outputDir = getOutputDir();

        String filename = xslFile.getName();
        String basename = filename.substring(0, filename.lastIndexOf('.'));

        File xmlFile = xmlDir.file(basename + ".xml").get().getAsFile();
        File foFile = outputDir.file(basename + ".fo.xml").get().getAsFile();

        if (wasRemoved) {
            foFile.delete();
            System.out.println(foFile.getName() + " gelöscht!");
            return;
        }

        if (!xmlFile.exists()) {
            System.out.println(xmlFile.getName() + " nicht gefunden!");
            return;
        }

        System.out.println(xmlFile.getName() + " + " + xslFile.getName() + " -> " + foFile.getName());

        Source xsltSource = new StreamSource(xslFile);
        Transformer xsltTransformer = transformerFactory.newTransformer(xsltSource);
        xsltTransformer.setURIResolver(new DirResolver(xmlDir));

        // Ausgabestrom für FO-Datei erstellen
        try (OutputStream out = Files.newOutputStream(foFile.toPath())) {
            // Eingabedatei öffnen
            Source xmlSource = new StreamSource(xmlFile);

            // Transformation durchführen
            xsltTransformer.transform(xmlSource, new StreamResult(out));
        }

        for (String dep : triggers.getOrDefault(filename, emptySet())) {
            File depXslFile = new File(xslFile.getParentFile(), dep);
            transform(depXslFile, false, triggers);
        }
    }

}
