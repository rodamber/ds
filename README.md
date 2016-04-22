# Projeto de Sistemas Distribuídos 2015-2016 #

Grupo de SD 2 - Campus Alameda

Mariana Ribeiro 78606 marianasofiaribeiro@tecnico.ulisboa.pt

Bernardo Cordeiro 78778 bernardo.f.cordeiro@tecnico.ulisboa.pt

Rodrigo Bernardo 78942 rodrigo.bernardo@tecnico.ulisboa.pt


Repositório:
[tecnico-distsys/A_02-project](https://github.com/tecnico-distsys/A_02-project/)

-------------------------------------------------------------------------------

## Instruções de instalação


### Ambiente

[0] Iniciar sistema operativo

Linux

[1] Iniciar servidores de apoio

JUDDI:
```
wget http://disciplinas.tecnico.ulisboa.pt/leic-sod/2015-2016/download/juddi-3.3.2_tomcat-7.0.64_9090.zip
unzip juddi-3.3.2_tomcat-7.0.64_9090.zip
chmod +x juddi-3.3.2_tomcat-7.0.64_9090/bin/*.sh
./juddi-3.3.2_tomcat-7.0.64_9090/bin/startup.sh &
```

[2] Obter código fonte do projeto (versão entregue)

```
git clone https://github.com/tecnico-distsys/A_02-project.git --branch v1.0 --single-branch
```


[3] Instalar módulos de bibliotecas auxiliares

```
wget http://disciplinas.tecnico.ulisboa.pt/leic-sod/2015-2016/labs/05-ws1/uddi-naming.zip
unzip uddi-naming.zip -d uddi-naming
mvn -f uddi-naming/pom.xml clean install
```

-------------------------------------------------------------------------------

### Serviço TRANSPORTER

[1] Construir e executar **servidor**

```
mvn -f A_02-project/transporter-ws/pom.xml clean install exec:java &
```

[2] Construir **cliente** e executar testes

```
mvn -f A_02-project/transporter-ws-cli/pom.xml clean install
```

...


-------------------------------------------------------------------------------

### Serviço BROKER

[1] Construir e executar **servidor**

```
mvn -f A_02-project/broker-ws/pom.xml clean install exec:java &
```


[2] Construir **cliente** e executar testes

```
mvn -f A_02-project/broker-ws-cli/pom.xml clean install
```

...

-------------------------------------------------------------------------------
### Tudo de seguida

wget http://disciplinas.tecnico.ulisboa.pt/leic-sod/2015-2016/download/juddi-3.3.2_tomcat-7.0.64_9090.zip
unzip juddi-3.3.2_tomcat-7.0.64_9090.zip
chmod +x juddi-3.3.2_tomcat-7.0.64_9090/bin/*.sh
./juddi-3.3.2_tomcat-7.0.64_9090/bin/startup.sh &

git clone https://github.com/tecnico-distsys/A_02-project.git --branch v1.0 --single-branch

wget http://disciplinas.tecnico.ulisboa.pt/leic-sod/2015-2016/labs/05-ws1/uddi-naming.zip
unzip uddi-naming.zip -d uddi-naming
mvn -f uddi-naming/pom.xml clean install

mvn -f A_02-project/transporter-ws/pom.xml clean install exec:java &
mvn -f A_02-project/transporter-ws-cli/pom.xml clean install
mvn -f A_02-project/broker-ws/pom.xml clean install exec:java &
mvn -f A_02-project/broker-ws-cli/pom.xml clean install

-------------------------------------------------------------------------------
**FIM**
