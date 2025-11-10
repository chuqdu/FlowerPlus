package base.api.controller;

import base.api.base.BaseAPIController;
import base.api.dto.response.TFUResponse;
import base.api.service.impl.CloudinaryService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class UploadController extends BaseAPIController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping("/upload")
    public ResponseEntity<TFUResponse<String>> uploadFile(@NotNull @RequestParam("file")MultipartFile file){
        try {
            String imageUrl = cloudinaryService.uploadImage(file, "uploads");
            return success(imageUrl);
        } catch (Exception e) {
            return badRequest("Upload file thất bại");
        }
    }

}
