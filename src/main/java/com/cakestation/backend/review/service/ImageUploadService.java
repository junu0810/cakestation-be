package com.cakestation.backend.review.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.codec.binary.Base64.decodeBase64;

@Service
@RequiredArgsConstructor
public class ImageUploadService {
    private final AmazonS3Client amazonS3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    private static final String FILE_EXTENSION = "png";
    private static final String FILE_EXTENSION_SEPARATOR = ".";

    public List<String> uploadFiles(List<String> reviewImages) {
        List<String> fileUrls = new ArrayList<>();

        for (String base64Data : reviewImages) {
            byte[] bI = decodeBase64((base64Data.substring(base64Data.indexOf(",") + 1)).getBytes());

            InputStream inputStream = new ByteArrayInputStream(bI);
            String fileName = buildFileName("review");
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(bI.length);
            objectMetadata.setContentType("image/png");
            objectMetadata.setCacheControl("public, max-age=31536000");

            amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));

            fileUrls.add(amazonS3Client.getUrl(bucketName, fileName).toString());
        }
        return fileUrls;
    }

    public String buildFileName(String category) {
        String uuid = UUID.randomUUID().toString();
        return category + "/" + uuid + FILE_EXTENSION_SEPARATOR + FILE_EXTENSION;
    }
}
