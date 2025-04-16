package keml.analysis_server.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import keml.analysis.AnalysisProvider;

@Service
public class JsonProcessorService {
	private final ObjectMapper om = new ObjectMapper();

	public byte[] processJsonAndReturn(JsonNode inputJson, String timestamp) throws IOException {
		Path filePath = saveJsonToFile(inputJson, timestamp);
		String analysisFiles = AnalysisProvider.runAnalysis(filePath);
		byte[] zipBytes = Files.readAllBytes(zipAnalysisFiles(analysisFiles));
		return zipBytes;
	}

	private Path saveJsonToFile(JsonNode json, String timestamp) throws IOException {
		Path dir = Paths.get("../keml.sample/introductoryExamples/keml");
		Files.createDirectories(dir);
		Path file = dir.resolve("input_" + timestamp + ".json");
		om.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), json);
		return file;
	}

	private Path zipAnalysisFiles(String analysisFiles) throws IOException {
		Path dir = Paths.get("../keml.sample/introductoryExamples/zipped");
		Files.createDirectories(dir);
		Path analysisFilesPath = Paths.get(analysisFiles);
		String target = "../keml.sample/introductoryExamples/zipped/" + analysisFilesPath.getFileName() + ".zip";
		File sourceFolder = new File(analysisFiles);
		try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(target))) {
			File[] files = sourceFolder.listFiles();
			for (File file : files) {
				String zipEntryName = sourceFolder.toPath().relativize(file.toPath()).toString();
				try {
					zipOut.putNextEntry(new ZipEntry(zipEntryName));
					Files.copy(file.toPath(), zipOut);
					zipOut.closeEntry();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return Paths.get(target);
	}
}
