# Criando dockerfile da sua aplicação (.jar)

## Preparação 
### Limpeza do lifecycle da aplicação, para apagar target anterior
 *  lifecycle clean
### Para realizar o build da aplicação
 * lifecycle package
### usando comando por mvn 
 * mvn clean package


### Crie um arquivo como:(Case sensitive) 
 * Dockerfile

# Docker Build 
## Processo: Dentro da pasta target da aplicação, via terminal
### 1 - construção do arquivo dockerfile:
 *	FROM (image)
	label maintainer:("mantenedor")
	WORKDIR /app (criar diretório)
	copy target/project-SNAPSHOT.jar /app/proect-docer.jar(copia do snapshot)
	ENTRYPOINT ["java, "-jar", "project.jar"]
### 2 - Excutar dentro do container:
	docker build (default)
	docker build -t imagem-spring:0.0.1 .(nome da imagem:versão desejada vulgo tag + "." indicando o diretório, que no caso é a raiz onde o projeto se encontra)
### 3 - Verificando imagens criadas
 *	docker images 
 
# Docker run
## Comando de verificação
### Verificar containers rodando
 *	docker ps
### Verificar lista de container
 *	docker ps -a
 
# Docker push
## 2 - Criar Repositório (necessário o username do dokcerhub)
	
## 2 - Criar imagem pronta da aplicação (preparação da imagem antes do push)
### Para criar Repositório (necessário o username do dokcerhub)
 *	docker tag docker/imagem-spring:0.0.1 phfenuchim/primeira imagem-app:0.0.1(nome da imagem+:+tag versão container + username+/+nome do repositório+:+tag de versão do repositório)
## 3 - Verificar image criada
### Comando para verificar
 * 	docker images
### imagem será igual a:
 *	username/imagem-spring
## Enviar o imagem para o dockerhub:
### Logar pelo terminal
 *	docker login
 *	usuario senha
### Enviar:
 *	docker push username/imagem-spring:0.0.1
 
# Rodando docker compose
## Criar arquivo.yml na raiz do projeto
 *	services:
		postgres:
			container_name: livestock
			image: postgres
			ports:
			- 5432:5432
			environment:
			- POSTGRES_USER=postgres
			- POSTGRES_PASSWORD=admin
			- POSTGRES_DB=livestock_db
			
## Gerando imagens do arquivo.yml
###na pasta do projeto: para baixar dependencias para excução(postgre nativo deve estar inativo)
 * 	docker compose up -d
