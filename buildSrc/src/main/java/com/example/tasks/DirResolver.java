package com.example.tasks;

import org.gradle.api.file.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import java.io.*;

public class DirResolver implements URIResolver {

    private final DirectoryProperty baseDir;

    public DirResolver(DirectoryProperty baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public Source resolve(String href, String base) {
        File document = baseDir.file(href).get().getAsFile();
        System.out.println("+ " + document.getName());
        return new StreamSource(document);
    }

}
