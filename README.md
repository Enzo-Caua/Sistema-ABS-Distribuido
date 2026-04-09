# Sistema de ABS Distribuído

Este projeto consiste em uma simulação distribuída de um sistema de frenagem **ABS (Anti-lock Braking System)** utilizando Java. O sistema é composto por múltiplos módulos independentes (Sensores, Atuadores, Servidor Central e Painel do Usuário) que se comunicam via rede utilizando protocolos TCP e UDP.

## 📌 Sumário

* [Softwares Utilizados](#-softwares-utilizados)
* [Arquitetura do Sistema](#-arquitetura-do-sistema)
* [Estrutura do Projeto](#-estrutura-do-projeto)
* [Especificações do Projeto](#-especificações-do-projeto)
* [Instalação e Execução](#-instalação-e-execução)
* [Autores](#-autores)

---

## 💻 Softwares Utilizados

* **Java JDK 17+** – Versão mínima recomendada para suporte a *switch expressions* e novas funcionalidades de concorrência.
* **JavaFX 17+** – Biblioteca utilizada para a construção das interfaces gráficas (GUIs).
* **Maven** – Gerenciador de dependências e automação de build.
* **Java Sockets API** – Utilizada para a comunicação de rede (TCP e UDP).
* **Scene Builder** – Ferramenta para o design dos layouts FXML.

---

## 🏗 Arquitetura do Sistema

O sistema opera de forma distribuída em quatro camadas principais:

1.  **Sensores (UDP):** Simulam a velocidade de rotação das rodas e enviam telemetria constante.
2.  **Servidor Central (TCP/UDP):** O "cérebro" do sistema. Processa a lógica de deslizamento (*slip*) e orquestra os comandos.
3.  **Atuadores (TCP):** Recebem comandos de frenagem e simulam a redução física da velocidade.
4.  **Painel do Usuário (TCP):** Interface de monitoramento e controle manual de frenagem.

---

## 🛠 Estrutura do Projeto

O projeto é organizado seguindo o padrão modular do Maven, separando a lógica de negócio dos ativos de interface.

```text
SISTEMA-ABS/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── atuadores/      # Lógica física e cliente de rede dos atuadores
│   │   │   ├── sensores/       # Simulação física e transmissão UDP das velocidades
│   │   │   ├── servidor/       # Servidor Central, Logica ABS e Gestores de conexão
│   │   │   ├── controllers/    # Controladores JavaFX (ponte UI <-> Código)
│   │   │   ├── telas/          # Classes de inicialização das aplicações JavaFX
│   │   │   └── usuario/        # Cliente de rede do painel de controle
│   │   └── resources/
│   │       ├── css/            # Estilização visual (botões, indicadores)
│   │       ├── imagens/        # Ícones e ativos gráficos
│   │       └── telas/          # Arquivos FXML (Layouts estruturais)
├── pom.xml                     # Configurações de dependências Maven
└── README.md                   # Documentação do projeto
```

### Detalhamento dos Módulos

*   **`atuadores`**: Contém o `AtuadorFreio` (cálculo de redução) e o `ClienteAtuadores` (comunicação TCP).
*   **`sensores`**: Contém o `SensorVelocidade` (física da roda) e o `ServidorUDP` (agregador de dados).
*   **`servidor`**: Contém a `LogicaABS` (algoritmo de decisão) e classes de persistência como `RegistroAtuadores`.
*   **`resources/telas`**: Define a interface visual de cada módulo de forma declarativa via FXML.

---

## ⚙ Especificações do Projeto

*   **Comunicação Híbrida:** 
    *   **UDP:** Envio de telemetria de alta frequência e descoberta automática de servidor via *Broadcast*.
    *   **TCP:** Comandos críticos de controle e troca de mensagens de estado entre usuários e servidor.
*   **Descoberta Automática (Service Discovery):** Clientes localizam o servidor na rede local sem necessidade de configurar o IP manualmente.
*   **Lógica ABS:** Algoritmo que monitora a velocidade de referência (vRef) e atua caso a diferença de velocidade entre as rodas indique um possível travamento.
*   **Hierarquia de Controle:** O comando manual do usuário (0% a 100%) possui prioridade sobre a lógica automática do ABS.
*   **Watchdog de Segurança:** O servidor detecta quedas de sensores e reseta os atuadores para evitar frenagens fantasmas.

---

## 🚀 Instalação e Execução

1.  **Requisitos:** Certifique-se de ter o **Maven** e o **JDK 17** instalados.
2.  **Build:** Execute `mvn clean install` na raiz do projeto.
3.  **Ordem de Execução:**
    *   1º: Inicie o **Servidor Central** (`servidor.ServidorCentral`).
    *   2º: Inicie os **Sensores** (`sensores.MainSensores`) e configure as rodas no terminal.
    *   3º: Inicie os **Atuadores** (`atuadores.MainAtuadores`) configurando as rodas correspondentes.
    *   4º: Inicie o **Painel do Usuário** (`telas.UsuarioApp`).
4.  **Uso:** Interaja com as ComboBoxes no Painel do Usuário para aplicar freio manual ou observe o ABS atuando automaticamente ao simular disparidade de velocidades.

---

## 👥 Autores

*   **Enzo Cauã da S. Barbosa**

Tutoria: **Prof. Dr. José Amancio Macedo Santos**
