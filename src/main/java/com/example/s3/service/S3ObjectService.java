package com.example.s3.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.s3.config.S3Properties;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class S3ObjectService {

    private final S3Client s3Client;
    private final S3Properties properties;

    public S3ObjectService(S3Client s3Client, S3Properties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    public void upload(String key, MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            var request = PutObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to upload object '" + key + "'", exception);
        }
    }

    public List<String> list() {
        var request = ListObjectsV2Request.builder()
                .bucket(properties.getBucket())
                .build();
        return s3Client.listObjectsV2(request)
                .contents()
                .stream()
                .map(object -> object.key())
                .toList();
    }

    public ObjectDownload download(String key) {
        try {
            var head = s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(key)
                    .build());
            var response = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(key)
                    .build());
            return new ObjectDownload(response, head.contentType(), head.contentLength());
        } catch (NoSuchKeyException exception) {
            throw new ObjectNotFoundException(key);
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                throw new ObjectNotFoundException(key);
            }
            throw exception;
        }
    }

    public void delete(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(key)
                .build());
    }

    public record ObjectDownload(InputStream response, String contentType, Long contentLength) {
    }

    public static class ObjectNotFoundException extends RuntimeException {
        public ObjectNotFoundException(String key) {
            super("Object not found: " + key);
        }
    }
}
