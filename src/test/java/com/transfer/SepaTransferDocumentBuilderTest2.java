package com.transfer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.Calendar;

import org.junit.Test;

import com.util.SepaValidationException;

public class SepaTransferDocumentBuilderTest2 {

    public SepaTransferDocumentBuilderTest2() {
    }

    /**
     * Test of toXml method, of class SepaTransferDocumentBuilder.
     */
    @Test
    public void testToXml() throws Exception {
        SepaTransferDocumentData data = new SepaTransferDocumentData("MALADE51NWD", "DE89370400440532013000", "Hans Mustermann", "12345");

        Calendar dueDate = Calendar.getInstance();
        dueDate.set(Calendar.HOUR, 0);
        dueDate.set(Calendar.MINUTE, 0);
        dueDate.set(Calendar.SECOND, 0);
        dueDate.add(Calendar.DATE, 14);
        data.setDateOfExecution(dueDate);

        data.addPayment(createTestPayment("123.4539", "Arme Wurst", "MALADE51NWD", "DE89370400440532013000"));
        data.addPayment(createTestPayment("99.9930", "Arme Wurst2", "MALADE51NWD", "DE89370400440532013000"));
        data.addPayment(createTestPayment("10", "Loooooong Loooooong Loooooong Loooooong Loooooong Loooooong Loooooong Name", "MALADE51NWD", "DE89370400440532013000"));
        
     /*   try (Stream<String> stream = Files.lines(Paths.get("C:\\Users\\hackathon\\hackathon-master\\hackathon-master\\second\\com.hackathon1.demo\\input.txt"))) {
            stream.forEach(s-> s.split(","));
            String row = stream.collect(Collectors.joining(","));
    }*/
        
		try (BufferedReader br = new BufferedReader(new FileReader(new File(
				"C:\\Users\\dell\\project\\com.hackathon1.demo\\input.txt")))) {
			for (String line; (line = br.readLine()) != null;) {
				// process the line.
				String row [] = line.split(",");
				if (row[0].equals("110")) {
					System.out.println(line);
					//data.setPayerIban(row[5]);
					SepaTransferDocumentBuilder2.toXml(data);
				} else {
					System.out.println();
					// error
				}
			}
			// line is not visible here.
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        String result = SepaTransferDocumentBuilder2.toXml(data);
//        System.out.println(result);
        assertTrue(result.contains("<InstdAmt Ccy=\"EUR\">123.45</InstdAmt>"));
        assertTrue(result.contains("<InstdAmt Ccy=\"EUR\">99.99</InstdAmt>"));
        assertTrue(result.contains("<EndToEndId>NOTPROVIDED</EndToEndId>"));
        assertTrue(result.contains("<CtrlSum>233.44</CtrlSum>"));
        assertTrue(result.contains("DE89370400440532013000"));
        assertTrue(result.contains("Arme Wurst2"));
        assertTrue(result.contains("Hans Mustermann"));
        //assertTrue(result.contains("test- berweisung"));
        assertTrue(result.contains("Loooooong Loooooong Loooooong Loooooong Loooooong Loooooong Loooooong "));
        assertFalse(result.contains("Loooooong Loooooong Loooooong Loooooong Loooooong Loooooong Loooooong N"));
    }

    private SepaTransferPayment createTestPayment(String sum, String debitorName, String bic, String iban) throws SepaValidationException {
        SepaTransferPayment result = new SepaTransferPayment();

        result.setPayeeBic(bic);
        result.setPayeeIban(iban);
        result.setPayeeName(debitorName);
        result.setPaymentSum(new BigDecimal(sum));
        result.setReasonForPayment("test-Überweisung");
        return result;
    }

}
