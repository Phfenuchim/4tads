package com.livestock.modules.product.services;

import com.livestock.modules.product.domain.product.Product;
import com.livestock.modules.product.domain.product_image.Product_image;
import com.livestock.modules.product.dto.CreateProductDTO;
import com.livestock.modules.product.dto.UpdateProductDTO;
import com.livestock.modules.product.exceptions.ProductInputException;
import com.livestock.modules.product.exceptions.ProductNotFoundException;
import com.livestock.modules.product.repositories.ProductImageRepository;
import com.livestock.modules.product.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    public Product createProduct(CreateProductDTO createProductDTO) {
        if (createProductDTO.getProductName() == null || createProductDTO.getProductName().isEmpty()) {
            throw new ProductInputException("Nome do produto não pode ser vazio!");
        }

        if (createProductDTO.getPrice() == null || createProductDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductInputException("Preço do produto deve ser maior que zero!");
        }

        if (productRepository.existsByProductName(createProductDTO.getProductName())) {
            throw new ProductInputException("Já existe um produto com este nome!");
        }

        Product product = new Product();
        product.setProductName(createProductDTO.getProductName());
        product.setDescription(createProductDTO.getDescription());
        product.setPrice(createProductDTO.getPrice());
        product.setQuantity(createProductDTO.getQuantity());
        product.setActive(true);

        return productRepository.save(product);
    }

    public Page<Product> getAllProductsPaginated(int pageNumber, int pageSize) {
        var products = this.productRepository.findAll(PageRequest.of(pageNumber, pageSize));

        if (products.isEmpty()) {
            throw new ProductNotFoundException("Nenhum produto encontrado!");
        }

        return products;
    }

    public List<Product> findAllProductsByNameFilter(String name) {
        var products = this.productRepository.findAllByProductNameContaining(name);

        if (products.isEmpty()) {
            throw new ProductNotFoundException("Nenhum produto encontrado com este nome!");
        }

        return products;
    }

    public boolean updateProductActiveStatus(UUID id, boolean active) {
        if (id == null) {
            throw new ProductInputException("ID do produto não pode ser nulo!");
        }

        var lines = productRepository.updateActiveProduct(id, active);

        if (lines == 0) {
            throw new IllegalArgumentException("Não foi possível ativar/desativar o produto!");
        }

        return true;
    }

    public boolean updateProductPrice(UUID id, BigDecimal price) {
        if (id == null) {
            throw new ProductInputException("ID do produto não pode ser nulo!");
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductInputException("Preço do produto deve ser maior que zero!");
        }

        var lines = productRepository.updateProductPrice(id, price);

        if (lines == 0) {
            throw new IllegalArgumentException("Não foi possível atualizar o preço do produto!");
        }

        return true;
    }

    public boolean updateProductQuantity(UUID id, Integer quantity) {
        if (id == null) {
            throw new ProductInputException("ID do produto não pode ser nulo!");
        }

        if (quantity == null || quantity < 0) {
            throw new ProductInputException("Quantidade do produto não pode ser negativa!");
        }

        var lines = productRepository.updateProductQuantity(id, quantity);

        if (lines == 0) {
            throw new IllegalArgumentException("Não foi possível atualizar a quantidade do produto!");
        }

        return true;
    }

    public Product updateProduct(UUID id, UpdateProductDTO updateProductDTO) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Produto não encontrado com o ID: " + id));

        if (updateProductDTO.getProductName() != null) {
            product.setProductName(updateProductDTO.getProductName());
        }

        if (updateProductDTO.getDescription() != null) {
            product.setDescription(updateProductDTO.getDescription());
        }

        if (updateProductDTO.getPrice() != null) {
            product.setPrice(updateProductDTO.getPrice());
        }

        if (updateProductDTO.getQuantity() != null) {
            product.setQuantity(updateProductDTO.getQuantity());
        }

        if (updateProductDTO.getActive() != null) {
            product.setActive(updateProductDTO.getActive());
        }

        return productRepository.save(product);
    }

    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Produto não encontrado com o ID: " + id));
    }

    @Transactional
    public Product_image addImageToProduct(UUID productId, String pathUrl, boolean isDefault) {
        Product product = getProductById(productId);

        if (isDefault) {
            // Reset any existing default images
            productImageRepository.resetDefaultImagesForProduct(productId);
        }

        Product_image productImage = new Product_image();
        productImage.setProduct(product);
        productImage.setPathUrl(pathUrl);
        productImage.setDefaultImage(isDefault);

        return productImageRepository.save(productImage);
    }

    @Transactional
    public void setDefaultImage(Long imageId, UUID productId) {
        // Reset existing default images
        productImageRepository.resetDefaultImagesForProduct(productId);

        // Set the new default image
        productImageRepository.setDefaultImage(imageId);
    }

    public List<Product_image> getProductImages(UUID productId) {
        return productImageRepository.findByProductId(productId);
    }

    @Transactional
    public void deleteProductImage(Long imageId) {
        productImageRepository.deleteById(imageId);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Produto não encontrado com o ID: " + id);
        }

        // Images will be deleted automatically due to cascade
        productRepository.deleteById(id);
    }
}
