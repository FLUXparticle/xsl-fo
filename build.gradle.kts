import com.example.tasks.*

val findDependencies by tasks.registering(TransformTask::class) {
    xslFile = file("dependencies.xsl")
    inputDir = file("xsl")
    outputDir = file("dep")
}

val convertXMLtoFO by tasks.registering(ConvertXMLtoFOTask::class) {
    dependsOn(findDependencies)
    xslDir = file("xsl")
    depDir = file("dep")
    xmlDir = file("xml")
    outputDir = file("fo")
}

val convertFOtoPDF by tasks.registering(ConvertFOtoPDFTask::class) {
    dependsOn(convertXMLtoFO)
    inputDir = file("fo")
    imageDir = file("images")
    outputDir = file("pdf")
}
