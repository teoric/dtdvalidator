module i5validator {
    exports de.mannheim.ids.clarin.xml;

    requires com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires info.picocli;
    requires java.xml;
    requires org.apache.commons.compress;
    requires org.apache.commons.io;
    requires org.slf4j;
}