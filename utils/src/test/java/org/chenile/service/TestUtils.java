package org.chenile.service;

import org.chenile.SpringConfig;
import org.chenile.utils.stream.Looper;
import org.junit.Assert;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;


//@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringConfig.class)
@SpringJUnitConfig(SpringConfig.class)

@ActiveProfiles("unittest")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public  class TestUtils {


	@Autowired
	private Looper looper;



	@Test
	@Order(2)
	public void testFile() throws Exception {

		String csvData = """
                EmployeeID,FirstName,LastName,Department,Salary,StartDate
                101,Jane,Doe,Engineering,85000,2021-03-15
                102,John,Smith,Marketing,72000,2020-07-01
                103,Alice,Johnson,HR,65000,2022-01-10
                104,Bob,Williams,Sales,78000,2019-11-20
                105,Eva,Brown,Engineering,91000,2023-05-12
                """;

		InputStream inputStream = new ByteArrayInputStream(csvData.getBytes());

		Properties headers = new Properties(); // if needed, you can populate it with mapping info
		headers.setProperty("delimiter", ",");

		List<EmployeeWithHeaders> context = new ArrayList<>();

		Consumer<Object> logHighEarners = obj -> {
			if (obj instanceof EmployeeWithHeaders emp && emp.getSalary() > 80000) {
				System.out.println("High earner: " + emp.getFirstName() + " - $" + emp.getSalary());
				context.add(emp);
			}
		};

		looper.loop(inputStream,"csv",headers, EmployeeWithHeaders.class,logHighEarners );

		Assert.assertEquals("Success!", 2, context.size());

	}

	@Test
	public void testFileWithoutHeaders() throws Exception {

		String csvData = """
                101,Jane,Doe,Engineering,85000,2021-03-15
                102,John,Smith,Marketing,72000,2020-07-01
                103,Alice,Johnson,HR,65000,2022-01-10
                104,Bob,Williams,Sales,78000,2019-11-20
                105,Eva,Brown,Engineering,91000,2023-05-12
                """;

		InputStream inputStream = new ByteArrayInputStream(csvData.getBytes());

		Properties headers = new Properties(); // if needed, you can populate it with mapping info
		headers.setProperty("csv.hasHeader", ",");
		headers.setProperty("csv.hasHeader","false");
		headers.setProperty("csv.schema", "employeeID,firstName,lastName,department,salary,startDate");


		List<EmployeeWithoutHeaders> context = new ArrayList<>();

		Consumer<Object> logHighEarners = obj -> {
			if (obj instanceof EmployeeWithoutHeaders emp && emp.getSalary() > 80000) {
				System.out.println("High earner: " + emp.getFirstName() + " - $" + emp.getSalary());
				context.add(emp);
			}
		};

		looper.loop(inputStream,"csv",headers, EmployeeWithoutHeaders.class,logHighEarners );

		Assert.assertEquals("Success!", 2, context.size());

	}


	@Test
	public void testMissingSchemaForHeaderlessCsv() throws Exception {

		assertThrows(IllegalArgumentException.class, () -> {
			String csv = "1,Alan,Turing,AI,100000,2020-01-01";
			InputStream inputStream = new ByteArrayInputStream(csv.getBytes());

			Properties props = new Properties();
			props.setProperty("csv.hasHeader", "false");
			List<EmployeeWithoutHeaders> context = new ArrayList<>();

			Consumer<Object> logHighEarners = obj -> {
				if (obj instanceof EmployeeWithoutHeaders emp && emp.getSalary() > 80000) {
					System.out.println("High earner: " + emp.getFirstName() + " - $" + emp.getSalary());
					context.add(emp);
				}
			};
			looper.loop(inputStream,"csv",props, EmployeeWithoutHeaders.class,logHighEarners );
		});


	}


	@Test
	public void testCsvWithCustomDelimiter() throws Exception {
		String csv = """
        1|Alan|Turing|AI|100000|2020-01-01
        2|Ada|Lovelace|ML|95000|2021-02-01
    """;

		InputStream inputStream = new ByteArrayInputStream(csv.getBytes());

		Properties props = new Properties();
		props.setProperty("csv.hasHeader", "false");
		props.setProperty("csv.columnSeparator", "|");
		props.setProperty("csv.schema", "employeeID,firstName,lastName,department,salary,startDate");

		List<EmployeeWithoutHeaders> context = new ArrayList<>();
		Consumer<Object> logHighEarners = obj -> {
			if (obj instanceof EmployeeWithoutHeaders emp && emp.getSalary() > 80000) {
				System.out.println("High earner: " + emp.getFirstName() + " - $" + emp.getSalary());
				context.add(emp);
			}
		};

		looper.loop(inputStream,"csv",props, EmployeeWithoutHeaders.class,logHighEarners );


		Assert.assertEquals(2, context.size());
		Assert.assertEquals("Lovelace", context.get(1).getLastName());
	}



}
