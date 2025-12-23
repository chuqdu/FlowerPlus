package base.api.controller;

import base.api.base.BaseAPIController;
import base.api.dto.response.TFUResponse;
import base.api.service.impl.CloudinaryService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class UploadController extends BaseAPIController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<TFUResponse<String>> uploadFile(
            @NotNull
            @RequestPart("file") MultipartFile file
    ) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file, "uploads");
            return success(imageUrl);
        } catch (Exception e) {
            return badRequest("Upload file thất bại");
        }
    }

}
