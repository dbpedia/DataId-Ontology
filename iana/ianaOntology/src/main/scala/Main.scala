import java.io.{ByteArrayOutputStream, PrintWriter}
import java.net.URL
import java.util

import better.files.File
import org.apache.commons.io.FileUtils
import org.apache.jena.rdf.model.{Model, ModelFactory, ResourceFactory}
import org.apache.jena.riot.{RDFDataMgr, RDFFormat}

import scala.io.{Codec, Source}
import scala.xml.{Node, XML}
import net.lingala.zip4j.core.ZipFile
import org.apache.jena.graph.NodeFactory


object Main {
  val dbpediaURL_Part = "http://dataid.dbpedia.org/ns/iana#"

  def main(args: Array[String]): Unit = {

    println("ONTOLOGY CREATION TOOL")

    val ontologyFile = File("./ontology/ianaOntology.ttl")
    var model = ModelFactory.createDefaultModel()


    val ianaURL = "https://www.iana.org/assignments/media-types/media-types.xml"
    readIanaXML(model, ianaURL)

    val sparDownloadURL = new URL("https://ndownloader.figshare.com/files/2447048")
    readSparOntology(model, sparDownloadURL)

    createRDFTypeTriples(model)
    createRDFLabelTriples(model)


    var prefixMap = new util.HashMap[String, String]()
    prefixMap.put("rdf", "http://www.w3.org/2000/01/rdf-schema#")
    prefixMap.put("owl", "http://www.w3.org/2002/07/owl#")
    prefixMap.put("prov", "http://www.w3.org/ns/prov#")
    prefixMap.put("dbpedia", "http://dataid.dbpedia.org/ns/iana#")

    model.setNsPrefixes(prefixMap)

    writeOntologyFile(model, ontologyFile)
  }

  def createRDFTypeTriples(model: Model): Unit = {
    val sub_list = model.listSubjects()

    while (sub_list.hasNext) {
      val sub = sub_list.nextResource().getURI

      val stmt = ResourceFactory.createStatement(
        ResourceFactory.createResource(sub),
        ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
        ResourceFactory.createResource(sub.splitAt(sub.lastIndexOf("/"))._1)
      )

      model.add(stmt)
    }
  }

  def createRDFLabelTriples(model: Model): Unit = {
    val sub_list = model.listSubjects()

    while (sub_list.hasNext) {
      val sub = sub_list.nextResource().getURI

      val stmt = ResourceFactory.createStatement(
        ResourceFactory.createResource(sub),
        ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label"),
        ResourceFactory.createStringLiteral(sub.splitAt(sub.lastIndexOf("#") + 1)._2)
      )

      model.add(stmt)
    }
  }

  def writeOntologyFile(model: Model, file: File): Unit = {
    val os = new ByteArrayOutputStream()
    RDFDataMgr.write(os, model, RDFFormat.TURTLE)
    val ontologyString = Source.fromBytes(os.toByteArray)(Codec.UTF8).getLines().mkString("", "\n", "\n")

    new PrintWriter((file).pathAsString) {
      write(ontologyString)
      close
    }

    println(s"Ontology file has been written to ${file.pathAsString}")
  }

  def readIanaXML(model: Model, ianaURL: String) = {

    val ianaXML = XML.load(ianaURL)

    val ianaURL_Part = s"${ianaXML.namespace}/${ianaXML.attribute("id").mkString}/"
    val ianaMimeTypes = (ianaXML \\ "registry" \\ "record" \ "file")

    ianaMimeTypes.foreach(ianaMimeType => {
      createIanaMimeTypeTriple(ianaMimeType, ianaURL_Part, dbpediaURL_Part, model)
    })

  }

  def createIanaMimeTypeTriple(node: Node, ianaURL_Part: String, dbpediaURL_Part: String, model: Model): Unit = {

    val stmt = ResourceFactory.createStatement(
      ResourceFactory.createResource(dbpediaURL_Part.concat(node.text)),
      ResourceFactory.createProperty("http://www.w3.org/ns/prov#wasDerivedFrom"),
      ResourceFactory.createResource(ianaURL_Part.concat(node.text))
    )
    model.add(stmt)
  }


  def readSparOntology(model: Model, sparDownloadURL: URL) = {

    val ontologyZipFile = File("./ontology/temp/sparOntology.zip")
    FileUtils.copyURLToFile(sparDownloadURL, ontologyZipFile.toJava)

    val unzippedOntology = unzip(ontologyZipFile)

    val files = unzippedOntology.listRecursively.toSeq
    for (file <- files) {
      if (!file.isDirectory && !file.pathAsString.contains("__MACOSX") && !file.pathAsString.contains("status")) {
        println(s"input file:\t${file.pathAsString}")
        val xmlFile = XML.loadFile(file.toJava)

        var sparMimeType = ""

        (xmlFile \ "Description").foreach(
          description => description.attribute("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "about").foreach(
            about => if (about.text.startsWith("http")) sparMimeType = about.text))

        createSparMimeTypeTriple(sparMimeType, dbpediaURL_Part, model)
      }
    }
  }

  def createSparMimeTypeTriple(sparURL: String, dbpediaURL_Part: String, model: Model): Unit = {

    val mimetype = sparURL.split("mediatype/").last.replace("%2B", "+")
    val dbpediaURL = dbpediaURL_Part.concat(mimetype)
    println(s"MimeType: ${mimetype}\n")

    if (model.containsResource(model.asRDFNode(NodeFactory.createURI(dbpediaURL)))) {

      val stmt = ResourceFactory.createStatement(
        ResourceFactory.createResource(dbpediaURL_Part.concat(mimetype)),
        ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#sameAs"),
        ResourceFactory.createResource(sparURL)
      )

      model.add(stmt)
    }

  }

  def unzip(file: File): File = {
    val zipFile = new ZipFile(file.pathAsString)
    val destination = (file.parent / file.nameWithoutExtension(true)).pathAsString

    if (zipFile.isEncrypted()) println("zip file encrypted")
    else zipFile.extractAll(destination)

    return File(destination)
  }

}


