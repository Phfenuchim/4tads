package com.livestock.modules.product.services;

import com.livestock.modules.product.domain.product.Product;
import com.livestock.modules.product.domain.product_image.Product_image;
import com.livestock.modules.product.dto.CreateProductDTO;
import com.livestock.modules.product.dto.UpdateProductDTO;
import com.livestock.modules.product.exceptions.ProductInputException;
import com.livestock.modules.product.exceptions.ProductNotFoundException;
import com.livestock.modules.product.repositories.ProductImageRepository;
import com.livestock.modules.product.repositories.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    // Construtor para injeção de dependências.
    public ProductService(ProductRepository productRepository,
                          ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
    }

    public Product createProduct(CreateProductDTO createProductDTO) {
        // Avisar: Validações de DTO movidas para o início do método.
        if (createProductDTO.getProductName() == null || createProductDTO.getProductName().isEmpty()) {
            throw new ProductInputException("Nome do produto não pode ser vazio!");
        }
        if (createProductDTO.getProductName().length() > 200) {
            throw new ProductInputException("Nome do produto não pode exceder 200 caracteres.");
        }
        if (createProductDTO.getDescription() != null && createProductDTO.getDescription().length() > 2000) { // Avisar: Adicionada verificação de nulidade para descrição.
            throw new ProductInputException("Descrição não pode exceder 2000 caracteres.");
        }
        if (createProductDTO.getQuantity() < 0) {
            throw new ProductInputException("Quantidade do produto não pode ser negativa!");
        }
        if (createProductDTO.getPrice() == null || createProductDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductInputException("Preço do produto deve ser maior que zero!");
        }
        if (createProductDTO.getRating() != null &&
                (createProductDTO.getRating().compareTo(BigDecimal.valueOf(1)) < 0 || // Avisar: Usar BigDecimal.valueOf(1) para Rating.
                        createProductDTO.getRating().compareTo(BigDecimal.valueOf(5)) > 0)) { // Avisar: Usar BigDecimal.valueOf(5) para Rating.
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
        product.setActive(true); // Avisar: Definindo produto como ativo por padrão na criação.

        return productRepository.save(product);
    }

    public Page<Product> getAllActiveProductsPaginated(int pageNumber, int pageSize) {
        return productRepository.findByActiveTrue(PageRequest.of(pageNumber, pageSize));
    }

    public Page<Product> getAllProductsForAdminPaginated(int pageNumber, int pageSize) {
        return productRepository.findAll(PageRequest.of(pageNumber, pageSize));
    }

    public List<Product> findActiveProductsByNameFilter(String name) {
        return productRepository.findByProductNameContainingIgnoreCaseAndActiveTrue(name);
    }

    public List<Product> findAllProductsByNameFilterForAdmin(String name) {
        return productRepository.findAllByProductNameContainingIgnoreCase(name);
    }

    public boolean updateProductActiveStatus(UUID id, boolean active) {
        if (id == null) {
            throw new ProductInputException("ID do produto não pode ser nulo!");
        }
        int lines = productRepository.updateActiveProduct(id, active); // Avisar: Nome da variável 'lines' é mais descritivo.
        if (lines == 0) {
            throw new ProductNotFoundException("Produto não encontrado ou status já é o desejado para o ID: " + id); // Avisar: Exceção mais específica.
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
        int lines = productRepository.updateProductPrice(id, price);
        if (lines == 0) {
            throw new ProductNotFoundException("Produto não encontrado ou preço já é o desejado para o ID: " + id); // Avisar: Exceção mais específica.
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
        int lines = productRepository.updateProductQuantity(id, quantity);
        if (lines == 0) {
            throw new ProductNotFoundException("Produto não encontrado ou quantidade já é a desejada para o ID: " + id); // Avisar: Exceção mais específica.
        }
        return true;
    }

    public Product updateProduct(UUID id, UpdateProductDTO updateProductDTO) {
        Product product = productRepository.findById(id) // Avisar: Nome da variável 'product' é mais descritivo.
                .orElseThrow(() -> new ProductNotFoundException("Produto não encontrado com o ID: " + id));

        // Avisar: Validações de DTO movidas para antes das atribuições.
        if (updateProductDTO.getProductName() != null) {
            if (updateProductDTO.getProductName().isEmpty()) {
                throw new ProductInputException("Nome do produto não pode ser vazio!");
            }
            if (updateProductDTO.getProductName().length() > 200) {
                throw new ProductInputException("Nome do produto não pode exceder 200 caracteres.");
            }
            // Avisar: Verificar se o novo nome já existe (exceto para o próprio produto)
            if (!product.getProductName().equalsIgnoreCase(updateProductDTO.getProductName()) &&
                    productRepository.existsByProductName(updateProductDTO.getProductName())) {
                throw new ProductInputException("Já existe outro produto com este nome!");
            }
            product.setProductName(updateProductDTO.getProductName());
        }
        if (updateProductDTO.getDescription() != null) {
            if (updateProductDTO.getDescription().length() > 2000) {
                throw new ProductInputException("Descrição não pode exceder 2000 caracteres.");
            }
            product.setDescription(updateProductDTO.getDescription());
        }
        if (updateProductDTO.getPrice() != null) {
            if (updateProductDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ProductInputException("Preço do produto deve ser maior que zero!");
            }
            product.setPrice(updateProductDTO.getPrice());
        }
        if (updateProductDTO.getQuantity() != null) {
            if (updateProductDTO.getQuantity() < 0) {
                throw new ProductInputException("Quantidade do produto não pode ser negativa!");
            }
            product.setQuantity(updateProductDTO.getQuantity());
        }
        if (updateProductDTO.getActive() != null) {
            product.setActive(updateProductDTO.getActive());
        }
        // Avisar: Rating não está sendo atualizado aqui. Se for para ser, adicionar lógica similar.

        return productRepository.save(product);
    }

    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Produto não encontrado com o ID: " + id));
    }

    @Transactional
    public Product_image addImageToProduct(UUID productId, String pathUrl, boolean isDefault) {
        Product product = getProductById(productId); // Reutiliza o método que já lança exceção se não encontrar.
        if (isDefault) {
            productImageRepository.resetDefaultImagesForProduct(productId);
        }
        Product_image newProductImage = new Product_image(); // Avisar: Nome da variável 'newProductImage' é mais descritivo.
        newProductImage.setProduct(product);
        newProductImage.setPathUrl(pathUrl);
        newProductImage.setDefaultImage(isDefault);
        return productImageRepository.save(newProductImage);
    }

    @Transactional
    public void setDefaultImage(Long imageId, UUID productId) {
        productImageRepository.resetDefaultImagesForProduct(productId);
        int updatedRows = productImageRepository.setDefaultImage(imageId);
        if (updatedRows == 0) {
            throw new IllegalArgumentException("Imagem não encontrada ou não foi possível definir como padrão. ID da Imagem: " + imageId); // Avisar: Exceção mais específica.
        }
    }

    public List<Product_image> getProductImages(UUID productId) {
        // Avisar: Considerar verificar se o produto existe antes de buscar imagens, ou deixar o repositório retornar lista vazia.
        return productImageRepository.findByProductId(productId);
    }

    @Transactional
    public void deleteProductImage(Long imageId) {
        // Avisar: Adicionar verificação se a imagem existe antes de deletar para evitar erro silencioso
        // ou para lançar uma ProductImageNotFoundException.
        if (!productImageRepository.existsById(imageId)) {
            throw new IllegalArgumentException("Imagem não encontrada para o ID: " + imageId);
        }
        productImageRepository.deleteById(imageId);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Produto não encontrado com o ID: " + id);
        }
        // Avisar: O comentário "Images will be deleted automatically due to cascade" depende da configuração
        // do CascadeType na relação @OneToMany Product -> Product_image na entidade Product.
        // Se CascadeType.REMOVE ou CascadeType.ALL não estiver definido, as imagens não serão excluídas automaticamente.
        productRepository.deleteById(id);
    }
}
