import java.util.zip.ZipFile
import org.scalatest.flatspec.AnyFlatSpec

import scala.xml.XML

class MyTests extends AnyFlatSpec {
  info("starting..")

  "zip file" should "contains multiple files" in {
    val zipFile = new ZipFile("./src/test/src/multipleFiles.zip")
    println(zipFile.size())

    assert(zipFile.size() >= 1)
  }

  it should "contains one files" in {
    val zipFile = new ZipFile("./src/test/src/singleFile.zip")
    println(zipFile.size())

    assert(zipFile.size() == 1)
  }


  "xml file" should "give the exact mimetype" in {
    val file = XML.loadFile("./src/test/src/alto-costmap+json.rdf")

    var mimeType = ""

    (file \ "Description").foreach(
      description => description.attributes.value.foreach(
        value => {
          if (value.text.nonEmpty && mimeType == "") mimeType = value.text
          if (value.text.nonEmpty && mimeType != "") println(s"mimetype war schon: $mimeType aber soll nun in $value umbenannt werden")
        }))


    //    val mimetype2 = (file \ "Description").foreach(description => println(description.attribute("rdf:about").foreach(about => println(about))))
  }


  "rdfType" should "be http://dataid.dbpedia.org/ns/iana#application" in {
    val uri = "http://dataid.dbpedia.org/ns/iana#application/kpml-request+xml"

    val rdfType = uri.splitAt(uri.lastIndexOf("/"))._1
    println(s"rdfType: $rdfType")

    assert(rdfType == "http://dataid.dbpedia.org/ns/iana#application")

  }

  "IanaXML" should "contain IanaURL" in {
    val ianaXML = XML.load("https://www.iana.org/assignments/media-types/media-types.xml")

    val ianaURL = s"${ianaXML.namespace}/${ianaXML.attribute("id").mkString}/"
    println(ianaURL)

    //    ianaXML.nonEmptyChildren.foreach(node => {
    //      println(s"NODE: $node")
    //      println(s"Node.Namespace: ${node.namespace}")
    //      println(s"Node.label: ${node.label}")
    //      println("CHILD")
    //      println(node.child.mkString)
    //    }) //filter(node => node.namespace.matches("title")).foreach(println(_))

    println((ianaXML \\ "registry" \\ "registry" \\ "title" \\ "application"))

    ianaXML
      .child
      .filter(node => node.label.matches("registry"))
      .filter(registry => registry.attribute("id").mkString.matches("application"))
      .foreach(node => node.child
        .filter(child => child.label.matches("title"))
        .foreach(title => println(title.child)))
  }

  "sparRDFFile" should "contain sparMimeTypeURL" in {
    val file = XML.loadFile("./src/test/src/alto-costmap+json.rdf")

    println("")
    (file \ "Description")
      .foreach(description => description.attribute("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "about")
        .foreach(about => if (about.text.startsWith("http")) println(s"1st: $about")))

    println(s"2nd: ${
      (file \ "Description" \ "@http://www.w3.org/1999/02/22-rdf-syntax-ns#about").text
    }")

    println(s"3rd: ${
      (file \ "Description" \ "@about").text
    }")

    file
      .child
      .filter(child => child.label.matches("Description"))
      .foreach(description => description.attribute("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "about")
        .foreach(about => println(s"4th: $about")))

  }
}
