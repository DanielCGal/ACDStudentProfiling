package com.studentprofiling.studentprofiling;


import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("studentprofiling/files")
@CrossOrigin(origins = "*")
public class FileController {

    private final Path pictureRoot = Paths.get("uploads/profile-pictures");
    private final Path uploadsRoot = Paths.get("uploads"); 

    @GetMapping("/profile-pictures/{fileName}")
    public ResponseEntity<Resource> getProfilePicture(@PathVariable String fileName) throws MalformedURLException {
        Path file = pictureRoot.resolve(fileName);
        Resource resource = new UrlResource(file.toUri());
        if (!resource.exists()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) 
                .body(resource);
    }

    
    @GetMapping("/any/**")
    public ResponseEntity<Resource> getAny(HttpServletRequest req) throws MalformedURLException {
        
        String rest = req.getRequestURI().replaceFirst(".*/any/", "");
        Path file = uploadsRoot.resolve(rest);
        Resource resource = new UrlResource(file.toUri());
        if (!resource.exists()) return ResponseEntity.notFound().build();
        MediaType mt = rest.toLowerCase().endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(mt).body(resource);
    }

}
