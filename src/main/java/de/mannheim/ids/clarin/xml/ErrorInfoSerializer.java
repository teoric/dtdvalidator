package de.mannheim.ids.clarin.xml;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class ErrorInfoSerializer extends StdSerializer<ErrorInfo> {

    public ErrorInfoSerializer() {
        this(null);
    }

    public ErrorInfoSerializer(Class<ErrorInfo> t) {
        super(t);
    }

    @Override
    public void serialize(
            ErrorInfo value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {

        jgen.writeStartObject();
        jgen.writeFieldName("occcurrences");
        jgen.writeStartArray();
        for (ErrorInfo.Occurrence occ : value.getOccurrences()) {
            jgen.writeObject(occ);
        }
        jgen.writeEndArray();
        jgen.writeEndObject();
    }
}

