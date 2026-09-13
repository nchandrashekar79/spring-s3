# Spring Boot AWS S3 Example

This project demonstrates a small Spring Boot 3 REST API backed by Amazon S3.
It supports uploading, listing, downloading, and deleting objects.

## Requirements

- Java 17+
- Maven 3.9+
- An AWS account and an S3 bucket
- AWS credentials available through the standard AWS SDK credential chain

## Configuration

Set the bucket and region before starting the application:

```powershell
$env:S3_BUCKET = "your-bucket-name"
$env:AWS_REGION = "us-east-1"
```

The AWS SDK v2 resolves credentials from environment variables, the shared
`~/.aws/credentials` profile, or an attached workload role. Do not put access
keys in `application.yml` or source code.

The identity running the application needs `s3:ListBucket` on the bucket and
`s3:GetObject`, `s3:PutObject`, and `s3:DeleteObject` on
`arn:aws:s3:::your-bucket-name/*`.

## Run

```powershell
./mvnw spring-boot:run
```

If the Maven wrapper is not present, use `mvn spring-boot:run`.

## API

Upload a file:

```powershell
curl.exe -X POST -F "file=@./photo.jpg" http://localhost:8080/api/objects/photo.jpg
```

List objects:

```powershell
curl.exe http://localhost:8080/api/objects
```

Download an object:

```powershell
curl.exe -OJ http://localhost:8080/api/objects/photo.jpg
```

Delete an object:

```powershell
curl.exe -X DELETE http://localhost:8080/api/objects/photo.jpg
```

The upload endpoint limits requests to 25 MB by default. Adjust the multipart
limits in `src/main/resources/application.yml` for a different example size.