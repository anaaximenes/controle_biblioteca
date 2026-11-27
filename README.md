# Sistema de Controle de Biblioteca

Aplicação desktop desenvolvida em **Java (Swing)** com persistência em **SQLite** para o gerenciamento completo de acervo, exemplares, leitores, funcionários, reservas, empréstimos e multas de uma biblioteca.

O projeto aplica pilares da **Programação Orientada a Objetos (POO)** — herança, polimorfismo, encapsulamento, enums, exceções personalizadas e o padrão arquitetural **DAO (Data Access Object)** — aliados a uma interface gráfica desktop.

---

## Sumário

- [Funcionalidades](#funcionalidades)
- [Regras de Negócio](#regras-de-negócio)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Banco de Dados](#banco-de-dados)
- [Como Executar](#como-executar)
- [Acesso Inicial](#acesso-inicial)
- [Testes](#testes)
- [Autores](#autores)

---

## Funcionalidades

A interface gráfica (`MainFrame`) organiza o sistema em módulos acessíveis pela barra lateral após o login:

- **Autenticação de Funcionários (`LoginFrame`)**: controle de acesso com senhas armazenadas em hash **SHA-256** (`SegurancaUtil`) e criação automática de um usuário administrador na primeira execução.
- **Empréstimos e Multas (`EmprestimoPanel`)**: registro de retiradas (para leitores e funcionários), definição automática da data prevista de devolução, registro de devoluções, liberação automática da cópia e cálculo de multas por atraso.
- **Reservas (`ReservaPanel`)**: criação e acompanhamento de reservas com máquina de estados (`ATIVA`, `CONCLUIDA`, `CANCELADA`, `EXPIRADA`), expiração automática em 3 dias e integração com o status das cópias.
- **Leitores (`LeitorPanel`)**: cadastro, edição, ativação/inativação e exclusão de leitores com controle de matrícula.
- **Obras do Acervo (`ObraPanel`)**: cadastro, atualização e exclusão de obras com título, autor, ano de publicação e ISBN.
- **Cópias (`CopiaPanel`)**: gerenciamento de exemplares físicos vinculados às obras, com código de barras único e status (`DISPONIVEL`, `EMPRESTADA`, `RESERVADA`, `MANUTENCAO`).
- **Funcionários (`FuncionarioPanel`)**: cadastro, atualização e remoção de funcionários com cargo e credenciais de login.

---

## Regras de Negócio

### Prazos e Limites (Herança e Polimorfismo)
As classes `Leitor` e `Funcionario` herdam da classe abstrata `Pessoa` e sobrescrevem métodos de regras específicas:

| Perfil | Prazo de Empréstimo | Limite de Livros | Identificador Formatado |
| :--- | :---: | :---: | :---: |
| **Leitor** | 15 dias | 3 livros | `MAT-<MATRICULA>` |
| **Funcionário** | 20 dias | 5 livros | `FUNC-0000` |

### Cálculo de Multa por Atraso
Ao finalizar uma devolução (`Emprestimo.finalizarDevolucao`), a multa é calculada automaticamente conforme os dias de atraso em relação à data prevista:

| Faixa de Atraso | Regra de Cálculo |
| :--- | :--- |
| Sem atraso (devolução no prazo) | R$ 0,00 |
| 1 a 29 dias de atraso | R$ 0,50 por dia de atraso |
| 30 a 59 dias de atraso | R$ 15,00 + R$ 0,25 por dia adicional acima de 30 dias |
| 60 dias ou mais | Teto máximo fixo de R$ 22,50 |

### Ciclo de Vida de Reservas
- Toda nova reserva inicia com status `ATIVA` e validade de **3 dias** a partir da data da reserva.
- Reservas ativas podem ser **concluídas** (`CONCLUIDA`) ou **canceladas** (`CANCELADA`).
- Caso se tente concluir uma reserva após a data de expiração, seu status passa para `EXPIRADA` e uma exceção é lançada.

### Exceções Personalizadas
- `SenhaInvalidaException` — lançada em tentativas de autenticação com senha incorreta, vazia ou nula.
- `ReservaInvalidaException` — lançada em transições inválidas de estado ou reservas expiradas.
- `DataDevolucaoInvalidaException` — lançada quando a data de devolução é nula, anterior à data do empréstimo ou quando o empréstimo já foi finalizado.

---

## Tecnologias Utilizadas

- **Java 11** (JDK 11+)
- **Apache Maven** (gerenciamento de dependências e build)
- **Java Swing** (interface gráfica desktop)
- **SQLite** com driver **sqlite-jdbc `3.45.1.0`** (banco de dados relacional embutido)
- **SHA-256** (`java.security.MessageDigest`) para criptografia de senhas

---

## Estrutura do Projeto

```text
controle_biblioteca/
├── pom.xml
├── BDBiblioteca.db                         # Banco SQLite gerado/atualizado pela aplicação
└── src/
    ├── main/java/com/poo/biblioteca/
    │   ├── Pessoa.java                     # Classe abstrata base (herança/polimorfismo)
    │   ├── Leitor.java
    │   ├── Funcionario.java
    │   ├── Obra.java
    │   ├── Copia.java
    │   ├── Emprestimo.java
    │   ├── Reserva.java
    │   ├── StatusCopia.java                # Enum: DISPONIVEL, EMPRESTADA, RESERVADA, MANUTENCAO
    │   ├── StatusReserva.java              # Enum: ATIVA, CONCLUIDA, CANCELADA, EXPIRADA
    │   ├── dao/                            # Camada de persistência JDBC/SQLite
    │   │   ├── CopiaDAO.java
    │   │   ├── EmprestimoDAO.java
    │   │   ├── FuncionarioDAO.java
    │   │   ├── LeitorDAO.java
    │   │   ├── ObraDAO.java
    │   │   └── ReservaDAO.java
    │   ├── exception/                      # Exceções de domínio
    │   │   ├── DataDevolucaoInvalidaException.java
    │   │   ├── ReservaInvalidaException.java
    │   │   └── SenhaInvalidaException.java
    │   ├── ui/                             # Interface gráfica (Swing) e camada de serviço
    │   │   ├── BibliotecaApp.java          # Classe principal (ponto de entrada da UI)
    │   │   ├── BibliotecaService.java
    │   │   ├── LoginFrame.java
    │   │   ├── MainFrame.java
    │   │   ├── EmprestimoPanel.java
    │   │   ├── ReservaPanel.java
    │   │   ├── LeitorPanel.java
    │   │   ├── ObraPanel.java
    │   │   ├── CopiaPanel.java
    │   │   ├── FuncionarioPanel.java
    │   │   ├── UIConstants.java
    │   │   └── UIHelper.java
    │   └── util/
    │       └── SegurancaUtil.java          # Geração de hash SHA-256
    └── test/java/com/poo/biblioteca/
        └── Main.java                       # Suíte de testes de modelos, regras e DAOs
```

---

## Banco de Dados

O sistema utiliza um banco de dados local SQLite no arquivo **`BDBiblioteca.db`** (na raiz do projeto). Não é necessária instalação ou configuração manual de servidor de banco de dados: todas as tabelas são criadas automaticamente na inicialização pelos DAOs e pelo `BibliotecaService`:

- `funcionario`
- `leitor`
- `obra`
- `copia`
- `reserva`
- `emprestimo`
- `emprestimo_funcionario_leitor` (tabela auxiliar para empréstimos realizados por funcionários)

---

## Como Executar

### Pré-requisitos
- **JDK 11** ou superior instalado
- **Apache Maven** (ou uma IDE com suporte a Maven, como **IntelliJ IDEA**)

### 1. Clonar o repositório
```bash
git clone https://github.com/anaaximenes/controle_biblioteca.git
cd controle_biblioteca
```

### 2. Executar pela IDE (Recomendado)
1. Abra o projeto no **IntelliJ IDEA** (ou Eclipse / VS Code) como um projeto Maven e aguarde o download da dependência `sqlite-jdbc`.
2. Execute a classe principal da interface gráfica:
   ```text
   src/main/java/com/poo/biblioteca/ui/BibliotecaApp.java
   ```

### 3. Executar pelo Terminal (Maven)
Compile o projeto e inicie a aplicação Swing com:
```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.poo.biblioteca.ui.BibliotecaApp"
```

---

## Acesso Inicial

Ao abrir o sistema pela primeira vez, o método `garantirFuncionarioPadrao` em `BibliotecaApp` verifica se o usuário administrador já existe e, caso contrário, cria automaticamente a conta padrão:

- **Login:** `admin`
- **Senha:** `admin`

> Após o primeiro acesso, é possível cadastrar novos funcionários ou alterar as credenciais diretamente pelo painel **Funcionários**.

---

## Testes

O projeto conta com uma suíte de testes manuais em console na classe [`src/test/java/com/poo/biblioteca/Main.java`](src/test/java/com/poo/biblioteca/Main.java), cobrindo:
- Polimorfismo (`Pessoa`, `Leitor` e `Funcionario`), autenticação e hash SHA-256;
- Gestão de obras, cópias e enums de status;
- Máquina de estados de reservas e tratamento de exceções;
- Regras de devolução e todas as faixas de cálculo de multa;
- Operações CRUD em todos os DAOs e cenário integrado ponta a ponta.

Para executar os testes, rode a classe `com.poo.biblioteca.Main` pela IDE ou via terminal:
```bash
mvn test-compile
mvn exec:java -Dexec.classpathScope=test -Dexec.mainClass="com.poo.biblioteca.Main"
```

---

## Autores

- **Ana Beatriz**
- **Gustavo Carneiro**
