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
        if (createProductDTO.getProductName().length() > 200) {
            throw new ProductInputException("Nome do produto não pode exceder 200 caracteres.");
        }
        if (createProductDTO.getDescription().length() > 2000) {
            throw new ProductInputException("Descrição não pode exceder 2000 caracteres.");
        }

        if (createProductDTO.getPrice() == null || createProductDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductInputException("Preço do produto deve ser maior que zero!");
        }
        if (createProductDTO.getRating() != null &&
                (createProductDTO.getRating().compareTo(new BigDecimal("1")) < 0 ||
                        createProductDTO.getRating().compareTo(new BigDecimal("5")) > 0)) {
            throw new IllegalArgumentException("Avaliação deve estar entre 1 e 5.");
        }

        if (productRepository.existsByProductName(createProductDTO.getProductName())) {
            throw new ProductInputException("Já existe um produto com este nome!");
        }

        Product product = new Product();
        product.setProductName(createProductDTO.getProductName());
        product.setDescription(createProductDTO.getDescription());
        product.setPrice(createProductDTO.getPrice());
        product.setQuantity(createProductDTO.getQuantity());
        product.setRating(createProductDTO.getRating());
        product.setActive(true);

        return productRepository.save(product);
    }

    public Page<Product> getAllProductsPaginated(int pageNumber, int pageSize) {
        return this.productRepository.findAll(PageRequest.of(pageNumber, pageSize));
    }


    public List<Product> findAllProductsByNameFilter(String name) {
        return this.productRepository.findAllByProductNameContainingIgnoreCase(name);
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

        Product_image product_image = new Product_image();
        product_image.setProduct(product);
        product_image.setPathUrl(pathUrl);
        product_image.setDefaultImage(isDefault);

        return productImageRepository.save(product_image);
    }

    @Transactional
    public void setDefaultImage(Long imageId, UUID productId) {
        // Redefine todas as imagens como não-padrão
        productImageRepository.resetDefaultImagesForProduct(productId);

        // Define a nova imagem como padrão
        int updatedRows = productImageRepository.setDefaultImage(imageId);

        if (updatedRows == 0) {
            throw new IllegalArgumentException("Não foi possível definir a imagem padrão.");
        }
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
