package com.sgi.backend.services;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Emailv31;
import com.sgi.backend.dao.IStatisticsDataDao;
import com.sgi.backend.entity.StatisticsData;

@Service
public class StatisticsDataService implements IStatisticsDataService {
	private static final String PATH_DEF_DATA = "C:\\Temp\\Datos";
	private static final String FILE_SEND_NAME = "consulta.txt";
	private static final String FILE_NAME = "Informacion";
	private static final String ID_DATA = "Identificacion";
	private static final String NAME_DATA = "Nombre";
	private static final String QUANTITY_DATA = "Cantidad";
	private static final String MESSAGE_MAIL = "Con el presente correo se adjunta el informe diario";
	private static final String MAIL_TO = "otzaltatto24@gmail.com";
	private final Logger log = LoggerFactory.getLogger(StatisticsDataService.class);
	

	@Autowired
	private IStatisticsDataDao statisticsDataDao;

	@Value("${mailjet.apikey}")
	private String apiKey;
	
	@Value("${mailjet.secretkey}")
	private String secretKey;
	
	public StatisticsData findById(Long id) {
		return statisticsDataDao.findById(id).orElse(null);
	}

	public StatisticsData save(StatisticsData statisticsData) {
		return statisticsDataDao.save(statisticsData);
	}

	public List<StatisticsData> findAll() {
		 List<StatisticsData> dataList = (List<StatisticsData>) statisticsDataDao.findByQuantityDesc(); 
	        try (FileWriter writer = new FileWriter(PATH_DEF_DATA +"\\consulta.txt")) {
	            for (StatisticsData dato : dataList) {
	                writer.write("ID: "+dato.getId()+" Name: "+ dato.getName()+" Quantity: " + dato.getQuantity()+ "\n"); 
	            }
	        } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return (List<StatisticsData>) statisticsDataDao.findByQuantityDesc();
	}


	
	public boolean delete(String nameFile) {
		if (nameFile != null && nameFile.length() > 0) {
			Path rutaFotoAnterior = Paths.get(PATH_DEF_DATA).resolve(nameFile).toAbsolutePath();
			File archivoFotoAnterior = rutaFotoAnterior.toFile();
			if(archivoFotoAnterior.exists() && archivoFotoAnterior.canRead()) {
				archivoFotoAnterior.delete();
				return true;
			}
		}
		
		return false;
	}
	

	public void getFileExcel() {

		
		String folderPath = PATH_DEF_DATA;
		File folder = new File(folderPath);

		if(folder.isDirectory()) {
			File[] files = folder.listFiles();
			for(File file : files) {
				if(file.isFile() && file.getName().endsWith(".xlsx")) {
//					System.out.println("Archivo Excel encontrado: "+ file.getName());
					try {
						validateNameFile(file.getName());
					} catch (JSONException | MailjetException | MailjetSocketTimeoutException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} else {
			log.info("Aun no se ha cargado el archivo");

		}
	}
	
	private void validateNameFile(String fileName) throws JSONException, MailjetException, MailjetSocketTimeoutException {
		   LocalDate dateNow = LocalDate.now();
	       DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
	       String dateFormat = dateNow.format(format);
	       String[] name = fileName.split("\\.");
	       if (FILE_NAME.concat(dateFormat).equals(name[0])) {
	    	   loadFile(fileName);		
		}else {
			log.info("Aun no se ha cargado el archivo");
		}
	       
	}

	private void loadFile(String nameFile) throws MailjetException, MailjetSocketTimeoutException, JSONException {

		Path rutaArchivo = Paths.get(PATH_DEF_DATA).resolve(nameFile).toAbsolutePath();
		Resource recurso;
		try {
			recurso = new UrlResource(rutaArchivo.toUri());
			Workbook workbook = new XSSFWorkbook(recurso.getInputStream());
			Sheet sheet = workbook.getSheetAt(0);

			Iterator<Row> rowIterator = sheet.iterator();
			List<Integer> fila = new ArrayList<>();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					if (cell.getRowIndex() == 0) {
						String valueCell = cell.getStringCellValue();
						if (valueCell.equalsIgnoreCase(ID_DATA) || valueCell.equalsIgnoreCase(NAME_DATA) || valueCell.equalsIgnoreCase(QUANTITY_DATA) ) {	            			
							fila.add(cell.getColumnIndex());
						}
					}
				} 

				if(row.getRowNum() != 0 ) {		
					if (row.getCell(fila.get(0)) != null) {
						updateData(fila, row);
					}

				}

			}
			
			recurso.getInputStream().close();
			delete(nameFile);
			 enviarCorreo();
		} catch (IOException e) {
			log.error("Error no se pudo cargar el archivo ");
		}
	}

	private void updateData(List<Integer> fila, Row row) {
		if(findById((long) row.getCell(fila.get(0)).getNumericCellValue()) != null) {						
			StatisticsData statisticsDataCurrent = findById((long) row.getCell(fila.get(0)).getNumericCellValue());
			statisticsDataCurrent.setQuantity(statisticsDataCurrent.getQuantity().add(new BigDecimal(row.getCell(fila.get(2)).getNumericCellValue())));
			save(statisticsDataCurrent);
		}else {
			StatisticsData statisticsDataNew = new StatisticsData();
			statisticsDataNew.setId((long) row.getCell(fila.get(0)).getNumericCellValue());
			statisticsDataNew.setName( row.getCell(fila.get(1)).getStringCellValue());
			statisticsDataNew.setQuantity(new BigDecimal(row.getCell(fila.get(2)).getNumericCellValue()));
			save(statisticsDataNew);
		}
	}


	

	//enviar correo
	



    public void enviarCorreo() throws MailjetException, MailjetSocketTimeoutException, JSONException, IOException {
    	 LocalDate dateNow = LocalDate.now();
         DateTimeFormatter format = DateTimeFormatter.ofPattern("dd MMMM yyyy");
         String dateFormat = dateNow.format(format);
         findAll();
    	MailjetClient client = new MailjetClient(apiKey, secretKey, new ClientOptions("v3.1"));
        
        JSONArray mailsToSend = new JSONArray();
        mailsToSend.put(new JSONObject().put("Email", MAIL_TO));
        
        JSONObject email = new JSONObject()
            .put(Emailv31.Message.FROM, new JSONObject()
                .put("Email", "otzaltatto24@gmail.com")
                .put("Name", "SGI"))
            .put(Emailv31.Message.TO, mailsToSend)
            .put(Emailv31.Message.SUBJECT, "Informe fecha: "+dateFormat)
            .put(Emailv31.Message.HTMLPART, MESSAGE_MAIL)
            .put(Emailv31.Message.ATTACHMENTS, new JSONArray()
                    .put(new JSONObject()
                            .put("ContentType", "text/plain")
                            .put("Filename", FILE_SEND_NAME)
                            .put("Base64Content", convertirArchivoAStringBase64())));

        MailjetRequest request = new MailjetRequest(Emailv31.resource)
            .property(Emailv31.MESSAGES, new JSONArray().put(email));
        
        MailjetResponse response = client.post(request);
        System.out.println(response.getStatus());
        
    	delete(FILE_SEND_NAME);
    	
    }

    private String convertirArchivoAStringBase64() throws IOException {
    	Path rutaArchivo = Paths.get(PATH_DEF_DATA).resolve(FILE_SEND_NAME).toAbsolutePath();
        byte[] bytes = Files.readAllBytes(rutaArchivo);
        return java.util.Base64.getEncoder().encodeToString(bytes);
    }

}
