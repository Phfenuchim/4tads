# Anotations Basics

## Metodos principais
### GET
### POST
### DELETE
### PaTH

## Anotations

### @SpringBootApplication
#### define o ponto inicial da aplicação/API

### @Service
#### Camada para regras de negocio, instacia o repositório, defini a classe/camada de serviço  

### @Controller
#### Define os endpoints, para a aplicação/API

### @RestController
#### incluiu o @ResponseBody

### @ResquestBody
#### Auxilia a aplicação/API a fazer a resquisição ex: POST, PATH. Que solicita, mas não exiji que o objeto esteja preenchido 

### @Column
#### @Column(unique=true) para definir coluna com valores unicos

### @PostMapping ("/create")
####ex: https://localhost:8080/api/(metodo)
### @GetMapping ("/all")
####ex: https://localhost:8080/api/(metodo)
### @DeleteMapping ("/delete")
####ex: https://localhost:8080/api/(metodo)

### @AutoWired
#### Injeção de dependência, para tabalhar com aplicação com varias camadas, conectando e comunicando diferentes camadas do serviço
 *	@AutoWired
	private classImportada repository
 * 	public classConstructor (classImportada repository){
		this.repository =	repository;
	}
### @PathVareable

## lombok - boilerplate (IMPORTANTE PARA ABSTRAÇÕES)

### @Data
#### usada para getters e setters
### @NoArgsConstructor
#### construtor sem parametro
### @AllArgsConstructor
#### construtor com todos os parametro


## Banco de dados

### @Entity
#### para criar/verificar tabelas no banco de dados, analisando as linhas abaixo da sua declaração ex: classe
### @Table(name = "nomeTable")
#### nomear tabela no banco de dados
### @id
#### declarado antes do atributo da classe, identifica o atributo como pk 
### @\GeneratedValue (strategy = GeneratedValue."estartegia ex:IDENTITY")
#### declarado antes do atributo da classe, serve para definir que o mesmo seja unico dentro do banco de dados
### @ManyToOne (IMPORTANTE)
#### definir cardinalidade, de 1_n, ex:"fechadura dura com varias chaves| uma missão pode ter vairas pessoas|um usuario no banco com varias contas"
### @OneToMany
#### definir cardinalidade, de n_1, ex:"Chave unica para varias fechaduras| uma pessoa pode fazer uma missão por vez| cada conta no banco pode ter somente um u´suário" 
