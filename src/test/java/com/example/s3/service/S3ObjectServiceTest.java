package com.example.s3.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.example.s3.config.S3Properties;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@ExtendWith(MockitoExtension.class)
class S3ObjectServiceTest {

    @Mock
    private S3Client s3Client;

    @Test
    void uploadsFileToConfiguredBucket() {
        var service = new S3ObjectService(s3Client, properties());
        var file = new MockMultipartFile("file", "hello.txt", "text/plain",
                "hello".getBytes(StandardCharsets.UTF_8));

        service.upload("hello.txt", file);

        verify(s3Client).putObject(any(PutObjectRequest.class),
                any(software.amazon.awssdk.core.sync.RequestBody.class));
    }

    @Test
    void listsObjectKeys() {
        when(s3Client.listObjectsV2(any(software.amazon.awssdk.services.s3.model.ListObjectsV2Request.class)))
                .thenReturn(ListObjectsV2Response.builder()
                        .contents(S3Object.builder().key("one.txt").build())
                        .build());

        var service = new S3ObjectService(s3Client, properties());

        assertThat(service.list()).containsExactly("one.txt");
    }

    private S3Properties properties() {
        var properties = new S3Properties();
        properties.setBucket("test-bucket");
        properties.setRegion("us-east-1");
        return properties;
    }
}
