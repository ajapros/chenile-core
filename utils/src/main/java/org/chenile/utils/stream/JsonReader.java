package org.chenile.utils.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

/**
 * Read a JSON file. Treat the file as a giant array of multiple records.
 * Parse it and return an iterator
 * @author Raja Shankar Kolluru
 *
 */
public class JsonReader<T> implements Iterable<T>{

	ObjectMapper objectMapper = new ObjectMapper();
	private List<T> list;
	

	public JsonReader(Path path, Class<T> recordClass) throws Exception{
		this(Files.newInputStream(path),recordClass);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JsonReader(InputStream inputStream, Class<?> recordClass) throws Exception{
		TypeFactory tf = objectMapper.getTypeFactory();
		CollectionType type = tf.constructCollectionType(List.class, recordClass);

		this.list = (List)objectMapper.readValue(inputStream, type);
	}

	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}

}
