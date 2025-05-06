package dev.crepe.infra.s3.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.s3.base-url}")
    private String baseUrl;

    /**
     * S3에 파일을 업로드하고 접근 URL을 반환합니다.
     *
     * @param file 업로드할 파일
     * @param dirName 저장할 디렉토리 이름
     * @return 업로드된 파일의 URL
     */
    public String uploadFile(MultipartFile file, String dirName) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = getExtension(originalFilename);
            String fileName = dirName + "/" + UUID.randomUUID() + extension;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return baseUrl + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 파일 이름에서 확장자를 추출합니다.
     *
     * @param fileName 파일 이름
     * @return 파일 확장자 (점 포함)
     */
    private String getExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex);
    }

    /**
     * 파일 URL에서 S3 키를 추출합니다.
     *
     * @param fileUrl 파일 URL
     * @return S3 키
     */
    public String extractKeyFromUrl(String fileUrl) {
        return fileUrl.replace(baseUrl + "/", "");
    }
}
