# DataId-Ontology
The DBpedia DataID core vocabulary is a meta-data system for detailed descriptions of datasets and their different manifestations. Established vocabularies like DCAT, VoID, Prov-O and FOAF are reused for maximum compatibility to establish a uniform and accepted way to describe and deliver dataset metadata for arbitrary datasets and to put existing standards into practice. In addition DataID can describe the relations of Agents (like persons or organizations) in regard to their rights and responsibilities.

Due to the growing complexity and different usage purposes we modularised the DataID ontology in a core and multiple mid-layer ontologies. While the core ontology is mandatory to import for any of the mid-level ontologies presented, non of those are required for describing data. That said, in many use cases some or all of the mid-level ontologies will be a useful extension.

**The DataID onion:**
![alt tag](https://raw.githubusercontent.com/dbpedia/DataId-Ontology/master/DataID%20onion.png)

The respective mid-level ontologies can be found in the folders ld (Linked Data), pv (Provenance), st (Statistics) and ps (Preservation), providing dedicated properties fitting the respective domain of interest. Further extending these ontologies can by necessary for the special purposes in some use cases.

Visit the DataID [landing page](http://wiki.dbpedia.org/projects/dbpedia-dataid) for more.

**DataID core:**<Enter>
The DataID core vocabulary describes datasets (based heavily on the DCAT ontology), as well as their relation to agents like persons or organizations in regard to their rights and responsibilities.
![alt tag](https://raw.githubusercontent.com/dbpedia/DataId-Ontology/master/DataIdOntology.png)
