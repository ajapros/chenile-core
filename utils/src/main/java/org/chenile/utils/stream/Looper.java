package org.chenile.utils.stream;

import org.chenile.core.event.EventProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.Properties;
import java.util.function.Consumer;

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

    public void loop( InputStream inputStream, String encodingType,
                     Properties headers, Class<?> recordClass,
                      Consumer<Object> consumer) throws Exception{
        for (Object o: fileReader(inputStream,encodingType,recordClass)) {
            invokeServicesPerRecord(consumer,o,headers);
        }
    }

    private Iterable<Object> fileReader(
            InputStream inputStream,String encodingType,Class<?> recordClass) throws Exception{
        return switch (encodingType.toUpperCase()) {
            case "CSV" -> new CsvReader(inputStream, recordClass);
            case "JSON" -> new JsonReader(inputStream, recordClass);
            default -> new LineIterable(inputStream);
        };
    }

    private void invokeServicesPerRecord(String eventId,
                                         Object record,Properties headers) {
        eventProcessor.handleEvent(eventId, record);
    }
    private void invokeServicesPerRecord(Consumer<Object> consumer,
                                         Object record,Properties headers) {
        consumer.accept( record);
    }
}
