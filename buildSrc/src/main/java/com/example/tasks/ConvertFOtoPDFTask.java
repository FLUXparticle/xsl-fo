package com.example.tasks;

import net.sf.saxon.*;
import org.apache.fop.apps.*;
import org.gradle.api.*;
import org.gradle.api.file.*;
import org.gradle.api.tasks.*;
import org.gradle.work.*;
import org.xml.sax.*;

import javax.xml.transform.Transformer;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;

public abstract class ConvertFOtoPDFTask extends DefaultTask {

    // TransformerFactory für Saxon erstellen
    private final TransformerFactory transformerFactory = new TransformerFactoryImpl();

    @Incremental
    @PathSensitive(PathSensitivity.NAME_ONLY)
    @InputDirectory
    public abstract DirectoryProperty getInputDir();

    @InputDirectory
    public abstract DirectoryProperty getImageDir();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    void performTask(InputChanges inputChanges) throws IOException, SAXException, TransformerException {
        DirectoryProperty inputDir = getInputDir();
        DirectoryProperty imageDir = getImageDir();
        DirectoryProperty outputDir = getOutputDir();

        // FOP-Konfiguration erstellen
        FopFactory fopFactory = FopFactory.newInstance(imageDir.getAsFile().get().toURI());
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

        Transformer transformer = transformerFactory.newTransformer();

        for (FileChange fileChange : inputChanges.getFileChanges(inputDir)) {
            if (fileChange.getFileType() == FileType.FILE) {
                System.out.println(fileChange);

                File foFile = fileChange.getFile();
                String filename = foFile.getName();
                String basename = filename.substring(0, filename.lastIndexOf('.', filename.lastIndexOf('.')-1));

                File pdfFile = outputDir.file(basename + ".pdf").get().getAsFile();

                System.out.println(foFile.getName() + " -> " + pdfFile.getName());

                try (OutputStream out = Files.newOutputStream(pdfFile.toPath())) {
                    // Ausgabestrom für PDF-Datei erstellen
                    Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

                    // FO-Datei öffnen
                    Source foSource = new StreamSource(foFile);

                    // Transformation durchführen und in PDF umwandeln
                    transformer.transform(foSource, new SAXResult(fop.getDefaultHandler()));
                }

                System.out.println("Öffne " + pdfFile.getName());
                Desktop.getDesktop().browse(pdfFile.toURI());
            }
        }
    }

}
