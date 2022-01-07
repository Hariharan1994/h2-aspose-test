package com.asposetest.sample.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.util.UUID;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import com.aspose.slides.InvalidPasswordException;
import com.aspose.slides.Presentation;
import com.asposetest.sample.support.AspsoeTestException;
import com.asposetest.sample.support.SettingCons;
import com.asposetest.sample.support.SupportUtils;

/**
 * This is an implementation class used for handling AsposeLibrary Test
 * 
 * @author HARIHARAN MANI
 * @since 07-01-2022
 */
@Service
public class AsposeTestServiceImpl implements AsposeTestService {

	private static final String CONTENT_HEADER = "Content-Disposition";

	@Override
	public void downloadPdfPreviewFile(HttpServletResponse response, File inputFile) {

		String originalFileName = inputFile.getName();

		String tempFileKey = String.valueOf(UUID.randomUUID());
		String tempSaveFileName = tempFileKey.concat(".pdf");

		SupportUtils.printSysout(
				String.format("Generating PDF Preview for [%s] in Temp >> %s", originalFileName, tempFileKey));

		File outPutFile = new File(SettingCons.TEMP_PREVIEW_DIR, tempSaveFileName);

		convertToPdf(inputFile, outPutFile);

		downloadConvertedPdf(response, outPutFile);
	}

	private void downloadConvertedPdf(HttpServletResponse response, File outPutFile) {
		String fileName = outPutFile.getName();
		SupportUtils.printSysout("Downloading " + fileName);

		try (FileInputStream inputStream = new FileInputStream(outPutFile);
				ServletOutputStream outputStream = response.getOutputStream();) {

			String encodedFileName = URLEncoder.encode(fileName, SettingCons.DEF_ENCODING).replace("+", "%20");
			response.setCharacterEncoding(SettingCons.DEF_ENCODING);
			response.setHeader("Content-Length", String.valueOf(outPutFile.length())); // To show total size
			response.setHeader(CONTENT_HEADER, "attachment; filename*=UTF-8''" + encodedFileName);
			response.setContentType("text/html");

			IOUtils.copy(inputStream, outputStream);

			outputStream.flush();
			SupportUtils.printSysout(String.format("[%s] PDF download completed...!", fileName));

			SupportUtils.printSysout(String.format("TEMP file delete status => [%s]", outPutFile.delete()));
		} catch (Exception e) {
			throw SupportUtils.customException("Download Failed : " + e.getMessage());
		}
	}

	private void convertToPdf(File inputFile, File outPutFile) {

		try (FileInputStream inputStream = new FileInputStream(inputFile);
				FileOutputStream outputStream = new FileOutputStream(outPutFile)) {

			savePresentationGroup(inputStream, outputStream, inputFile.getName());

			outputStream.flush();
			SupportUtils.printSysout("deCrypt, deCompress process for PDF FilePreview completed...!");
		} catch (AspsoeTestException restExcep) {
			throw restExcep;
		} catch (Exception e) {
			throw SupportUtils.customException("generateTempPdf ERROR - " + e.getMessage());
		}
	}

	private void savePresentationGroup(FileInputStream inputStream, FileOutputStream outputStream, String fileType) {

		try {
			com.aspose.slides.LoadOptions loadOptions = new com.aspose.slides.LoadOptions();
			Presentation pres = new Presentation(inputStream, loadOptions);
			SupportUtils.printSysout(String.format("[%s] loaded with [aspose.slides] class", fileType));

			pres.save(outputStream, com.aspose.slides.SaveFormat.Pdf); // this line throws error only in server

			SupportUtils.printSysout(fileType + " saved as PDF");
		} catch (InvalidPasswordException e) {
			throw SupportUtils.customException("Password Protected File");
		} catch (Exception e) {
			throw SupportUtils.customException("savePresentationGroup ERROR - " + e.getMessage());
		}
	}
}