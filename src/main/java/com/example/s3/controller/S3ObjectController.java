package com.example.s3.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.s3.service.S3ObjectService;

@RestController
@RequestMapping("/api/objects")
public class S3ObjectController {

    private final S3ObjectService objectService;

    public S3ObjectController(S3ObjectService objectService) {
        this.objectService = objectService;
    }

    @PostMapping("/{key}")
    public ResponseEntity<Void> upload(@PathVariable String key, @RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        objectService.upload(key, file);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<String> list() {
        return objectService.list();
    }

    @GetMapping("/{key}")
    public ResponseEntity<InputStreamResource> download(@PathVariable String key) {
        var object = objectService.download(key);
        var headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(key).build());
        if (object.contentLength() != null) {
            headers.setContentLength(object.contentLength());
        }
        var contentType = object.contentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(object.contentType());
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(contentType)
                .body(new InputStreamResource(object.response()));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        objectService.delete(key);
        return ResponseEntity.noContent().build();
    }
}
