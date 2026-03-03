package com.example.demo.service;

import com.example.demo.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ProductService {
    private final List<Product> listProduct = new ArrayList<>();

    public List<Product> getAll() {
        return listProduct;
    }

    public List<Product> search(String keyword) {
        String text = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        if (text.isEmpty()) {
            return listProduct;
        }
        return listProduct.stream().filter(product -> {
            String productName = product.getName() == null ? "" : product.getName().toLowerCase(Locale.ROOT);
            String categoryName = product.getCategory() == null || product.getCategory().getName() == null
                    ? ""
                    : product.getCategory().getName().toLowerCase(Locale.ROOT);
            return productName.contains(text) || categoryName.contains(text);
        }).toList();
    }

    public Product get(int id) {
        return listProduct.stream().filter(product -> product.getId() == id).findFirst().orElse(null);
    }

    public void add(Product newProduct) {
        int maxId = listProduct.stream().mapToInt(Product::getId).max().orElse(0);
        newProduct.setId(maxId + 1);
        listProduct.add(newProduct);
    }

    public void update(Product editProduct) {
        Product find = get(editProduct.getId());
        if (find != null) {
            find.setPrice(editProduct.getPrice());
            find.setName(editProduct.getName());
            find.setCategory(editProduct.getCategory());
            if (editProduct.getImage() != null) {
                find.setImage(editProduct.getImage());
            }
        }
    }

    public void delete(int id) {
        listProduct.removeIf(product -> product.getId() == id);
    }

    public void updateImage(Product newProduct, MultipartFile imageProduct) {
        String contentType = imageProduct.getContentType();
        if (contentType != null && !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Tệp tải lên không phải là hình ảnh!");
        }
        if (!imageProduct.isEmpty()) {
            try {
                Path dirImages = Paths.get("uploads");
                if (!Files.exists(dirImages)) {
                    Files.createDirectories(dirImages);
                }
                String originalFilename = imageProduct.getOriginalFilename();
                String safeFilename = originalFilename == null ? "image" : originalFilename;
                String newFileName = UUID.randomUUID() + "-" + safeFilename;
                Path pathFileUpload = dirImages.resolve(newFileName);
                Files.copy(imageProduct.getInputStream(), pathFileUpload, StandardCopyOption.REPLACE_EXISTING);
                newProduct.setImage(newFileName);
            } catch (IOException e) {
                throw new RuntimeException("Không thể lưu hình ảnh", e);
            }
        }
    }
}
