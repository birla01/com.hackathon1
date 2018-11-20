package com.transfer;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.commons.io.FileUtils;

import com.directdebit.xml.schema.pain_001_001_06.AccountIdentification4Choice;
import com.directdebit.xml.schema.pain_001_001_06.ActiveOrHistoricCurrencyAndAmount;
import com.directdebit.xml.schema.pain_001_001_06.AmountType4Choice;
import com.directdebit.xml.schema.pain_001_001_06.BranchAndFinancialInstitutionIdentification5;
import com.directdebit.xml.schema.pain_001_001_06.CashAccount24;
import com.directdebit.xml.schema.pain_001_001_06.ChargeBearerType1Code;
import com.directdebit.xml.schema.pain_001_001_06.CreditTransferTransaction20;
import com.directdebit.xml.schema.pain_001_001_06.CustomerCreditTransferInitiationV06;
import com.directdebit.xml.schema.pain_001_001_06.FinancialInstitutionIdentification8;
import com.directdebit.xml.schema.pain_001_001_06.GroupHeader48;
import com.directdebit.xml.schema.pain_001_001_06.PartyIdentification43;
import com.directdebit.xml.schema.pain_001_001_06.PaymentIdentification1;
import com.directdebit.xml.schema.pain_001_001_06.PaymentInstruction16;
import com.directdebit.xml.schema.pain_001_001_06.PaymentMethod3Code;
import com.directdebit.xml.schema.pain_001_001_06.PaymentTypeInformation19;
import com.directdebit.xml.schema.pain_001_001_06.RemittanceInformation10;
import com.directdebit.xml.schema.pain_001_001_06.ServiceLevel8Choice;
import com.directdebit.xml.schema.pain_001_003_03.ActiveOrHistoricCurrencyCodeEUR;
import com.util.SepaXmlDocumentBuilder;

class SepaTransferDocumentBuilder2 extends SepaXmlDocumentBuilder {

	public static String toXml(SepaTransferDocumentData source) throws DatatypeConfigurationException, IOException {
		com.directdebit.xml.schema.pain_001_001_06.Document doc = new com.directdebit.xml.schema.pain_001_001_06.Document();
		CustomerCreditTransferInitiationV06 transferData = new CustomerCreditTransferInitiationV06();

		doc.setCstmrCdtTrfInitn(transferData);

		transferData.setGrpHdr(createGroupHeaderSdd(source));

		transferData.getPmtInf().add(createPaymentInstructions(source));

		StringWriter resultWriter = new StringWriter();
		marshal(doc.getClass().getPackage().getName(),
				new com.directdebit.xml.schema.pain_001_001_06.ObjectFactory().createDocument(doc), resultWriter);

		FileUtils.writeStringToFile(new File("C:\\Users\\dell\\pain001006\\" + Calendar.getInstance().getTimeInMillis()),
				resultWriter.toString());

		return resultWriter.toString();
	}

	private static GroupHeader48 createGroupHeaderSdd(SepaTransferDocumentData data)
			throws DatatypeConfigurationException {
		GroupHeader48 result = new GroupHeader48();
		// message id
		result.setMsgId(data.getDocumentMessageId());

		// created on
		result.setCreDtTm(calendarToXmlGregorianCalendarDateTime(GregorianCalendar.getInstance()));

		// number of tx
		result.setNbOfTxs(String.valueOf(data.getPayments().size()));

		// control sum
		result.setCtrlSum(data.getTotalPaymentSum());

		// creditor name
		PartyIdentification43 partyIdentification43 = new PartyIdentification43();
		partyIdentification43.setNm(data.getPayerName());

		result.setInitgPty(partyIdentification43);

		return result;
	}

	private static PaymentInstruction16 createPaymentInstructions(SepaTransferDocumentData data)
			throws DatatypeConfigurationException {
		PaymentInstruction16 result = new PaymentInstruction16();
		result.setBtchBookg(data.isBatchBooking());
		result.setChrgBr(ChargeBearerType1Code.SLEV);
		result.setCtrlSum(data.getTotalPaymentSum());
		result.setNbOfTxs(String.valueOf(data.getPayments().size()));

		setPayerName(data, result);

		setPayerIbanAndBic(data, result);

		result.setPmtInfId(data.getDocumentMessageId());
		result.setPmtMtd(PaymentMethod3Code.TRF);
		result.setReqdExctnDt(calendarToXmlGregorianCalendarDateTime(data.getDateOfExecution()));

		setPaymentTypeInformation(result);

		for (SepaTransferPayment p : data.getPayments()) {
			addPaymentData(result, p);
		}

		return result;
	}

	private static void addPaymentData(PaymentInstruction16 result, SepaTransferPayment p) {
		result.getCdtTrfTxInf().add(createPaymentData(p));
	}

	private static void setPayerName(SepaTransferDocumentData data, PaymentInstruction16 result) {
		PartyIdentification43 pi2 = new PartyIdentification43();
		pi2.setNm(data.getPayerName());
		result.setDbtr(pi2);
	}

	private static void setPayerIbanAndBic(SepaTransferDocumentData data, PaymentInstruction16 result) {
		AccountIdentification4Choice ai = new AccountIdentification4Choice();
		ai.setIBAN(data.getPayerIban());
		CashAccount24 ca1 = new CashAccount24();
		ca1.setId(ai);
		result.setDbtrAcct(ca1);

		BranchAndFinancialInstitutionIdentification5 bafii = new BranchAndFinancialInstitutionIdentification5();
		FinancialInstitutionIdentification8 fii = new FinancialInstitutionIdentification8();
		fii.setBICFI(data.getPayerBic());
		bafii.setFinInstnId(fii);
		result.setDbtrAgt(bafii);
	}

	private static void setPaymentTypeInformation(PaymentInstruction16 result) {
		PaymentTypeInformation19 pti = new PaymentTypeInformation19();
		ServiceLevel8Choice sls = new ServiceLevel8Choice();
		sls.setCd("SEPA");
		pti.setSvcLvl(sls);
		result.setPmtTpInf(pti);
	}

	private static CreditTransferTransaction20 createPaymentData(SepaTransferPayment p) {
		CreditTransferTransaction20 result = new CreditTransferTransaction20();
		setPaymentCurrencyAndSum(p, result);
		setPayeeName(p, result);
		setPayeeIbanAndBic(p, result);
		setEndToEndId(p, result);
		setReasonForPayment(p, result);

		return result;
	}

	private static void setPaymentCurrencyAndSum(SepaTransferPayment p, CreditTransferTransaction20 result) {
		AmountType4Choice at = new AmountType4Choice();
		ActiveOrHistoricCurrencyAndAmount aohcaa = new ActiveOrHistoricCurrencyAndAmount();
		aohcaa.setCcy(ActiveOrHistoricCurrencyCodeEUR.EUR.toString());
		aohcaa.setValue(p.getPaymentSum());
		at.setInstdAmt(aohcaa);
		result.setAmt(at);
	}

	private static void setPayeeName(SepaTransferPayment p, CreditTransferTransaction20 result) {
		PartyIdentification43 pis2 = new PartyIdentification43();
		pis2.setNm(p.getPayeeName());
		result.setCdtr(pis2);
	}

	private static void setEndToEndId(SepaTransferPayment p, CreditTransferTransaction20 result) {
		PaymentIdentification1 pis = new PaymentIdentification1();
		String id = p.getEndToEndId();
		pis.setEndToEndId(id == null || id.isEmpty() ? "NOTPROVIDED" : "");
		result.setPmtId(pis);
	}

	private static void setReasonForPayment(SepaTransferPayment p, CreditTransferTransaction20 result) {
		RemittanceInformation10 ri = new RemittanceInformation10();
		// ri.getStrd().get(0).sete(p.getReasonForPayment());
		result.setRmtInf(ri);
	}

	private static void setPayeeIbanAndBic(SepaTransferPayment p, CreditTransferTransaction20 ctti) {
		CashAccount24 ca = new CashAccount24();
		AccountIdentification4Choice ai = new AccountIdentification4Choice();
		ai.setIBAN(p.getPayeeIban());
		ca.setId(ai);
		ctti.setCdtrAcct(ca);

		BranchAndFinancialInstitutionIdentification5 bafiis = new BranchAndFinancialInstitutionIdentification5();
		FinancialInstitutionIdentification8 fii = new FinancialInstitutionIdentification8();
		fii.setBICFI(p.getPayeeBic());
		bafiis.setFinInstnId(fii);
		ctti.setCdtrAgt(bafiis);
	}
}
