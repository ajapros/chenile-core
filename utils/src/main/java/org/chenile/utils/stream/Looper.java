package org.chenile.utils.stream;

import org.chenile.core.event.EventProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * Loops through the input stream that has been passed and makes a record for each line in the input stream
 * using the encoding type that has been passed. Each line will be converted to a record of type recordClass.
 * For each record, the Chenile Event "eventId" is invoked.
 */
public class Looper {
    @Autowired
    EventProcessor eventProcessor;
    public void loop(String eventId, InputStream inputStream, String encodingType,
                        Properties headers, Class<?> recordClass) throws Exception{
        for (Object o: fileReader(inputStream,encodingType,recordClass)) {
            invokeServicesPerRecord(eventId,o,headers);
        }
    }

    private Iterable<Object> fileReader(
            InputStream inputStream,String encodingType,Class<?> recordClass) throws Exception{
        switch(encodingType.toUpperCase()) {
            case "CSV":
                return new CsvReader(inputStream, recordClass);
            case "JSON":
                return new JsonReader(inputStream,recordClass);
            case "OTHER":
            default:
                return List.of();
        }
    }

    private void invokeServicesPerRecord(String eventId,
                                         Object record,Properties headers) {
        eventProcessor.handleEvent(eventId, record);
    }
}
