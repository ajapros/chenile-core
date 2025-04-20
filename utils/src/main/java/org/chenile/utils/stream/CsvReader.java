package org.chenile.utils.stream;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Properties;

/**
 * A generic CSV file reader using Jackson CSV module.
 * Supports both header-based and headerless CSV files.
 *
 * @param <T> the target type to map each CSV row to
 */
public class CsvReader<T> implements Iterable<T> {

	private final MappingIterator<T> mappingIterator;

	/**
	 * Constructs a CsvReader using a file path and target class.
	 * Assumes the CSV has headers and comma as the default separator.
	 *
	 * @param path  the path to the CSV file
	 * @param clazz the class to map CSV rows to
	 * @throws Exception if an error occurs during reading
	 */
	public CsvReader(Path path, Class<T> clazz) throws Exception {
		this(Files.newInputStream(path), clazz, null);
	}

	/**
	 * Constructs a CsvReader using an InputStream, target class, and optional configuration.
	 *
	 * @param inputStream the input stream of the CSV file
	 * @param clazz       the class to map CSV rows to
	 * @param properties  configuration for CSV parsing:
	 *                    - csv.columnSeparator (default: ",")
	 *                    - csv.hasHeader (default: true)
	 *                    - csv.schema (comma-separated field names; required if no header)
	 * @throws Exception if parsing fails or required configuration is missing
	 */
	public CsvReader(InputStream inputStream, Class<T> clazz, Properties properties) throws Exception {
		Properties effectiveProps = (properties != null) ? properties : new Properties();

		String columnSeparator = effectiveProps.getProperty("csv.columnSeparator", ",");
		boolean hasHeader = Boolean.parseBoolean(effectiveProps.getProperty("csv.hasHeader", "true"));
		String schemaDefinition = effectiveProps.getProperty("csv.schema");

		CsvSchema schema = buildSchema(hasHeader, columnSeparator, schemaDefinition);
		CsvMapper csvMapper = new CsvMapper();

		this.mappingIterator = csvMapper.readerFor(clazz)
				.with(schema)
				.readValues(new InputStreamReader(inputStream));
	}

	@Override
	public Iterator<T> iterator() {
		return mappingIterator;
	}

	/**
	 * Builds a CSV schema either with headers or with explicitly defined column names.
	 *
	 * @param hasHeader         whether the CSV contains a header row
	 * @param columnSeparator   the delimiter used in the CSV
	 * @param schemaDefinition  the comma-separated list of column names (used if no header)
	 * @return the constructed CsvSchema
	 */
	private CsvSchema buildSchema(boolean hasHeader, String columnSeparator, String schemaDefinition) {
		char separator = columnSeparator.charAt(0);

		if (hasHeader) {
			return CsvSchema.emptySchema().withHeader().withColumnSeparator(separator);
		}

		if (schemaDefinition == null || schemaDefinition.trim().isEmpty()) {
			throw new IllegalArgumentException("Schema must be provided for headerless CSV files.");
		}

		CsvSchema.Builder builder = CsvSchema.builder().setColumnSeparator(separator);
		for (String column : schemaDefinition.split(",")) {
			builder.addColumn(column.trim());
		}

		return builder.build();
	}
}
