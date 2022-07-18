package com.mediaxi.fileserver.controller;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.swing.plaf.multi.MultiPanelUI;
import javax.validation.Valid;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@Slf4j
public class FileController {
    @Value("${uploadPath}")
    private String FILE_UPLOAD_PATH;
    private Integer fileCount = 1;

    private static final String serverUrl = "http://localhost:8081/api/v1/composition/layers/2/clips/1/connect";

    static {
        System.setProperty("java.awt.headless", "false");
    }

    @PostMapping(value = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity addFiles(@RequestParam("file") MultipartFile file) {
        try {
            saveFile(file);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity("파일이 업로드 되었습니다", HttpStatus.OK);
    }

    private void sendToLayerOn() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(new MediaType[] { MediaType.APPLICATION_JSON }));

        // Body set
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        HttpEntity<String> request = new HttpEntity<String>("", headers);

        ResponseEntity<String> response = restTemplate.postForEntity(serverUrl, request , String.class );

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("레졸룸 Layer Play [" + response.getStatusCodeValue() + " Status]");
        } else{
            log.info("레졸룸 API Error" + response.getStatusCodeValue() + " Status");
        }
    }
    //curl -X POST "http://localhost:8081/api/v1/composition/layers/2/clips/1/connect" -H  "accept: */*" -H  "Content-Type: application/json" -d "true"

    public void saveFile(MultipartFile file) throws IOException {
        String newFileName = fileCount + ".jpg";
        File saveFile = new File(FILE_UPLOAD_PATH, newFileName);
        log.info(String.format("[%d]새로운 파일 저장! 경로 : %s", fileCount,saveFile.getAbsolutePath())) ;
        FileCopyUtils.copy(file.getBytes(), saveFile);

        if (++fileCount > 5) {
            fileCount = 1;
        }
        try {
            sendToLayerOn();
            // e
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
