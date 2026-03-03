package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String Index(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<Product> products = keyword == null || keyword.isBlank()
                ? productService.getAll()
                : productService.search(keyword);
        model.addAttribute("listproduct", products);
        model.addAttribute("keyword", keyword);
        return "product/products";
    }

    @GetMapping("/create")
    public String Create(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAll());
        return "product/create";
    }

    @PostMapping("/create")
    public String Create(@Valid Product newProduct, BindingResult result,
                         @RequestParam("category.id") int categoryId,
                         @RequestParam("imageProduct") MultipartFile imageProduct, Model model) {
        validateImageName(imageProduct, result);
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAll());
            return "product/create";
        }
        try {
            productService.updateImage(newProduct, imageProduct);
        } catch (IllegalArgumentException ex) {
            result.rejectValue("image", "image.invalid", ex.getMessage());
            model.addAttribute("categories", categoryService.getAll());
            return "product/create";
        }
        newProduct.setCategory(categoryService.get(categoryId));
        productService.add(newProduct);
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String Edit(@PathVariable int id, Model model) {
        Product find = productService.get(id);
        if (find == null) {
            return "error/404";
        }
        model.addAttribute("product", find);
        model.addAttribute("categories", categoryService.getAll());
        return "product/edit";
    }

    @PostMapping("/edit")
    public String Edit(@Valid Product editProduct, BindingResult result,
                       @RequestParam("category.id") int categoryId,
                       @RequestParam("imageProduct") MultipartFile imageProduct, Model model) {
        validateImageName(imageProduct, result);
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAll());
            return "product/edit";
        }

        Product current = productService.get(editProduct.getId());
        if (current == null) {
            return "error/404";
        }

        if (imageProduct != null && !imageProduct.isEmpty()) {
            try {
                productService.updateImage(editProduct, imageProduct);
            } catch (IllegalArgumentException ex) {
                result.rejectValue("image", "image.invalid", ex.getMessage());
                model.addAttribute("categories", categoryService.getAll());
                return "product/edit";
            }
        } else {
            editProduct.setImage(current.getImage());
        }

        editProduct.setCategory(categoryService.get(categoryId));
        productService.update(editProduct);
        return "redirect:/products";
    }

    @PostMapping("/delete/{id}")
    public String Delete(@PathVariable int id) {
        productService.delete(id);
        return "redirect:/products";
    }

    private void validateImageName(MultipartFile imageProduct, BindingResult result) {
        if (imageProduct == null || imageProduct.isEmpty()) {
            return;
        }
        String fileName = imageProduct.getOriginalFilename();
        if (fileName != null && fileName.length() > 200) {
            result.rejectValue("image", "image.length", "Tên hình ảnh không quá 200 kí tự");
        }
    }
}
