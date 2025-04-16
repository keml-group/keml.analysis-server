package keml.analysis_server.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import keml.analysis_server.services.JsonProcessorService;

@RestController
@RequestMapping("/api")
public class JsonProcessingController {

	private final JsonProcessorService jsonProcessorService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public JsonProcessingController(JsonProcessorService jsonProcessorService) {
		this.jsonProcessorService = jsonProcessorService;
	}

	private final String APPLICATION_ZIP_VALUE = "application/zip";

	@PostMapping(path = "/process-json", consumes = MediaType.APPLICATION_JSON_VALUE, produces = APPLICATION_ZIP_VALUE)
	public ResponseEntity<?> processAndRespond(@RequestBody JsonNode jsonNode) {
		try {
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
			byte[] zipBytes = jsonProcessorService.processJsonAndReturn(jsonNode, timestamp);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentDisposition(
					ContentDisposition.attachment().filename("input_" + timestamp + ".zip").build());
			headers.setContentType(MediaType.valueOf(APPLICATION_ZIP_VALUE));
			return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
		} catch (IOException e) {
			return ResponseEntity.internalServerError()
					.body(objectMapper.createObjectNode().put("error", e.getMessage()));
		}
	}
}
